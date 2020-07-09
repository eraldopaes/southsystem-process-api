package br.com.southsystem.process.mapper;


import br.com.southsystem.process.domain.Item;
import br.com.southsystem.process.exceptionhandler.FileProcessException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static br.com.southsystem.process.utils.Constants.*;

@Component
public class ItemMapper implements LineMapper<List<Item>> {

    public static final String TOKENIZER_FOR_LIST_SALE = ",";
    public static final String TOKENIZER_FOR_ITEM = "-";
    private String line = "";

    @Override
    public List<Item> lineToEntity(String line) {
        this.line = line;
        List<Item> items = new ArrayList<>();

        String lineWithSales = line.replace("[", "").replace("]", "");
        String[] lineSplited = lineWithSales.split(TOKENIZER_FOR_LIST_SALE);

        for (String sale : lineSplited) {
            String[] item = sale.split(TOKENIZER_FOR_ITEM);
            validate(item);
            items.add(new Item(Integer.valueOf(item[0]), Integer.valueOf(item[1]), new BigDecimal(item[2])));
        }

        return items;
    }

    @Override
    public void validate(String[] lineSplited) {
        validateSize(lineSplited, 3, this.line);
        validateId(lineSplited[0]);
        validateQuantity(lineSplited[1]);
        validatePrice(lineSplited[2]);
    }

    private void validatePrice(String price) {
        try {
            Double.parseDouble(price);
        } catch (Exception e) {
            throw new FileProcessException(INVALID_PRICE, this.line);
        }
    }

    private void validateId(String id) {
        try {
            Integer.parseInt(id);
        } catch (Exception e) {
            throw new FileProcessException(INVALID_ID_ITEM, this.line);
        }
    }

    private void validateQuantity(String quantity) {
        try {
            Integer.parseInt(quantity);
        } catch (Exception e) {
            throw new FileProcessException(INVALID_QUANTITY, this.line);
        }
    }
}
