package br.com.southsystem.process.mapper;

import br.com.southsystem.process.domain.Sale;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SaleMapper {

    public static final String TOKENIZER = "ç";

    private final ItemMapper itemMapper;

    @Autowired
    public SaleMapper(ItemMapper itemMapper) {
        this.itemMapper = itemMapper;
    }

    public Sale lineToSale(String line) {
        String[] lineSplited = line.split(TOKENIZER);
        return new Sale(lineSplited[1], lineSplited[3], itemMapper.lineToListOfItems(lineSplited[2]));
    }
}
