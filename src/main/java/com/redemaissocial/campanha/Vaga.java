package com.redemaissocial.campanha;

import com.redemaissocial.perfil.Habilidade;
import java.util.ArrayList;
import java.util.List;

public class Vaga {
    private String id;
    private String descricao;
    private int quantidade;
    private String localizacao;
    private final List<Habilidade> habilidadesRequeridas = new ArrayList<>();
    private final List<Candidatura> candidaturas = new ArrayList<>();

    public Vaga(String id, String descricao, int quantidade, String localizacao, int cargaHoraria) {
        this.id = id;
        this.descricao = descricao;
        this.quantidade = quantidade;
        this.localizacao = localizacao;
    }

    public String getDescricao() { return descricao; }
    public List<Candidatura> getCandidaturas() { return candidaturas; }
}