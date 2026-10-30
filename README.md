# 📮 CEP Lookup — Desafio Técnico

API REST em **Java 21** + Spring Boot 3.3 para busca de CEP com log persistido em banco de dados.

---

## ☕ Recursos Java 21 utilizados

| Recurso | Onde | Benefício |
|---------|------|-----------|
| **Records** | `CepResponseDTO`, `ErrorResponse` | Imutabilidade, zero boilerplate de getters/equals/hashCode |
| **Text Blocks** | Testes de integração | JSON legível sem escape manual (`\"`) |
| **`var` (inferência local)** | Services e clients | Código mais conciso sem perda de tipagem |
| **Switch expression com pattern matching** | `GlobalExceptionHandler` | Mapeamento de exceções sem if-else encadeado |
| **Métodos semânticos em records** | `response.found()` | API expressiva e legível no domínio |
| **`String.formatted()`** | `ViaCepClient` | Interpolação limpa sem concatenação |

---

## 🏗️ Arquitetura

```
Cliente HTTP
    │
    ▼ GET /api/v1/cep/{cep}
CepController          ← recebe a requisição, valida formato
    │
    ▼
CepService (interface)
    │
    ├─► CepClient (interface)
    │       └─► ViaCepClient → WireMock (mock) / ViaCEP (prod)
    │
    └─► CepLogService (interface)
            └─► CepLogServiceImpl → PostgreSQL (cep_logs)
```

---

## 🧱 Princípios SOLID aplicados

| Princípio | Onde |
|-----------|------|
| **S** — Single Responsibility | `CepController` só trata HTTP. `CepServiceImpl` só orquestra. `CepLogServiceImpl` só persiste logs. |
| **O** — Open/Closed | `base-url` externalizado em `application.yml`; nova funcionalidade via wrapper, sem alterar classes existentes. |
| **L** — Liskov Substitution | `ViaCepClient` é substituível por qualquer outra implementação de `CepClient` (inclusive o mock de testes). |
| **I** — Interface Segregation | `CepService` e `CepLogService` são interfaces separadas; nenhum cliente precisa conhecer os métodos do outro. |
| **D** — Dependency Inversion | Controller depende de `CepService` (interface). Service depende de `CepClient` e `CepLogService` (interfaces). |

---

## 🚀 Como executar

### Pré-requisitos
- Java 21+
- Maven 3.8+
- Docker & Docker Compose

### 1. Subir os containers (PostgreSQL + WireMock)

```bash
docker-compose up -d
```

Isso sobe:
- **PostgreSQL** em `localhost:5432` — banco `cepdb`
- **WireMock** em `localhost:8089` — mock da API de CEP

### 2. Verificar WireMock

```bash
curl http://localhost:8089/ws/01001000/json/
```

### 3. Executar a aplicação

```bash
./mvnw spring-boot:run
```

A API estará disponível em `http://localhost:8080`.

---

## 📡 Endpoints

### Buscar CEP

```http
GET /api/v1/cep/{cep}
```

**Exemplo:**
```bash
curl http://localhost:8080/api/v1/cep/01001000
```

**Resposta 200:**
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

**CEPs de teste disponíveis no mock:**
| CEP | Endereço |
|-----|----------|
| `01001000` | Praça da Sé, São Paulo/SP |
| `22041011` | Av. Atlântica, Copacabana/RJ |
| `00000000` | Retorna `{"erro": true}` → 404 |

### Histórico de consultas

```http
GET /api/v1/cep/{cep}/historico
```

```bash
curl http://localhost:8080/api/v1/cep/01001000/historico
```

---

## 🗄️ Modelo do banco

Tabela `cep_logs`:

| Coluna | Tipo | Descrição |
|--------|------|-----------|
| `id` | BIGSERIAL PK | Chave primária |
| `cep_consultado` | VARCHAR(8) | CEP consultado |
| `consultado_em` | TIMESTAMP | Data/hora da consulta |
| `logradouro` | VARCHAR | Retorno da API |
| `bairro` | VARCHAR | Retorno da API |
| `localidade` | VARCHAR | Cidade |
| `uf` | CHAR(2) | Estado |
| `ddd` | VARCHAR(3) | Código de discagem |
| `status_consulta` | VARCHAR(20) | `SUCCESS` ou `NOT_FOUND` |
| `payload_retorno` | TEXT | JSON completo retornado |

---

## 🧪 Testes

```bash
./mvnw test
```

Os testes de integração usam:
- **WireMock 3 embarcado** (sem Docker necessário)
- **H2 in-memory** para o banco de dados

---

## 🐳 Docker: personalizar URL da API

Para apontar para a API real (ViaCEP) em vez do mock:

```yaml
# application.yml
cep:
  api:
    base-url: https://viacep.com.br/ws
```

---

## 📁 Estrutura do projeto

```
src/main/java/com/challenge/ceplookup/
├── CepLookupApplication.java
├── config/
│   └── AppConfig.java            # Beans: RestTemplate, ObjectMapper
├── controller/
│   └── CepController.java        # REST endpoints
├── service/
│   ├── CepService.java           # Interface (DIP)
│   ├── CepServiceImpl.java       # Orquestração
│   ├── CepLogService.java        # Interface (ISP)
│   └── CepLogServiceImpl.java    # Persistência de logs
├── client/
│   ├── CepClient.java            # Interface (DIP / LSP)
│   └── ViaCepClient.java         # Implementação HTTP
├── repository/
│   └── CepLogRepository.java     # Spring Data JPA
├── model/
│   └── CepLog.java               # Entidade JPA
├── dto/
│   └── CepResponseDTO.java       # Record Java 21 (imutável)
└── exception/
    ├── CepNotFoundException.java
    └── GlobalExceptionHandler.java  # ErrorResponse como record
```
