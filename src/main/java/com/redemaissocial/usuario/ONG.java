package com.redemaissocial.usuario;

import com.redemaissocial.campanha.Campanha;
import com.redemaissocial.entidade.PessoaJuridica;
import java.util.ArrayList;
import java.util.List;

public class ONG extends PessoaJuridica {
    private String missao;
    private final List<Campanha> campanhas = new ArrayList<>();

    public ONG(String id, String nome, String email, String tel, String cnpj, String razao, String missao, String site) {
        super(id, nome, email, tel, cnpj, razao, "");
        this.missao = missao;
    }

    public void criarCampanha(Campanha c) {
        campanhas.add(c);
        System.out.println("Campanha criada: " + c.getTitulo());
    }

    public List<Campanha> getCampanhas() { return campanhas; }
}