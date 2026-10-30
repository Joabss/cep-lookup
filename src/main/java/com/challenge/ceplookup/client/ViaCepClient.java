package com.challenge.ceplookup.client;

import com.challenge.ceplookup.dto.CepResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

/**
 * Cliente HTTP que chama a API externa de CEP (WireMock em dev, ViaCEP em prod).
 *
 * Java 21 / SOLID:
 *  - SRP: responsável apenas pela chamada HTTP; sem lógica de negócio.
 *  - OCP: URL base externalizada em application.yml — troca de endpoint sem alterar código.
 *  - LSP: implementa CepClient e é substituível por qualquer outro cliente.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ViaCepClient implements CepClient {

    private final RestTemplate restTemplate;

    @Value("${cep.api.base-url}")
    private String baseUrl;

    @Override
    public CepResponseDTO buscarCep(String cep) {
        var url = "%s/%s/json/".formatted(baseUrl, cep);
        log.info("Chamando API externa de CEP: {}", url);

        try {
            var response = restTemplate.getForObject(url, CepResponseDTO.class);

            if (response == null) {
                log.warn("API retornou resposta nula para CEP {}", cep);
                return CepResponseDTO.notFound(cep);
            }

            // Java 21: usa o método semântico do record ao invés de checar campo diretamente
            log.debug("Resposta da API para CEP {}: encontrado={}", cep, response.found());
            return response;

        } catch (HttpClientErrorException.NotFound e) {
            log.warn("CEP {} não encontrado na API externa", cep);
            return CepResponseDTO.notFound(cep);
        } catch (Exception e) {
            log.error("Erro ao consultar API de CEP para {}: {}", cep, e.getMessage());
            throw new RuntimeException("Falha ao consultar API de CEP: " + e.getMessage(), e);
        }
    }
}
