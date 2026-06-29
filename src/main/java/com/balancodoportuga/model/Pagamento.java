package com.balancodoportuga.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Pagamento implements Serializable {

    private static final DateTimeFormatter FMT_BR = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private int id;
    private Reserva reserva;
    private double valor;
    private LocalDate dataPagamento;
    private String metodoPagamento;
    private String tipo;

    public Pagamento() {
        this.tipo = "Pagamento";
    }

    public Pagamento(int id, Reserva reserva, double valor, LocalDate dataPagamento, String metodoPagamento) {
        this.id = id;
        this.reserva = reserva;
        this.valor = valor;
        this.dataPagamento = dataPagamento;
        this.metodoPagamento = metodoPagamento;
        this.tipo = "Pagamento";
    }

    public Pagamento(int id, Reserva reserva, double valor, LocalDate dataPagamento, String metodoPagamento, String tipo) {
        this.id = id;
        this.reserva = reserva;
        this.valor = valor;
        this.dataPagamento = dataPagamento;
        this.metodoPagamento = metodoPagamento;
        this.tipo = tipo;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public Reserva getReserva() {
        return reserva;
    }

    public void setReserva(Reserva reserva) {
        this.reserva = reserva;
    }


    public double getValor() {
        return valor;
    }

    public void setValor(double valor) {
        this.valor = valor;
    }


    public LocalDate getDataPagamento() {
        return dataPagamento;
    }

    public void setDataPagamento(LocalDate dataPagamento) {
        this.dataPagamento = dataPagamento;
    }


    public String getMetodoPagamento() {
        return metodoPagamento;
    }

    public void setMetodoPagamento(String metodoPagamento) {
        this.metodoPagamento = metodoPagamento;
    }


    public String getTipo() {
        if (tipo == null || tipo.isBlank()) {
            return "Pagamento";
        }

        return tipo;
    }

    public void setTipo(String tipo) {
        if (tipo == null || tipo.isBlank()) {
            this.tipo = "Pagamento";
        } else {
            this.tipo = tipo;
        }
    }


    public String getDataPagamentoFormatada() {
        return dataPagamento != null ? dataPagamento.format(FMT_BR) : "";
    }

    public String getValorFormatado() {
        return String.format("R$ %.2f", valor);
    }

    public boolean isMulta() {
        return "Multa".equalsIgnoreCase(getTipo());
    }

    public boolean isPagamento() {
        return "Pagamento".equalsIgnoreCase(getTipo());
    }
}