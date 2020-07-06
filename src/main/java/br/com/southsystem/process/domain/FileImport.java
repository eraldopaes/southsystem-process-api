package br.com.southsystem.process.domain;

import br.com.southsystem.process.domain.enums.FileImportStatusEnum;
import lombok.Data;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@DynamicUpdate
@Table(name = "file_import")
public class FileImport {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "file_import_id")
    private Integer id;

    @Column(name = "file_import_filename")
    private String filename;

    @Enumerated(EnumType.STRING)
    @Column(name = "file_import_status")
    private FileImportStatusEnum status;

    @Column(name = "file_import_error")
    private String error;
}
