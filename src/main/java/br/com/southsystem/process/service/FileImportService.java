package br.com.southsystem.process.service;

import br.com.southsystem.process.domain.*;
import br.com.southsystem.process.domain.enums.BucketTypeEnum;
import br.com.southsystem.process.domain.enums.FileImportStatusEnum;
import br.com.southsystem.process.dto.FileImportDTO;
import br.com.southsystem.process.exceptionhandler.BusinessException;
import br.com.southsystem.process.mapper.ClientMapper;
import br.com.southsystem.process.mapper.SaleMapper;
import br.com.southsystem.process.mapper.SalesmanMapper;
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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
public class FileImportService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileImportService.class);
    public static final String LINE_WITH_NOT_RECOGNIZED_ENTITY = "Entidade não esperada no arquivo!";
    public static final String FILE_WITH_ERRORS = "O arquivo possui erros";

    private final FileImportRepository fileImportRepository;
    private final Storage storage;
    private final ClientMapper clientMapper;
    private final SalesmanMapper salesmanMapper;
    private final SaleMapper saleMapper;
    private final LocalDateTimeUtils localDateTimeUtils;
    private final FileImportHistoryService fileImportHistoryService;

    @Autowired
    public FileImportService(FileImportRepository fileImportRepository,
                             Storage storage,
                             ClientMapper clientMapper,
                             SalesmanMapper salesmanMapper,
                             SaleMapper saleMapper,
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
        FileImport fileImport = changeFileImportStatus(fileImportDTO.getId(), FileImportStatusEnum.PROCESSING, null);
        InputStream inputStream = getFile(fileImport);
        readFile(inputStream, fileImport);
    }

    private void readFile(InputStream inputStream, FileImport fileImport) {

        AtomicInteger numberOfClients = new AtomicInteger(0);
        AtomicInteger numberOfSalesman = new AtomicInteger(0);
        AtomicReference<BigDecimal> biggestSale = new AtomicReference<>(BigDecimal.ZERO);
        AtomicReference<String> biggestSaleId = new AtomicReference<>("");
        AtomicReference<BigDecimal> slowestSale = new AtomicReference<>(BigDecimal.valueOf(Long.MAX_VALUE));
        AtomicReference<String> worstSaleName = new AtomicReference<>("");

        try {

            List<String> lines = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.toList());

            lines.forEach(line -> {

                if (line.startsWith("001")) {

                    Salesman salesman = salesmanMapper.lineToSalesman(line);
                    numberOfSalesman.getAndIncrement();
                    LOGGER.info("Vendedor processado: {}", salesman);

                } else if (line.startsWith("002")) {

                    Client client = clientMapper.lineToClient(line);
                    numberOfClients.getAndIncrement();
                    LOGGER.info("Cliente processado: {}", client);

                } else if (line.startsWith("003")) {

                    Sale sale = saleMapper.lineToSale(line);
                    BigDecimal totalSale = calculeTotalSale(sale);
                    if (totalSale.compareTo(biggestSale.get()) >= 0) {
                        biggestSale.set(totalSale);
                        biggestSaleId.set(sale.getId());
                    }
                    if (totalSale.compareTo(slowestSale.get()) <= 0) {
                        slowestSale.set(totalSale);
                        worstSaleName.set(sale.getSalesmanName());
                    }
                    LOGGER.info("Venda processada: {}", sale);

                } else {
                    changeFileImportStatus(fileImport.getId(), FileImportStatusEnum.ERROR, LINE_WITH_NOT_RECOGNIZED_ENTITY);
                    throw new BusinessException("file-import-service.invalid-file");
                }
            });

            writeFile(fileImport.getFilename(), numberOfClients.get(), numberOfSalesman.get(), biggestSaleId.get(), worstSaleName.get());

        } catch (Exception e) {

            if (e instanceof BusinessException) {
                throw new BusinessException(((BusinessException) e).getErrorCode());
            }

            changeFileImportStatus(fileImport.getId(), FileImportStatusEnum.ERROR, FILE_WITH_ERRORS);
            throw new BusinessException("file-import-service.invalid-file");
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
        sb.append("numberOfClients: ").append(numberOfClients).append("\n")
                .append("numberOfSalesman: ").append(numberOfSalesman).append("\n")
                .append("biggestSaleId: ").append(biggestSaleId).append("\n")
                .append("worstSaleName: ").append(worstSaleName);

        File file = new File(renameFile(filename));
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(sb.toString().getBytes());
        storage.save(file, BucketTypeEnum.FILE_OUTPUT);
    }

    private InputStream getFile(FileImport fileImport) {
        return storage.recover(fileImport.getFilename(), BucketTypeEnum.FILE_INPUT);
    }

    private String renameFile(String filename) {
        String filenameWithoutExtension = filename.replace(".dat", "");
        return filenameWithoutExtension + ".done.dat";
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public FileImport changeFileImportStatus(Integer fileImportId, FileImportStatusEnum fileImportStatusEnum, String error) {
        FileImport fileImport = fileImportRepository.findById(fileImportId)
                .orElseThrow(() -> new BusinessException("file-import-service.not-found"));
        fileImport.setStatus(fileImportStatusEnum);
        fileImport.setError(error);
        FileImport fileImportSaved = fileImportRepository.save(fileImport);

        FileImportHistory fileImportHistory = new FileImportHistory();
        fileImportHistory.setFileImport(fileImportSaved);
        fileImportHistory.setStatus(fileImportStatusEnum);
        fileImportHistory.setDate(localDateTimeUtils.getLocalDateTime());
        fileImportHistoryService.save(fileImportHistory);

        return fileImportSaved;
    }
}