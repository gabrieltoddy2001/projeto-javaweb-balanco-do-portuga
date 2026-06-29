package com.balancodoportuga.bean;

import com.balancodoportuga.model.Reserva;
import com.balancodoportuga.service.PagamentoService;
import com.balancodoportuga.service.ReservaService;
import jakarta.annotation.PostConstruct;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Named("dashboardClienteBean")
@ViewScoped
public class DashboardClienteBean implements Serializable {

    private ReservaService reservaService;
    private PagamentoService pagamentoService;

    private List<Reserva> reservasCliente;
    private List<Reserva> ultimasReservas;
    private Reserva reservaAtual;

    private int totalReservas;
    private int reservasAtivas;
    private int reservasConcluidas;
    private int reservasCanceladas;

    private double valorTotalReservas;
    private double totalPago;
    private double valorPendente;
    private int progressoReservaAtual;
    private String corProgressoReservaAtual;

    public DashboardClienteBean() {
        this.reservaService = new ReservaService();
        this.pagamentoService = new PagamentoService();
    }

    @PostConstruct
    public void init() {
        carregarDashboard();
    }

    private LoginBean getLoginBean() {
        FacesContext ctx = FacesContext.getCurrentInstance();

        if (ctx == null) {
            return null;
        }

        return (LoginBean) ctx.getApplication()
                .evaluateExpressionGet(ctx, "#{loginBean}", LoginBean.class);
    }

    public void carregarDashboard() {
        try {
            LoginBean lb = getLoginBean();

            if (lb == null || lb.getClienteLogado() == null) {
                reservasCliente = new ArrayList<>();
                ultimasReservas = new ArrayList<>();
                return;
            }

            reservasCliente = reservaService.listarPorCliente(lb.getClienteLogado().getId());

            if (reservasCliente == null) {
                reservasCliente = new ArrayList<>();
            }

            totalReservas = reservasCliente.size();
            reservasAtivas = 0;
            reservasConcluidas = 0;
            reservasCanceladas = 0;
            valorTotalReservas = 0;
            totalPago = 0;
            valorPendente = 0;

            for (Reserva r : reservasCliente) {
                String status = normalizarStatus(r.getStatus());

                if (isAtiva(status)) {
                    reservasAtivas++;
                } else if (isConcluida(status)) {
                    reservasConcluidas++;
                } else if (isCancelada(status)) {
                    reservasCanceladas++;
                }

                double totalReserva = r.getTotal();
                double pagoReserva = pagamentoService.getTotalPago(r.getId());
                double restante = Math.max(0, totalReserva - pagoReserva);

                valorTotalReservas += totalReserva;
                totalPago += pagoReserva;
                valorPendente += restante;
            }

            reservaAtual = reservasCliente.stream()
                    .filter(r -> isAtiva(normalizarStatus(r.getStatus())))
                    .max(Comparator.comparing(Reserva::getId))
                    .orElse(null);

            if (reservaAtual != null) {
                progressoReservaAtual = pagamentoService.calcularProgresso(reservaAtual);
                corProgressoReservaAtual = pagamentoService.getCorProgresso(progressoReservaAtual);
            } else {
                progressoReservaAtual = 0;
                corProgressoReservaAtual = "#16a34a";
            }

            ultimasReservas = reservasCliente.stream()
                    .sorted(Comparator.comparing(Reserva::getId).reversed())
                    .limit(5)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            reservasCliente = new ArrayList<>();
            ultimasReservas = new ArrayList<>();
            reservaAtual = null;
        }
    }

    private String normalizarStatus(String status) {
        if (status == null) {
            return "";
        }

        String semAcento = Normalizer.normalize(status, Normalizer.Form.NFD)
                .replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");

        return semAcento.trim().toUpperCase();
    }

    private boolean isAtiva(String status) {
        return status.equals("ATIVA")
                || status.equals("SOLICITADA")
                || status.equals("EM ANDAMENTO")
                || status.equals("EM_ANDAMENTO");
    }

    private boolean isConcluida(String status) {
        return status.equals("CONCLUIDA");
    }

    private boolean isCancelada(String status) {
        return status.equals("CANCELADA");
    }

    public int getPercentualPagoGeral() {
        return valorTotalReservas > 0 ? (int) ((totalPago * 100) / valorTotalReservas) : 0;
    }

    public List<Reserva> getReservasCliente() {
        return reservasCliente;
    }

    public List<Reserva> getUltimasReservas() {
        return ultimasReservas;
    }

    public Reserva getReservaAtual() {
        return reservaAtual;
    }

    public int getTotalReservas() {
        return totalReservas;
    }

    public int getReservasAtivas() {
        return reservasAtivas;
    }

    public int getReservasConcluidas() {
        return reservasConcluidas;
    }

    public int getReservasCanceladas() {
        return reservasCanceladas;
    }

    public double getValorTotalReservas() {
        return valorTotalReservas;
    }

    public double getTotalPago() {
        return totalPago;
    }

    public double getValorPendente() {
        return valorPendente;
    }

    public int getProgressoReservaAtual() {
        return progressoReservaAtual;
    }

    public String getCorProgressoReservaAtual() {
        return corProgressoReservaAtual;
    }
}