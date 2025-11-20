// Recomendacao.java
package com.redemaissocial.recomendacao;

import java.time.LocalDateTime;

public class Recomendacao {
    private Long id;
    private String candidatoId;
    private String vagaId;
    private double scoreCompatibilidade; // 0.0 a 100.0
    private LocalDateTime dataGeracao = LocalDateTime.now();
    private String motivo;

    public Recomendacao(String candidatoId, String vagaId, double score, String motivo) {
        this.candidatoId = candidatoId;
        this.vagaId = vagaId;
        this.scoreCompatibilidade = score;
        this.motivo = motivo;
    }

    @Override
    public String toString() {
        return String.format("Recomendação: Candidato %s → Vaga %s | Score: %.1f%% | %s",
                candidatoId, vagaId, scoreCompatibilidade, motivo);
    }
}