package br.com.southsystem.process.mapper;

import br.com.southsystem.process.domain.Item;
import br.com.southsystem.process.domain.Sale;
import br.com.southsystem.process.exceptionhandler.FileProcessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

import static br.com.southsystem.process.utils.Constants.*;

@Component
public class SaleMapper implements LineMapper<Sale> {

    public static final String TOKENIZER = "ç";

    private final LineMapper<List<Item>> lineMapper;
    private String line = "";

    @Autowired
    public SaleMapper(LineMapper<List<Item>> lineMapper) {
        this.lineMapper = lineMapper;
    }

    @Override
    public Sale lineToEntity(String line) {
        this.line = line;
        String[] lineSplited = line.split(TOKENIZER);
        validate(lineSplited);
        return new Sale(lineSplited[1], lineSplited[3], lineMapper.lineToEntity(lineSplited[2]));
    }

    @Override
    public void validate(String[] lineSplited) {
        validateSize(lineSplited, 4, this.line);
        validateSaleId(lineSplited[1]);
        validateSales(lineSplited[2]);
        validateName(lineSplited[3]);
    }

    private void validateSales(String sales) {
        if (!(sales.startsWith("[") && sales.endsWith("]"))) {
            throw new FileProcessException(INVALID_ARRAY, this.line);
        }
    }

    private void validateSaleId(String saleId) {
        if (StringUtils.isEmpty(saleId.trim()))
            throw new FileProcessException(INVALID_ID_SALE, this.line);
    }

    private void validateName(String name) {
        if (StringUtils.isEmpty(name.trim()))
            throw new FileProcessException(NAME_IS_REQUIRED, this.line);
    }
}
