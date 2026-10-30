package com.challenge.ceplookup;

import com.challenge.ceplookup.dto.CepResponseDTO;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
        "cep.api.base-url=http://localhost:9090/ws",
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=sa",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class CepLookupIntegrationTest {

    private static WireMockServer wireMockServer;

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @BeforeAll
    static void startWireMock() {
        wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().port(9090));
        wireMockServer.start();
        configureFor("localhost", 9090);
    }

    @AfterAll
    static void stopWireMock() {
        wireMockServer.stop();
    }

    @BeforeEach
    void resetMappings() {
        wireMockServer.resetAll();
    }

    @Test
    @DisplayName("Deve retornar dados de endereço para CEP válido")
    void shouldReturnAddressForValidCep() {
        var body = """
                {
                  "cep": "01001-000",
                  "logradouro": "Praça da Sé",
                  "bairro": "Sé",
                  "localidade": "São Paulo",
                  "uf": "SP",
                  "ddd": "11"
                }
                """;

        stubFor(get(urlMatching("/ws/01001000/json/?"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(body)));

        ResponseEntity<CepResponseDTO> response = restTemplate
                .getForEntity("http://localhost:" + port + "/api/v1/cep/01001000", CepResponseDTO.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().localidade()).isEqualTo("São Paulo");
        assertThat(response.getBody().uf()).isEqualTo("SP");
        assertThat(response.getBody().found()).isTrue();
    }

    @Test
    @DisplayName("Deve retornar 404 para CEP não encontrado")
    void shouldReturn404ForNotFoundCep() {
        stubFor(get(urlMatching("/ws/00000000/json/?"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {"erro": true}
                                """)));

        ResponseEntity<String> response = restTemplate
                .getForEntity("http://localhost:" + port + "/api/v1/cep/00000000", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("Deve retornar 400 para formato de CEP inválido")
    void shouldReturn400ForInvalidCepFormat() {
        ResponseEntity<String> response = restTemplate
                .getForEntity("http://localhost:" + port + "/api/v1/cep/INVALIDO", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
