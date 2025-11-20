// AvaliacaoCampanha.java
package com.redemaissocial.avaliacao;

import java.time.LocalDateTime;

public class AvaliacaoCampanha {
    private Long id;
    private int nota;
    private String comentario;
    private LocalDateTime dataAvaliacao = LocalDateTime.now();
    private String campanhaId;
    private String autorId;

    public AvaliacaoCampanha(int nota, String comentario, String campanhaId, String autorId) {
        this.nota = nota;
        this.comentario = comentario;
        this.campanhaId = campanhaId;
        this.autorId = autorId;
    }

    @Override
    public String toString() {
        return String.format("Campanha avaliada: %d estrelas | \"%s\"", nota, comentario);
    }
}