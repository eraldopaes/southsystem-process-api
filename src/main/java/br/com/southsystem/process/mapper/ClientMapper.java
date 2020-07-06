package br.com.southsystem.process.mapper;

import br.com.southsystem.process.domain.Client;
import org.springframework.stereotype.Component;

@Component
public class ClientMapper {

    public static final String TOKENIZER = "ç";

    public Client lineToClient(String line) {
        String[] lineSplited = line.split(TOKENIZER);
        return new Client(lineSplited[1], lineSplited[2], lineSplited[3]);
    }
}
