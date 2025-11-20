package com.redemaissocial.campanha;

import com.redemaissocial.usuario.Candidato;
import java.util.Date;

public class Candidatura {
    private Candidato candidato;
    private Vaga vaga;
    private Date data = new Date();
    private String status = "PENDENTE";

    public void setCandidato(Candidato c) { this.candidato = c; }
    public void setVaga(Vaga v) { this.vaga = v; }
}