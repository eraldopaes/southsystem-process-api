package br.com.southsystem.process.service;

import br.com.southsystem.process.domain.*;
import br.com.southsystem.process.domain.enums.BucketTypeEnum;
import br.com.southsystem.process.domain.enums.FileImportStatusEnum;
import br.com.southsystem.process.dto.FileImportDTO;
import br.com.southsystem.process.exceptionhandler.BusinessException;
import br.com.southsystem.process.exceptionhandler.FileProcessException;
import br.com.southsystem.process.mapper.LineMapper;
import br.com.southsystem.process.repository.FileImportRepository;
import br.com.southsystem.process.storage.Storage;
import br.com.southsystem.process.utils.LocalDateTimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

import static br.com.southsystem.process.utils.Constants.*;

@Service
public class FileImportService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileImportService.class);
    private static final String LINE_WITH_NOT_RECOGNIZED_ENTITY = "Entidade não esperada no arquivo!";
    private static final String FILE_WITH_ERRORS = "O arquivo possui erro não mapeado";

    private final FileImportRepository fileImportRepository;
    private final Storage storage;
    private final LineMapper<Client> clientMapper;
    private final LineMapper<Salesman> salesmanMapper;
    private final LineMapper<Sale> saleMapper;
    private final LocalDateTimeUtils localDateTimeUtils;
    private final FileImportHistoryService fileImportHistoryService;

    @Autowired
    public FileImportService(FileImportRepository fileImportRepository,
                             Storage storage,
                             LineMapper<Client> clientMapper,
                             LineMapper<Salesman> salesmanMapper,
                             LineMapper<Sale> saleMapper,
                             LocalDateTimeUtils localDateTimeUtils,
                             FileImportHistoryService fileImportHistoryService) {
        this.fileImportRepository = fileImportRepository;
        this.storage = storage;
        this.clientMapper = clientMapper;
        this.salesmanMapper = salesmanMapper;
        this.saleMapper = saleMapper;
        this.localDateTimeUtils = localDateTimeUtils;
        this.fileImportHistoryService = fileImportHistoryService;
    }

    @Transactional(propagation = Propagation.REQUIRED, noRollbackFor = Exception.class)
    public void process(FileImportDTO fileImportDTO) {
        FileImport fileImport = changeFileImportStatus(fileImportDTO.getId(), FileImportStatusEnum.PROCESSING, null, null);
        InputStream inputStream = getFile(fileImport);
        readFile(inputStream, fileImport);
    }

    private void readFile(InputStream inputStream, FileImport fileImport) {

        Integer numberOfClients = 0;
        Integer numberOfSalesman = 0;

        BigDecimal biggestSale = BigDecimal.ZERO;
        String biggestSaleId = "";

        BigDecimal slowestSale = BigDecimal.valueOf(Long.MAX_VALUE);
        String worstSaleName = "";

        try {

            List<String> lines = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))
                    .lines()
                    .filter(line -> !line.isEmpty())
                    .collect(Collectors.toList());

            for (String line : lines) {
                if (line.startsWith(PREFIX_SALESMAN)) {

                    Salesman salesman = salesmanMapper.lineToEntity(line);
                    numberOfSalesman++;
                    LOGGER.info("Vendedor processado: {}", salesman);

                } else if (line.startsWith(PREFIX_CLIENT)) {

                    Client client = clientMapper.lineToEntity(line);
                    numberOfClients++;
                    LOGGER.info("Cliente processado: {}", client);

                } else if (line.startsWith(PREFIX_SALE)) {

                    Sale sale = saleMapper.lineToEntity(line);
                    BigDecimal totalSale = calculeTotalSale(sale);
                    if (totalSale.compareTo(biggestSale) >= 0) {
                        biggestSale = totalSale;
                        biggestSaleId = sale.getId();
                    }
                    if (totalSale.compareTo(slowestSale) <= 0) {
                        slowestSale = totalSale;
                        worstSaleName = sale.getSalesmanName();
                    }
                    LOGGER.info("Venda processada: {}", sale);

                } else {
                    throw new FileProcessException(LINE_WITH_NOT_RECOGNIZED_ENTITY, line);
                }
            }

            writeFile(fileImport.getFilename(), numberOfClients, numberOfSalesman, biggestSaleId, worstSaleName);

        } catch (Exception e) {
            if (e instanceof FileProcessException) {
                changeFileImportStatus(fileImport.getId(), FileImportStatusEnum.ERROR, ((FileProcessException) e).getLine(), e.getMessage());
                throw new FileProcessException(e.getMessage());
            }

            changeFileImportStatus(fileImport.getId(), FileImportStatusEnum.ERROR, FILE_WITH_ERRORS, null);
            throw new BusinessException("Erro não mapeado ao processar arquivos");
        }
    }

    private BigDecimal calculeTotalSale(Sale sale) {
        return sale.getItems().stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO);
    }

    public void writeFile(String filename, Integer numberOfClients, Integer numberOfSalesman, String biggestSaleId, String worstSaleName) throws IOException {

        StringBuilder sb = new StringBuilder();
        sb.append(CLIENTS).append(numberOfClients).append("\n")
                .append(SALESMAN).append(numberOfSalesman).append("\n")
                .append(ID_BIGGEST_SALE).append(biggestSaleId).append("\n")
                .append(WORST_SALESMAN).append(worstSaleName);

        File file = new File(renameFile(filename));
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(sb.toString().getBytes());
        storage.save(file, BucketTypeEnum.FILE_OUTPUT);
    }

    private InputStream getFile(FileImport fileImport) {
        return storage.recover(fileImport.getFilename(), BucketTypeEnum.FILE_INPUT);
    }

    private String renameFile(String filename) {
        String filenameWithoutExtension = filename.replace(DAT_EXTENSION, "");
        return filenameWithoutExtension + DONE_EXTENSION;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public FileImport changeFileImportStatus(Integer fileImportId, FileImportStatusEnum fileImportStatusEnum, String line, String error) {
        FileImport fileImport = fileImportRepository.findById(fileImportId)
                .orElseThrow(() -> new FileProcessException("Arquivo não existe"));
        fileImport.setStatus(fileImportStatusEnum);
        fileImport.setError(error);
        fileImport.setErrorLine(line);
        FileImport fileImportSaved = fileImportRepository.save(fileImport);

        FileImportHistory fileImportHistory = new FileImportHistory();
        fileImportHistory.setFileImport(fileImportSaved);
        fileImportHistory.setStatus(fileImportStatusEnum);
        fileImportHistory.setDate(localDateTimeUtils.getLocalDateTime());
        fileImportHistoryService.save(fileImportHistory);

        return fileImportSaved;
    }
}
