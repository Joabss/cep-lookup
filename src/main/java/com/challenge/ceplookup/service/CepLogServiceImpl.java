package com.challenge.ceplookup.service;

import com.challenge.ceplookup.dto.CepResponseDTO;
import com.challenge.ceplookup.model.CepLog;
import com.challenge.ceplookup.repository.CepLogRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementa o registro de log para cada consulta de CEP.
 *
 * Java 21: usa var para inferência de tipo local e acessa campos do record via métodos acessores.
 * SOLID — SRP: apenas persiste logs. DIP: depende de CepLogRepository (interface JPA).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CepLogServiceImpl implements CepLogService {

    private final CepLogRepository repository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public CepLog registrar(String cep, CepResponseDTO response) {
        var payload = toJson(response);

        // Java 21: acessa campo do record via método acessor (response.found())
        var status = response.found() ? "SUCCESS" : "NOT_FOUND";

        var logEntry = CepLog.builder()
                .cepConsultado(cep)
                .consultadoEm(LocalDateTime.now())
                .logradouro(response.logradouro())
                .complemento(response.complemento())
                .bairro(response.bairro())
                .localidade(response.localidade())
                .uf(response.uf())
                .ibge(response.ibge())
                .ddd(response.ddd())
                .statusConsulta(status)
                .payloadRetorno(payload)
                .build();

        var saved = repository.save(logEntry);
        log.info("Log persistido para CEP {} com status {} [id={}]", cep, saved.getStatusConsulta(), saved.getId());
        return saved;
    }

    @Override
    public List<CepLog> buscarHistorico(String cep) {
        return repository.findHistoricoByCep(cep);
    }

    private String toJson(CepResponseDTO dto) {
        try {
            return objectMapper.writeValueAsString(dto);
        } catch (JsonProcessingException e) {
            log.warn("Não foi possível serializar o response para JSON: {}", e.getMessage());
            return "{}";
        }
    }
}
