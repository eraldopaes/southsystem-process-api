package br.com.southsystem.process.domain.enums;

public enum BucketTypeEnum {

    FILE_INPUT("file-input"),
    FILE_OUTPUT("file-output");

    private final String name;

    BucketTypeEnum(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
