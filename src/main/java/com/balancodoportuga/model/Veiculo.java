package com.balancodoportuga.model;

import java.io.Serializable;

public class Veiculo implements Serializable {

    private int id;
    private String modelo;
    private String marca;
    private String placa;
    private int ano;
    private double diaria;
    private String tipo;
    private String foto;

    public Veiculo() {
        this.tipo = "CARRO";
    }

    public Veiculo(int id, String modelo, String marca, String placa, int ano, double diaria) {
        this.id = id;
        this.modelo = modelo;
        this.marca = marca;
        this.placa = placa;
        this.ano = ano;
        this.diaria = diaria;
        this.tipo = "CARRO";
    }

    public Veiculo(int id, String modelo, String marca, String placa, int ano, double diaria, String tipo, String foto) {
        this.id = id;
        this.modelo = modelo;
        this.marca = marca;
        this.placa = placa;
        this.ano = ano;
        this.diaria = diaria;
        this.tipo = tipo;
        this.foto = foto;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }


    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }


    public String getPlaca() {
        return placa;
    }

    public void setPlaca(String placa) {
        this.placa = placa;
    }


    public int getAno() {
        return ano;
    }

    public void setAno(int ano) {
        this.ano = ano;
    }


    public double getDiaria() {
        return diaria;
    }

    public void setDiaria(double diaria) {
        this.diaria = diaria;
    }


    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        if (tipo == null || tipo.trim().isEmpty()) {
            this.tipo = "CARRO";
        } else {
            this.tipo = tipo;
        }
    }


    public String getFoto() {
        return foto;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }


    public boolean isCarro() {
        return "CARRO".equalsIgnoreCase(tipo);
    }

    public boolean isMoto() {
        return "MOTO".equalsIgnoreCase(tipo);
    }

    public boolean temFoto() {
        return foto != null && !foto.trim().isEmpty();
    }

    @Override
    public String toString() {
        return modelo + " - " + placa;
    }
}