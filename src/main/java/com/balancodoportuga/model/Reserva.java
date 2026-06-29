package com.balancodoportuga.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Reserva implements Serializable {

    private static final DateTimeFormatter FMT_BR = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private int id;
    private Cliente client;
    private Veiculo vehicle;
    private LocalDate dataInicio;
    private LocalDate dataFim;
    private double total;
    private String status;
    private String statusPagamento;

    public Reserva() {}

    public Reserva(int id, Cliente client, Veiculo vehicle, LocalDate inicio,
                   LocalDate fim, double total, String status, String statusPagamento) {
        this.id = id;
        this.client = client;
        this.vehicle = vehicle;
        this.dataInicio = inicio;
        this.dataFim = fim;
        this.total = total;
        this.status = normalizarStatus(status);
        this.statusPagamento = statusPagamento;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public Cliente getClient() {
        return client;
    }

    public void setClient(Cliente client) {
        this.client = client;
    }


    public Veiculo getVehicle() {
        return vehicle;
    }

    public void setVehicle(Veiculo vehicle) {
        this.vehicle = vehicle;
    }


    public LocalDate getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(LocalDate dataInicio) {
        this.dataInicio = dataInicio;
    }


    public LocalDate getDataFim() {
        return dataFim;
    }

    public void setDataFim(LocalDate dataFim) {
        this.dataFim = dataFim;
    }


    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }


    public String getStatus() {
        return normalizarStatus(status);
    }

    public void setStatus(String status) {
        this.status = normalizarStatus(status);
    }


    public String getStatusPagamento() {
        return statusPagamento;
    }

    public void setStatusPagamento(String statusPagamento) {
        this.statusPagamento = statusPagamento;
    }


    public String getDataInicioFormatada() {
        return dataInicio != null ? dataInicio.format(FMT_BR) : "";
    }

    public String getDataFimFormatada() {
        return dataFim != null ? dataFim.format(FMT_BR) : "";
    }

    public String getTotalFormatado() {
        return String.format("R$ %.2f", total);
    }

    public String getPeriodoFormatado() {
        return getDataInicioFormatada() + " a " + getDataFimFormatada();
    }

    public String getDescricaoReserva() {
        String modelo = vehicle != null ? vehicle.getModelo() : "?";

        return String.format(
                "Reserva #%d - %s (%s) - %s",
                id,
                modelo,
                getPeriodoFormatado(),
                getTotalFormatado()
        );
    }

    public String getDescricaoReservaMobile() {
        String modelo = vehicle != null ? vehicle.getModelo() : "?";

        return String.format(
                "#%d - %s | %s | %s",
                id,
                modelo,
                getPeriodoFormatado(),
                getTotalFormatado()
        );
    }


    public String getStatusDescricao() {
        String statusNormalizado = getStatus();

        switch (statusNormalizado) {
            case "SOLICITADA":
                return "Solicitada";

            case "EM_ANDAMENTO":
                return "Em andamento";

            case "CONCLUIDA":
                return "Concluída";

            case "CANCELADA":
                return "Cancelada";

            default:
                return "Solicitada";
        }
    }

    public String getStatusClasse() {
        String statusNormalizado = getStatus();

        switch (statusNormalizado) {
            case "SOLICITADA":
                return "solicitada";

            case "EM_ANDAMENTO":
                return "em-andamento";

            case "CONCLUIDA":
                return "concluida";

            case "CANCELADA":
                return "cancelada";

            default:
                return "solicitada";
        }
    }

    public boolean isSolicitada() {
        return "SOLICITADA".equalsIgnoreCase(getStatus());
    }

    public boolean isEmAndamento() {
        return "EM_ANDAMENTO".equalsIgnoreCase(getStatus());
    }

    public boolean isConcluida() {
        return "CONCLUIDA".equalsIgnoreCase(getStatus());
    }

    public boolean isCancelada() {
        return "CANCELADA".equalsIgnoreCase(getStatus());
    }

    private String normalizarStatus(String status) {
        if (status == null || status.isBlank()) {
            return "SOLICITADA";
        }

        String s = status.trim()
                .toUpperCase()
                .replace("Á", "A")
                .replace("À", "A")
                .replace("Ã", "A")
                .replace("Â", "A")
                .replace("É", "E")
                .replace("Ê", "E")
                .replace("Í", "I")
                .replace("Ó", "O")
                .replace("Õ", "O")
                .replace("Ô", "O")
                .replace("Ú", "U")
                .replace("Ç", "C");

        switch (s) {
            case "SOLICITADA":
            case "ATIVA":
                return "SOLICITADA";

            case "EM ANDAMENTO":
            case "EM_ANDAMENTO":
                return "EM_ANDAMENTO";

            case "CONCLUIDA":
                return "CONCLUIDA";

            case "CANCELADA":
                return "CANCELADA";

            default:
                return "SOLICITADA";
        }
    }

    @Override
    public String toString() {
        return String.format("Reserva #%d - %s (%s)",
                getId(),
                getVehicle() != null ? getVehicle().getModelo() : "?",
                getVehicle() != null ? getVehicle().getPlaca() : "?");
    }
}