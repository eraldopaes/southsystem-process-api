package br.com.southsystem.process.service;

import br.com.southsystem.process.domain.enums.FileImportStatusEnum;
import br.com.southsystem.process.dto.FileImportDTO;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static br.com.southsystem.process.config.broker.RabbitMQConfig.FILE_IMPORT_QUEUE;

@Service
public class FileImportListener {

    private final FileImportService fileImportService;

    @Autowired
    public FileImportListener(FileImportService fileImportService) {
        this.fileImportService = fileImportService;
    }

    @RabbitListener(queues = FILE_IMPORT_QUEUE)
    public void importFileQueueListener(FileImportDTO fileImportDTO) {
        try {
            fileImportService.process(fileImportDTO);
            fileImportService.changeFileImportStatus(fileImportDTO.getId(), FileImportStatusEnum.PROCESSED, null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
