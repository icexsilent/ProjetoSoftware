// SolicitacaoAjuda.java
package com.redemaissocial.solicitacao;

import java.time.LocalDateTime;

public class SolicitacaoAjuda {
    private Long id;
    private String beneficiarioId;
    private String titulo;
    private String descricao;
    private String urgencia; // BAIXA, MEDIA, ALTA, URGENTE
    private LocalDateTime dataSolicitacao = LocalDateTime.now();
    private String status = "ABERTA"; // ABERTA, EM_ANDAMENTO, CONCLUIDA

    public SolicitacaoAjuda(String beneficiarioId, String titulo, String descricao, String urgencia) {
        this.beneficiarioId = beneficiarioId;
        this.titulo = titulo;
        this.descricao = descricao;
        this.urgencia = urgencia;
    }

    public void atender() { this.status = "EM_ANDAMENTO"; }
    public void concluir() { this.status = "CONCLUIDA"; }

    @Override
    public String toString() {
        return String.format("[%s] %s - %s (por %s)", urgencia, titulo, descricao, beneficiarioId);
    }
}