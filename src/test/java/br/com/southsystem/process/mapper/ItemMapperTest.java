package br.com.southsystem.process.mapper;

import br.com.southsystem.process.MockitoExtension;
import br.com.southsystem.process.domain.Item;
import br.com.southsystem.process.exceptionhandler.FileProcessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static br.com.southsystem.process.utils.Constants.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class ItemMapperTest {

    @InjectMocks
    private ItemMapper itemMapper;

    @Nested
    @DisplayName("Deve testar mapeamento de itens")
    class ItemMapperFullTest {

        @Test
        @DisplayName("Deve mapear os itens com sucesso")
        public void mappingItemWithSucess() {

            String itemLine = "[1-34-10,2-35-11]";

            List<Item> items = itemMapper.lineToEntity(itemLine);

            assertEquals(generateListOfItems(), items);
        }

        private List<Item> generateListOfItems() {
            List<Item> items = new ArrayList<>();
            items.add(new Item(1, 34, BigDecimal.valueOf(10)));
            items.add(new Item(2, 35, BigDecimal.valueOf(11)));
            return items;
        }

        @Test
        @DisplayName("Deve lançar exception de id do item inválido")
        public void mappingItemWithError_itemIdInvalid() {

            String itemLine = "[a-34-10]";

            FileProcessException fileProcessException = assertThrows(FileProcessException.class,
                    () -> itemMapper.lineToEntity(itemLine));

            assertEquals(itemLine, fileProcessException.getLine());
            assertEquals(INVALID_ID_ITEM, fileProcessException.getMessage());
        }

        @Test
        @DisplayName("Deve lançar exception de quantidade do item inválido")
        public void mappingItemWithError_quantityInvalid() {

            String itemLine = "[1-a-10]";

            FileProcessException fileProcessException = assertThrows(FileProcessException.class,
                    () -> itemMapper.lineToEntity(itemLine));

            assertEquals(itemLine, fileProcessException.getLine());
            assertEquals(INVALID_QUANTITY, fileProcessException.getMessage());
        }

        @Test
        @DisplayName("Deve lançar exception de preço do item inválido")
        public void mappingItemWithError_priceInvalid() {

            String itemLine = "[1-5-a]";

            FileProcessException fileProcessException = assertThrows(FileProcessException.class,
                    () -> itemMapper.lineToEntity(itemLine));

            assertEquals(itemLine, fileProcessException.getLine());
            assertEquals(INVALID_PRICE, fileProcessException.getMessage());
        }

        @Test
        @DisplayName("Deve lançar exception de layout")
        public void mappingItemWithError_layout() {

            String itemLine = "[1-34-10-5]";

            FileProcessException fileProcessException = assertThrows(FileProcessException.class,
                    () -> itemMapper.lineToEntity(itemLine));

            assertEquals(itemLine, fileProcessException.getLine());
            assertEquals(INVALID_LAYOUT, fileProcessException.getMessage());
        }
    }
}
