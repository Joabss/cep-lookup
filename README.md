# CEP Lookup

API REST em Java 21 + Spring Boot 3 para consultar CEPs, validar o formato informado e registrar cada consulta em banco PostgreSQL com histórico de pesquisa.

## Funcionalidades

- Busca de endereço por CEP usando API externa de CEP
- Suporte ao ambiente de desenvolvimento com WireMock para simular o serviço externo
- Validação de entrada: o CEP deve conter exatamente 8 dígitos numéricos
- Persistência de logs em banco PostgreSQL (`cep_logs`)
- Consulta do histórico de buscas por CEP
- Tratamento de erros com respostas HTTP apropriadas (`400`, `404` e `500`)
- Testes de integração com WireMock + H2

## Tecnologias

- Java 21
- Spring Boot 3.3.4
- Maven
- PostgreSQL
- Docker / Docker Compose
- WireMock
- Spring Data JPA

## Como iniciar

### Pré-requisitos

- Java 21+
- Maven 3.8+
- Docker e Docker Compose

### 1) Subir os serviços de apoio

```bash
docker compose up -d
```

Isso inicia:

- PostgreSQL em `localhost:5432`
  - banco: `cepdb`
  - usuário: `cepuser`
  - senha: `ceppass`
- WireMock em `localhost:8089`

### 2) Executar a aplicação

```bash
mvn spring-boot:run
```

A aplicação fica disponível em:

- API: `http://localhost:8080`
- Health check: `http://localhost:8080/actuator/health`

## Configuração

As configurações principais estão em `src/main/resources/application.yml`.

Exemplo:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/cepdb
    username: cepuser
    password: ceppass

cep:
  api:
    base-url: http://localhost:8089/ws
```

Para usar a API real do ViaCEP, altere o valor para:

```yaml
cep:
  api:
    base-url: https://viacep.com.br/ws
```

## Endpoints

### Buscar CEP

```http
GET /api/v1/cep/{cep}
```

Exemplo:

```bash
curl http://localhost:8080/api/v1/cep/01001000
```

Resposta esperada (exemplo):

```json
{
  "cep": "01001-000",
  "logradouro": "Praça da Sé",
  "complemento": "lado ímpar",
  "bairro": "Sé",
  "localidade": "São Paulo",
  "uf": "SP",
  "ibge": "3550308",
  "ddd": "11"
}
```

### Histórico de consultas

```http
GET /api/v1/cep/{cep}/historico
```

Exemplo:

```bash
curl http://localhost:8080/api/v1/cep/01001000/historico
```

## Exemplos com curl

### Buscar um CEP válido

```bash
curl -i http://localhost:8080/api/v1/cep/01001000
```

### Buscar CEP inexistente

```bash
curl -i http://localhost:8080/api/v1/cep/00000000
```

### CEP com formato inválido

```bash
curl -i http://localhost:8080/api/v1/cep/INVALIDO
```

### Ver histórico de consultas

```bash
curl -i http://localhost:8080/api/v1/cep/01001000/historico
```

### Exemplo com retorno em JSON formatado

```bash
curl -s http://localhost:8080/api/v1/cep/01001000 | jq
```

Se o `jq` não estiver instalado, basta usar:

```bash
curl -s http://localhost:8080/api/v1/cep/01001000
```

## Resumo de arquitetura em diagramas

### Fluxo principal

```text
Cliente HTTP
    |
    v
GET /api/v1/cep/{cep}
    |
    v
CepController
    |
    v
CepServiceImpl
    |\
    | \__ CepClient (ViaCepClient)
    |          |
    |          v
    |     API externa / WireMock
    |
    \__ CepLogServiceImpl
               |
               v
          PostgreSQL
             cep_logs
```

### Sequência de execução

```text
Cliente -> CepController: requisita CEP
CepController -> CepServiceImpl: buscarCep(cep)
CepServiceImpl -> CepClient: buscarCep(cep)
CepClient -> APIExterna: GET /ws/{cep}/json
APIExterna --> CepClient: payload de endereço
CepClient --> CepServiceImpl: CepResponseDTO
CepServiceImpl -> CepLogServiceImpl: registrar(cep, response)
CepLogServiceImpl -> PostgreSQL: INSERT cep_logs
CepServiceImpl --> CepController: retorno 200 ou exceção 404
CepController --> Cliente: JSON da resposta
```

## CEPs de teste

O WireMock já vem com alguns CEPs configurados:

- `01001000` → Praça da Sé / São Paulo - SP
- `22041011` → Avenida Atlântica / Rio de Janeiro - RJ
- `00000000` → simula CEP não encontrado

## Banco de dados

A aplicação cria/atualiza o schema automaticamente com JPA (`ddl-auto: update`).
A tabela principal é `cep_logs`, e guarda:

- CEP consultado
- data e hora da consulta
- logradouro, bairro, cidade, UF
- status da busca (`SUCCESS` ou `NOT_FOUND`)
- payload JSON completo retornado pela API externa

## Testes

Para executar os testes do projeto:

```bash
mvn test
```

## Estrutura do projeto

```text
.
├── docker/
│   └── wiremock/
│       └── mappings/
│           └── cep-mappings.json
├── src/
│   ├── main/
│   │   ├── java/com/challenge/ceplookup/
│   │   │   ├── client/
│   │   │   ├── config/
│   │   │   ├── controller/
│   │   │   ├── dto/
│   │   │   ├── exception/
│   │   │   ├── model/
│   │   │   ├── repository/
│   │   │   └── service/
│   │   └── resources/
│   │       └── application.yml
│   └── test/java/com/challenge/ceplookup/
├── docker-compose.yml
├── pom.xml
├── .gitignore
└── README.md
```

## Observações

- O projeto usa a camada externa de CEP em modo mock por padrão para facilitar o desenvolvimento local.
- Em produção, basta trocar a URL base para a API real do ViaCEP.
- O formato do CEP é validado na camada HTTP com `@Pattern(regexp = "\\d{8}")`.
