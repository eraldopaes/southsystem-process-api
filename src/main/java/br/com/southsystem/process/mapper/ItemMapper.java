package br.com.southsystem.process.mapper;


import br.com.southsystem.process.domain.Item;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class ItemMapper {

    public static final String TOKENIZER_FOR_LIST_SALE = ",";
    public static final String TOKENIZER_FOR_ITEM = "-";

    public List<Item> lineToListOfItems(String line) {

        List<Item> items = new ArrayList<>();

        String lineWithSales = line.replace("[", "").replace("]", "");
        String[] sales = lineWithSales.split(TOKENIZER_FOR_LIST_SALE);

        for (String sale : sales) {
            String[] item = sale.split(TOKENIZER_FOR_ITEM);
            items.add(new Item(Integer.valueOf(item[0]), Integer.valueOf(item[1]), new BigDecimal(item[2])));
        }

        return items;
    }
}
