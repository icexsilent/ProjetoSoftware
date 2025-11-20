package com.redemaissocial.entidade;

public abstract class Entidade {
    private String id;
    private String nomeCompleto;
    private String email;
    private String telefone;

    public Entidade(String id, String nomeCompleto, String email, String telefone) {
        this.id = id;
        this.nomeCompleto = nomeCompleto;
        this.email = email;
        this.telefone = telefone;
    }

    public void getDados() {
        System.out.println("ID: " + id + " | " + nomeCompleto + " | " + email);
    }

    public String getId() { return id; }
    public String getNomeCompleto() { return nomeCompleto; }
    public String getEmail() { return email; }
}