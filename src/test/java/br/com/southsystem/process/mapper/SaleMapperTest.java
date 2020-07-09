package br.com.southsystem.process.mapper;

import br.com.southsystem.process.MockitoExtension;
import br.com.southsystem.process.domain.Item;
import br.com.southsystem.process.domain.Sale;
import br.com.southsystem.process.exceptionhandler.FileProcessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static br.com.southsystem.process.utils.Constants.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SaleMapperTest {

    @InjectMocks
    private SaleMapper saleMapper;
    @Mock
    private LineMapper<List<Item>> lineMapper;

    @Nested
    @DisplayName("Deve testar mapeamento de vendas")
    class SaleMapperFullTest {

        @Test
        @DisplayName("Deve mapear a venda com sucesso")
        public void mappingSaleWithSucess() {

            String saleLine = "003ç08ç[1-34-10,2-35-11]çPaulo";

            when(lineMapper.lineToEntity("[1-34-10,2-35-11]")).thenReturn(generateListOfItems());

            Sale sale = saleMapper.lineToEntity(saleLine);

            assertEquals("08", sale.getId());
            assertEquals("Paulo", sale.getSalesmanName());
            assertEquals(generateListOfItems(), sale.getItems());
        }

        private List<Item> generateListOfItems() {
            List<Item> items = new ArrayList<>();
            items.add(new Item(1, 34, BigDecimal.valueOf(10)));
            items.add(new Item(2, 35, BigDecimal.valueOf(11)));
            return items;
        }

        @Test
        @DisplayName("Deve lançar exception por não ter sido passado um array válido de vendas")
        public void mappingSaleWithError_saleStartInvalid() {

            String saleLine = "003ç08ç1-34-10,2-35-11]çPaulo";

            FileProcessException fileProcessException = assertThrows(FileProcessException.class,
                    () -> saleMapper.lineToEntity(saleLine));

            assertEquals(saleLine, fileProcessException.getLine());
            assertEquals(INVALID_ARRAY, fileProcessException.getMessage());
        }

        @Test
        @DisplayName("Deve lançar exception por não ter sido passado um array válido de vendas")
        public void mappingSaleWithError_saleEndInvalid() {

            String saleLine = "003ç08ç[1-34-10,2-35-11çPaulo";

            FileProcessException fileProcessException = assertThrows(FileProcessException.class,
                    () -> saleMapper.lineToEntity(saleLine));

            assertEquals(saleLine, fileProcessException.getLine());
            assertEquals(INVALID_ARRAY, fileProcessException.getMessage());
        }

        @Test
        @DisplayName("Deve lançar exception de id da venda inválida")
        public void mappingSaleWithError_saleIdIsRequired() {

            String saleLine = "003çç[1-34-10,2-33-1.50,3-40-0.10]çPaulo";

            FileProcessException fileProcessException = assertThrows(FileProcessException.class,
                    () -> saleMapper.lineToEntity(saleLine));

            assertEquals(saleLine, fileProcessException.getLine());
            assertEquals(INVALID_ID_SALE, fileProcessException.getMessage());
        }

        @Test
        @DisplayName("Deve lançar exception de nome inválido")
        public void mappingSaleWithError_nameEmpty() {

            String saleLine = "003ç08ç[1-34-10,2-33-1.50,3-40-0.10]ç ";

            FileProcessException fileProcessException = assertThrows(FileProcessException.class,
                    () -> saleMapper.lineToEntity(saleLine));

            assertEquals(saleLine, fileProcessException.getLine());
            assertEquals(NAME_IS_REQUIRED, fileProcessException.getMessage());
        }

        @Test
        @DisplayName("Deve lançar exception de layout")
        public void mappingSaleWithError_layout() {

            String saleLine = "003ç08ç[1-34-10,2-33-1.50,3-40-0.10]";

            FileProcessException fileProcessException = assertThrows(FileProcessException.class,
                    () -> saleMapper.lineToEntity(saleLine));

            assertEquals(saleLine, fileProcessException.getLine());
            assertEquals(INVALID_LAYOUT, fileProcessException.getMessage());
        }
    }
}