package br.com.southsystem.process.storage;

import br.com.southsystem.process.config.property.S3Property;
import br.com.southsystem.process.domain.enums.BucketTypeEnum;
import br.com.southsystem.process.exceptionhandler.BusinessException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;

@Component
public class S3Storage implements Storage {

    private static final Logger LOGGER = LoggerFactory.getLogger(S3Storage.class);

    private final AmazonS3 amazonS3;
    private final S3Property s3Property;

    @Autowired
    public S3Storage(AmazonS3 amazonS3, S3Property s3Property) {
        this.amazonS3 = amazonS3;
        this.s3Property = s3Property;
    }

    @Override
    public String save(File file, BucketTypeEnum bucketTypeEnum) {

        try {
            AccessControlList acl = new AccessControlList();
            sendFile(file.getName(), file, acl, bucketTypeEnum);
            return file.getName();
        } catch (Exception e) {
            LOGGER.info("Ocorreu um erro ao salvar o arquivo no S3. Detalhes: {}", e.getMessage());
            throw new BusinessException("Erro ao salvar arquivo");
        }

    }

    @Override
    public InputStream recover(String filename, BucketTypeEnum bucketTypeEnum) {
        try {
            return amazonS3.getObject(s3Property.getBucketName() + "/" + bucketTypeEnum.getName(), filename).getObjectContent();
        } catch (Exception e) {
            LOGGER.error("Não foi possível recuperar o arquivo no S3", e);
            throw new BusinessException("Não foi possível recuperar o arquivo");
        }
    }

    private ObjectMetadata sendFile(String filename, File file, AccessControlList acl, BucketTypeEnum bucketTypeEnum) throws FileNotFoundException {

        String path;

        if (bucketTypeEnum == BucketTypeEnum.FILE_OUTPUT) {
            path = BucketTypeEnum.FILE_OUTPUT.getName() + "/" + filename;
            LOGGER.info("Path para imagem: " + path);

            ObjectMetadata metadata = new ObjectMetadata();
            PutObjectRequest putObjectRequest = new PutObjectRequest(s3Property.getBucketName(), path, new FileInputStream(file), metadata);
            putObjectRequest.withAccessControlList(acl);
            amazonS3.putObject(putObjectRequest);
            return metadata;
        }

        throw new BusinessException("Erro ao enviar arquivo para o S3");
    }
}
