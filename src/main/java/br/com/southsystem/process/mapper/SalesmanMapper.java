package br.com.southsystem.process.mapper;

import br.com.southsystem.process.domain.Salesman;
import br.com.southsystem.process.exceptionhandler.FileProcessException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static br.com.southsystem.process.utils.Constants.*;

@Component
public class SalesmanMapper implements LineMapper<Salesman> {

    public static final String TOKENIZER = "ç";
    private String line = "";

    @Override
    public Salesman lineToEntity(String line) {
        this.line = line;
        String[] lineSplited = line.split(TOKENIZER);
        validate(lineSplited);
        return new Salesman(lineSplited[1], lineSplited[2], new BigDecimal(lineSplited[3]));
    }

    @Override
    public void validate(String[] lineSplited) {
        validateSize(lineSplited, 4, this.line);
        validateCpf(lineSplited[1]);
        validateName(lineSplited[2]);
        validateSalary(lineSplited[3]);
    }

    private void validateSalary(String salary) {
        try {
            Double.parseDouble(salary);
        } catch (Exception e) {
            throw new FileProcessException(INVALID_SALARY, this.line);
        }
    }

    private void validateName(String name) {
        if (StringUtils.isEmpty(name.trim()))
            throw new FileProcessException(NAME_IS_REQUIRED, this.line);
    }

    private void validateCpf(String cpf) {
        Pattern pattern = Pattern.compile("[0-9]{13}");
        Matcher matcher = pattern.matcher(cpf);
        if (!matcher.matches())
            throw new FileProcessException(INVALID_CPF, this.line);
    }
}
