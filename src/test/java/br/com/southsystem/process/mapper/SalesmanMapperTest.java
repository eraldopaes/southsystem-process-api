package br.com.southsystem.process.mapper;

import br.com.southsystem.process.domain.Salesman;
import br.com.southsystem.process.exceptionhandler.FileProcessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import br.com.southsystem.process.MockitoExtension;

import java.math.BigDecimal;

import static br.com.southsystem.process.utils.Constants.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class SalesmanMapperTest {

    @InjectMocks
    private SalesmanMapper salesmanMapper;

    @Nested
    @DisplayName("Deve testar mapeamento de vendedores")
    class SalesmanMapperFullTest {

        @Test
        @DisplayName("Deve mapear o vendedor com sucesso")
        public void mappingSalesmanWithSucess() {

            String salesmanLine = "001ç1234567891234çPedroç50000";

            Salesman salesman = salesmanMapper.lineToEntity(salesmanLine);

            assertEquals("1234567891234", salesman.getCpf());
            assertEquals("Pedro", salesman.getName());
            assertEquals(BigDecimal.valueOf(50000), salesman.getSalary());
        }

        @Test
        @DisplayName("Deve lançar exception de CPF inválido")
        public void mappingSalesmanWithError_cpfInvalid() {

            String salesmanLine = "001ç12345678912çPedroç50000";

            FileProcessException fileProcessException = assertThrows(FileProcessException.class,
                    () -> salesmanMapper.lineToEntity(salesmanLine));

            assertEquals(salesmanLine, fileProcessException.getLine());
            assertEquals(INVALID_CPF, fileProcessException.getMessage());
        }

        @Test
        @DisplayName("Deve lançar exception de nome inválido")
        public void mappingSalesmanWithError_nameIsRequired() {

            String salesmanLine = "001ç1234567891234çç50000";

            FileProcessException fileProcessException = assertThrows(FileProcessException.class,
                    () -> salesmanMapper.lineToEntity(salesmanLine));

            assertEquals(salesmanLine, fileProcessException.getLine());
            assertEquals(NAME_IS_REQUIRED, fileProcessException.getMessage());
        }

        @Test
        @DisplayName("Deve lançar exception de salário inválido")
        public void mappingSalesmanWithError_salaryEmpty() {

            String salesmanLine = "001ç1234567891234çEraldoç ";

            FileProcessException fileProcessException = assertThrows(FileProcessException.class,
                    () -> salesmanMapper.lineToEntity(salesmanLine));

            assertEquals(salesmanLine, fileProcessException.getLine());
            assertEquals(INVALID_SALARY, fileProcessException.getMessage());
        }

        @Test
        @DisplayName("Deve lançar exception de salário inválido")
        public void mappingSalesmanWithError_salaryInvalid() {

            String salesmanLine = "001ç1234567891234çEraldoç50,2";

            FileProcessException fileProcessException = assertThrows(FileProcessException.class,
                    () -> salesmanMapper.lineToEntity(salesmanLine));

            assertEquals(salesmanLine, fileProcessException.getLine());
            assertEquals(INVALID_SALARY, fileProcessException.getMessage());
        }

        @Test
        @DisplayName("Deve lançar exception de layout")
        public void mappingSalesmanWithError_layout() {

            String salesmanLine = "002ç2345675434544345çEraldoçRuralçOutro";

            FileProcessException fileProcessException = assertThrows(FileProcessException.class,
                    () -> salesmanMapper.lineToEntity(salesmanLine));

            assertEquals(salesmanLine, fileProcessException.getLine());
            assertEquals(INVALID_LAYOUT, fileProcessException.getMessage());
        }
    }
}