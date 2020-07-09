# southsystem-process-api

API responsável por processar os arquivos

Passos do processamento:
 - A API recebe via mensageria os ID's dos arquivos a serem processados, busca metadados no mysql e baixa o arquivo no S3
 - É realizada a leitura desse arquivo
 - Validações são feitas. Essas validações foram feitas da forma mais simplista possível, e baseados nos dados passado de exemplo (por exemplo não existe validação se um CPF é real ou não, apenas verifica se é do tamanho dos que passados nos exemplos)
 - Por simplicidade, qualquer erro no arquivo interrompe seu processamento. É possível saber a linha do erro e uma mensagem amigável, visitando o endpoint de consulta de status de um arquivo na API de importação
 - Se o arquivo for processado sem erros, será gerado um relatório que será enviado para o S3 para consulta posterior, o que pode ser feito também na API de importação
 
Pontos de melhorias:
 - Cobertura maior de testes
 - É possível melhorar as validações
 - É possível melhorar o tratamento de erros, para que não se interrompa o processamento do arquivo por causa de uma linha com layout errado. Com isso poderíamos adicionar por exemplo os status PROCESSADO COMPLETO e PROCESSADO PARCIALMENTE

 
