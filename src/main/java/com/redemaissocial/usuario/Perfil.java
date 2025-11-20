package com.redemaissocial.usuario;

import com.redemaissocial.perfil.Habilidade;
import com.redemaissocial.perfil.Interesse;
import java.util.ArrayList;
import java.util.List;

public class Perfil {
    private int raioBuscaKm = 20;
    private final List<Habilidade> habilidades = new ArrayList<>();
    private final List<Interesse> interesses = new ArrayList<>();

    public List<Habilidade> getHabilidades() { return habilidades; }
    public List<Interesse> getInteresses() { return interesses; }
    public void setRaioBuscaKm(int raio) { this.raioBuscaKm = raio; }
}