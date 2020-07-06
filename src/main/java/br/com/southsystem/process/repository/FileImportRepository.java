package br.com.southsystem.process.repository;

import br.com.southsystem.process.domain.FileImport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FileImportRepository extends JpaRepository<FileImport, Integer> {

    Optional<FileImport> findById(Integer id);
}
