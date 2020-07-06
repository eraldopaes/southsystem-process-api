package br.com.southsystem.process.service;

import br.com.southsystem.process.domain.FileImportHistory;
import br.com.southsystem.process.repository.FileImportHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FileImportHistoryService {

    private final FileImportHistoryRepository fileImportHistoryRepository;

    @Autowired
    public FileImportHistoryService(FileImportHistoryRepository fileImportHistoryRepository) {
        this.fileImportHistoryRepository = fileImportHistoryRepository;
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void save(FileImportHistory history) {
        fileImportHistoryRepository.save(history);
    }
}
