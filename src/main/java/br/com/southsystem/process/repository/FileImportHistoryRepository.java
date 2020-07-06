package br.com.southsystem.process.repository;

import br.com.southsystem.process.domain.FileImportHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileImportHistoryRepository extends JpaRepository<FileImportHistory, Integer> {
}
