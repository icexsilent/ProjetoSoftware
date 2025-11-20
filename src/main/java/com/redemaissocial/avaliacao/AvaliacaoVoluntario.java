// AvaliacaoVoluntario.java
package com.redemaissocial.avaliacao;

import java.time.LocalDateTime;

public class AvaliacaoVoluntario {
    private Long id;
    private int nota; // 1 a 5
    private String comentario;
    private LocalDateTime dataAvaliacao = LocalDateTime.now();
    private String voluntarioId;
    private String campanhaId;

    public AvaliacaoVoluntario(int nota, String comentario, String voluntarioId, String campanhaId) {
        this.nota = nota;
        this.comentario = comentario;
        this.voluntarioId = voluntarioId;
        this.campanhaId = campanhaId;
    }

    @Override
    public String toString() {
        return String.format("Avaliação: %d estrelas | \"%s\" | Voluntário: %s", nota, comentario, voluntarioId);
    }
}