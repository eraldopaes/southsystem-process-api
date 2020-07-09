package br.com.southsystem.process.mapper;

import br.com.southsystem.process.MockitoExtension;
import br.com.southsystem.process.domain.Client;
import br.com.southsystem.process.exceptionhandler.FileProcessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;

import static br.com.southsystem.process.utils.Constants.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class ClientMapperTest {

    @InjectMocks
    private ClientMapper clientMapper;

    @Nested
    @DisplayName("Deve testar mapeamento de clientes")
    class ClientMapperFullTest {

        @Test
        @DisplayName("Deve mapear o cliente com sucesso")
        public void mappingClientWithSucess() {

            String clientLine = "002ç2345675434544345çJose da SilvaçRural";

            Client client = clientMapper.lineToEntity(clientLine);

            assertEquals("2345675434544345", client.getCnpj());
            assertEquals("Jose da Silva", client.getName());
            assertEquals("Rural", client.getArea());
        }

        @Test
        @DisplayName("Deve lançar exception de CNPJ inválido")
        public void mappingClientWithError_cnpjInvalid() {

            String clientLine = "002ç2345675435çJose da SilvaçRural";

            FileProcessException fileProcessException = assertThrows(FileProcessException.class,
                    () -> clientMapper.lineToEntity(clientLine));

            assertEquals(clientLine, fileProcessException.getLine());
            assertEquals(INVALID_CNPJ, fileProcessException.getMessage());
        }

        @Test
        @DisplayName("Deve lançar exception de nome inválido")
        public void mappingClientWithError_nameIsRequired() {

            String clientLine = "002ç2345675434544345ççRural";

            FileProcessException fileProcessException = assertThrows(FileProcessException.class,
                    () -> clientMapper.lineToEntity(clientLine));

            assertEquals(clientLine, fileProcessException.getLine());
            assertEquals(NAME_IS_REQUIRED, fileProcessException.getMessage());
        }

        @Test
        @DisplayName("Deve lançar exception de área inválido")
        public void mappingClientWithError_areaIsRequired() {

            String clientLine = "002ç2345675434544345çEraldoç ";

            FileProcessException fileProcessException = assertThrows(FileProcessException.class,
                    () -> clientMapper.lineToEntity(clientLine));

            assertEquals(clientLine, fileProcessException.getLine());
            assertEquals(AREA_IS_REQUIRED, fileProcessException.getMessage());
        }

        @Test
        @DisplayName("Deve lançar exception de layout")
        public void mappingClientWithError_layout() {

            String clientLine = "002ç2345675434544345çEraldoçRuralçOutro";

            FileProcessException fileProcessException = assertThrows(FileProcessException.class,
                    () -> clientMapper.lineToEntity(clientLine));

            assertEquals(clientLine, fileProcessException.getLine());
            assertEquals(INVALID_LAYOUT, fileProcessException.getMessage());
        }
    }

}