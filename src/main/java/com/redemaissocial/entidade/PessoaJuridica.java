package com.redemaissocial.entidade;

public class PessoaJuridica extends Entidade {
    private String cnpj;
    private String razaoSocial;

    public PessoaJuridica(String id, String nomeCompleto, String email, String telefone,
                          String cnpj, String razaoSocial, String ie) {
        super(id, nomeCompleto, email, telefone);
        this.cnpj = cnpj;
        this.razaoSocial = razaoSocial;
    }
}