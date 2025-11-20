package com.redemaissocial.usuario;

import com.redemaissocial.campanha.Vaga;
import com.redemaissocial.campanha.Candidatura;
import com.redemaissocial.entidade.PessoaFisica;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Candidato extends PessoaFisica {
    private Perfil perfil;
    private final List<Candidatura> candidaturas = new ArrayList<>();

    public Candidato(String id, String nomeCompleto, String email, String telefone,
                     String cpf, String rg, Date dataNascimento) {
        super(id, nomeCompleto, email, telefone, cpf, rg, dataNascimento);
    }

    public void candidatar(Vaga vaga) {
        Candidatura c = new Candidatura();
        c.setCandidato(this);
        c.setVaga(vaga);
        candidaturas.add(c);
        vaga.getCandidaturas().add(c);
        System.out.println("Candidatura enviada para: " + vaga.getDescricao());
    }

    public void setPerfil(Perfil perfil) { this.perfil = perfil; }
    public Perfil getPerfil() { return perfil; }
}