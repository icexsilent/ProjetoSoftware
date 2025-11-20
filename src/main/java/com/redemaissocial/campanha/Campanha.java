package com.redemaissocial.campanha;

import java.util.ArrayList;
import java.util.List;

public class Campanha {
    private String id;
    private String titulo;
    private String descricao;
    private final List<Vaga> vagas = new ArrayList<>();

    public Campanha(String id, String titulo, String descricao, String causa) {
        this.id = id;
        this.titulo = titulo;
        this.descricao = descricao;
    }

    public String getTitulo() { return titulo; }
    public List<Vaga> getVagas() { return vagas; }
}