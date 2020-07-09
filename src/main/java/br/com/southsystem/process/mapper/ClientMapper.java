package br.com.southsystem.process.mapper;

import br.com.southsystem.process.domain.Client;
import br.com.southsystem.process.exceptionhandler.FileProcessException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static br.com.southsystem.process.utils.Constants.*;

@Component
public class ClientMapper implements LineMapper<Client> {

    public static final String TOKENIZER = "ç";
    private String line = "";

    @Override
    public Client lineToEntity(String line) {
        this.line = line;
        String[] lineSplited = line.split(TOKENIZER);
        validate(lineSplited);
        return new Client(lineSplited[1], lineSplited[2], lineSplited[3]);
    }

    @Override
    public void validate(String[] lineSplited) {
        validateSize(lineSplited, 4, this.line);
        validateCnpj(lineSplited[1]);
        validateName(lineSplited[2]);
        validateArea(lineSplited[3]);
    }

    private void validateArea(String area) {
        if (StringUtils.isEmpty(area.trim()))
            throw new FileProcessException(AREA_IS_REQUIRED, this.line);
    }


    private void validateName(String name) {
        if (StringUtils.isEmpty(name.trim()))
            throw new FileProcessException(NAME_IS_REQUIRED, this.line);
    }

    private void validateCnpj(String cnpj) {
        Pattern pattern = Pattern.compile("[0-9]{16}");
        Matcher matcher = pattern.matcher(cnpj);
        if (!matcher.matches())
            throw new FileProcessException(INVALID_CNPJ, this.line);
    }
}
