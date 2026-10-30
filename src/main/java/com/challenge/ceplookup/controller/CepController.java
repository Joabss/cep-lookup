package com.challenge.ceplookup.controller;

import com.challenge.ceplookup.dto.CepResponseDTO;
import com.challenge.ceplookup.model.CepLog;
import com.challenge.ceplookup.service.CepService;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller para busca de CEP.
 *
 * Spring Boot 3 / Java 21: usa jakarta.validation (não mais javax.validation).
 * SOLID — SRP: apenas mapeia HTTP request/response. DIP: depende de CepService (interface).
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/api/v1/cep")
@RequiredArgsConstructor
public class CepController {

    private final CepService cepService;

    /**
     * GET /api/v1/cep/{cep}
     * Busca dados de endereço para o CEP informado.
     *
     * @param cep 8 dígitos numéricos (ex: 01001000)
     * @return 200 com dados do endereço | 404 se não encontrado | 400 se formato inválido
     */
    @GetMapping("/{cep}")
    public ResponseEntity<CepResponseDTO> buscarCep(
            @PathVariable
            @Pattern(regexp = "\\d{8}", message = "CEP deve conter exatamente 8 dígitos numéricos")
            String cep) {

        log.info("Requisição recebida para CEP: {}", cep);
        CepResponseDTO response = cepService.buscarCep(cep);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/cep/{cep}/historico
     * Retorna todas as consultas anteriores para o CEP, ordenadas pela mais recente.
     */
    @GetMapping("/{cep}/historico")
    public ResponseEntity<List<CepLog>> buscarHistorico(
            @PathVariable
            @Pattern(regexp = "\\d{8}", message = "CEP deve conter exatamente 8 dígitos numéricos")
            String cep) {

        List<CepLog> historico = cepService.buscarHistorico(cep);
        return ResponseEntity.ok(historico);
    }
}
