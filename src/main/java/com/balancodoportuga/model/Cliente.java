package com.balancodoportuga.model;

import java.io.Serializable;

public class Cliente implements Serializable {
    private int id;
    private String nome;
    private String cpf;
    private String email;
    private String telefone;
    private String endereco;
    private String numero;
    private String bairro;
    private String cidade;
    private String estado;
    private String cnh;

    public Cliente() {}

    public Cliente(int id, String nome, String cpf, String email, String telefone,
                   String endereco, String numero, String bairro, String cidade,
                   String estado, String cnh) {
        this.id = id;
        this.nome = nome;
        this.cpf = cpf;
        this.email = email;
        this.telefone = telefone;
        this.endereco = endereco;
        this.numero = numero;
        this.bairro = bairro;
        this.cidade = cidade;
        this.estado = estado;
        this.cnh = cnh;
    }

    public Cliente(int id, String nome, String cpf, String email, String telefone) {
        this(id, nome, cpf, email, telefone, "", "", "", "", "", "");
    }

    public Cliente(int id) {
        this.id = id;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }
    public String getEndereco() { return endereco; }
    public void setEndereco(String endereco) { this.endereco = endereco; }
    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }
    public String getBairro() { return bairro; }
    public void setBairro(String bairro) { this.bairro = bairro; }
    public String getCidade() { return cidade; }
    public void setCidade(String cidade) { this.cidade = cidade; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getCnh() { return cnh; }
    public void setCnh(String cnh) { this.cnh = cnh; }

    public String getEnderecoCompleto() {
        return String.format("%s, %s - %s, %s/%s", endereco, numero, bairro, cidade, estado);
    }

    @Override
    public String toString() {
        return nome;
    }
}
