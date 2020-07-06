package br.com.southsystem.process.storage;

import br.com.southsystem.process.domain.enums.BucketTypeEnum;

import java.io.File;
import java.io.InputStream;

public interface Storage {

    String save(File file, BucketTypeEnum bucketTypeEnum);

    InputStream recover(String filename, BucketTypeEnum bucketTypeEnum);
}
