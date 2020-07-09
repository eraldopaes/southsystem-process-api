package br.com.southsystem.process.mapper;

import br.com.southsystem.process.exceptionhandler.FileProcessException;
import br.com.southsystem.process.utils.Constants;

public interface LineMapper<T> {

    T lineToEntity(String line);

    void validate(String[] lineSplited);

    default void validateSize(String[] lineSplited, int realSize, String line) {
        if (lineSplited.length != realSize) {
            throw new FileProcessException(Constants.INVALID_LAYOUT, line);
        }
    }
}
