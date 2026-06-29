package com.balancodoportuga.bean;

import com.balancodoportuga.model.Pagamento;
import com.balancodoportuga.model.Reserva;
import com.balancodoportuga.service.PagamentoService;
import com.balancodoportuga.service.ReservaService;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@jakarta.faces.view.ViewScoped
@jakarta.inject.Named("pagamentoBean")
public class PagamentoBean implements Serializable {

    private static final double EPS = 0.0001;

    private PagamentoService pagamentoService;
    private ReservaService reservaService;

    public PagamentoBean() {
        this.pagamentoService = new PagamentoService();
        this.reservaService = new ReservaService();
    }

    // Funcionário
    private List<Reserva> reservas;
    private List<Reserva> reservasFiltradas;
    private Reserva reservaSelecionada;

    // Filtros
    private String filtroBusca;
    private String filtroStatus = "";

    // Pagamento registro
    private double valorPagamento;
    private String metodoPagamentoFuncionario = "Pix";

    // Multa
    private double valorMulta;
    private String motivoMulta;

    // Detalhes
    private List<Pagamento> pagamentosDetalhe;
    private int progressoPagamento;
    private double valorRestante;
    private String corProgresso;

    // Cliente mode
    private List<Reserva> reservasCliente;
    private Integer reservaClienteSelecionadaId;
    private List<Pagamento> pagamentosCliente;
    private double valorTotalReservaCliente;
    private double totalPagoCliente;
    private double restanteCliente;
    private int progressoCliente;
    private String corProgressoCliente;
    private double valorPagamentoCliente;
    private String metodoPagamentoCliente = "Pix";

    // Histórico
    private long qtdPagamentosHistorico;
    private double totalPagoHistorico;
    private double mediaPagamentosHistorico;

    @PostConstruct
    public void init() {
        carregarDados();
    }

    private LoginBean getLoginBean() {
        FacesContext ctx = FacesContext.getCurrentInstance();

        if (ctx == null) {
            return null;
        }

        return (LoginBean) ctx.getApplication()
                .evaluateExpressionGet(ctx, "#{loginBean}", LoginBean.class);
    }

    private void carregarDados() {
        try {
            reservas = reservaService.listarTodas();
            reservasFiltradas = new ArrayList<>(reservas);

            LoginBean lb = getLoginBean();

            if (lb != null && lb.getClienteLogado() != null) {
                reservasCliente = reservaService.listarPorCliente(lb.getClienteLogado().getId());
            } else {
                reservasCliente = new ArrayList<>();
            }

        } catch (Exception e) {
            reservas = new ArrayList<>();
            reservasFiltradas = new ArrayList<>();
            reservasCliente = new ArrayList<>();
        }
    }

    // --- Funcionário ---

    public void filtrar() {
        if (reservas == null) {
            return;
        }

        String termo = filtroBusca != null ? filtroBusca.trim().toLowerCase() : "";

        reservasFiltradas = reservas.stream()
                .filter(r -> filtroStatus == null || filtroStatus.isEmpty()
                || getStatusPagamento(r).equalsIgnoreCase(filtroStatus))
                .filter(r -> termo.isEmpty()
                || (r.getClient() != null && r.getClient().getNome().toLowerCase().contains(termo))
                || (r.getVehicle() != null && r.getVehicle().getModelo().toLowerCase().contains(termo))
                || (r.getVehicle() != null && r.getVehicle().getPlaca().toLowerCase().contains(termo)))
                .collect(Collectors.toList());
    }

    public void limparFiltro() {
        filtroStatus = "";
        filtroBusca = null;
        reservasFiltradas = reservas != null ? new ArrayList<>(reservas) : new ArrayList<>();
    }

    public String getStatusPagamento(Reserva r) {
        try {
            double totalPago = pagamentoService.getTotalPago(r.getId());

            if (totalPago >= r.getTotal()) {
                return "PAGO";
            }

            if (totalPago > 0) {
                return "PARCIAL";
            }

            return "PENDENTE";

        } catch (Exception e) {
            return "PENDENTE";
        }
    }

    public void prepararRegistro() {
        if (reservaSelecionada == null) {
            addFalha(FacesMessage.SEVERITY_WARN, "Selecione uma reserva!");
            return;
        }

        try {
            valorRestante = pagamentoService.calcularRestante(reservaSelecionada);
        } catch (Exception e) {
            valorRestante = 0;
        }

        valorPagamento = 0;
        metodoPagamentoFuncionario = "Pix";
    }

    public void prepararDetalhes() {
        if (reservaSelecionada == null) {
            addFalha(FacesMessage.SEVERITY_WARN, "Selecione uma reserva!");
            return;
        }

        try {
            pagamentosDetalhe = pagamentoService.listarPorReserva(reservaSelecionada.getId());
            progressoPagamento = pagamentoService.calcularProgresso(reservaSelecionada);
            valorRestante = pagamentoService.calcularRestante(reservaSelecionada);
            corProgresso = pagamentoService.getCorProgresso(progressoPagamento);

        } catch (Exception e) {
            addFalha(FacesMessage.SEVERITY_ERROR, "Erro ao carregar detalhes: " + e.getMessage());
        }
    }

    public void prepararMulta() {
        if (reservaSelecionada == null) {
            addFalha(FacesMessage.SEVERITY_WARN, "Selecione uma reserva!");
            return;
        }

        valorMulta = 0;
        motivoMulta = null;
    }

    public void registrarPagamento() {
        try {
            if (reservaSelecionada == null) {
                addFalha(FacesMessage.SEVERITY_WARN, "Selecione uma reserva!");
                return;
            }

            if (valorPagamento <= 0) {
                addFalha(FacesMessage.SEVERITY_ERROR, "O valor deve ser maior que zero!");
                return;
            }

            Reserva r = reservaService.buscarPorId(reservaSelecionada.getId());

            if (r == null) {
                addFalha(FacesMessage.SEVERITY_WARN, "Reserva não encontrada.");
                return;
            }

            double restanteAtual = pagamentoService.calcularRestante(r);
            valorRestante = restanteAtual;

            if (restanteAtual <= EPS) {
                addFalha(FacesMessage.SEVERITY_WARN, "Esta reserva já está totalmente paga.");
                return;
            }

            if (valorPagamento - restanteAtual > EPS) {
                addFalha(
                        FacesMessage.SEVERITY_WARN,
                        "O valor informado não pode ser maior que o valor restante da reserva. Valor restante: R$ "
                        + formatarMoeda(restanteAtual)
                );
                return;
            }

            String metodo = (metodoPagamentoFuncionario != null && !metodoPagamentoFuncionario.isBlank())
                    ? metodoPagamentoFuncionario
                    : "Pix";

            pagamentoService.registrarPagamento(r, valorPagamento, metodo);

            carregarDados();

            valorPagamento = 0;
            metodoPagamentoFuncionario = "Pix";
            valorRestante = pagamentoService.calcularRestante(r);

            addMsg(FacesMessage.SEVERITY_INFO, "Pagamento registrado com sucesso!");

        } catch (Exception e) {
            addFalha(FacesMessage.SEVERITY_ERROR, "Erro: " + e.getMessage());
        }
    }

    public void aplicarMulta() {
        try {
            if (reservaSelecionada == null) {
                addFalha(FacesMessage.SEVERITY_WARN, "Selecione uma reserva!");
                return;
            }

            if (valorMulta <= 0) {
                addFalha(FacesMessage.SEVERITY_ERROR, "Valor deve ser maior que zero!");
                return;
            }

            if (motivoMulta == null || motivoMulta.isBlank()) {
                addFalha(FacesMessage.SEVERITY_WARN, "Descreva o motivo!");
                return;
            }

            pagamentoService.aplicarMulta(reservaSelecionada.getId(), valorMulta, motivoMulta);

            carregarDados();

            valorMulta = 0;
            motivoMulta = null;

            addMsg(FacesMessage.SEVERITY_INFO, "Multa aplicada com sucesso!");

        } catch (Exception e) {
            addFalha(FacesMessage.SEVERITY_ERROR, "Erro: " + e.getMessage());
        }
    }

    // --- Cliente ---

    public void onReservaClienteSelecionada() {
        if (reservaClienteSelecionadaId == null) {
            return;
        }

        try {
            Reserva r = reservaService.buscarPorId(reservaClienteSelecionadaId);

            if (r != null) {
                valorTotalReservaCliente = r.getTotal();
                totalPagoCliente = pagamentoService.getTotalPago(r.getId());
                restanteCliente = pagamentoService.calcularRestante(r);
                progressoCliente = pagamentoService.calcularProgresso(r);
                corProgressoCliente = pagamentoService.getCorProgresso(progressoCliente);
                pagamentosCliente = pagamentoService.listarPorReserva(r.getId());
            }

        } catch (Exception e) {
            addFalha(FacesMessage.SEVERITY_ERROR, "Erro: " + e.getMessage());
        }
    }

    public void efetuarPagamentoCliente() {
        try {
            if (reservaClienteSelecionadaId == null) {
                addFalha(FacesMessage.SEVERITY_WARN, "Selecione uma reserva!");
                return;
            }

            if (valorPagamentoCliente <= 0) {
                addFalha(FacesMessage.SEVERITY_ERROR, "O valor deve ser maior que zero!");
                return;
            }

            Reserva r = reservaService.buscarPorId(reservaClienteSelecionadaId);

            if (r == null) {
                addFalha(FacesMessage.SEVERITY_WARN, "Reserva não encontrada.");
                return;
            }

            double restanteAtual = pagamentoService.calcularRestante(r);
            restanteCliente = restanteAtual;

            if (restanteAtual <= EPS) {
                addFalha(FacesMessage.SEVERITY_WARN, "Esta reserva já está totalmente paga.");
                return;
            }

            if (valorPagamentoCliente - restanteAtual > EPS) {
                addFalha(
                        FacesMessage.SEVERITY_WARN,
                        "O valor informado não pode ser maior que o valor restante da reserva. Valor restante: R$ "
                        + formatarMoeda(restanteAtual)
                );
                return;
            }

            String metodo = (metodoPagamentoCliente != null && !metodoPagamentoCliente.isBlank())
                    ? metodoPagamentoCliente
                    : "Pix";

            pagamentoService.registrarPagamento(r, valorPagamentoCliente, metodo);

            onReservaClienteSelecionada();

            valorPagamentoCliente = 0;
            metodoPagamentoCliente = "Pix";

            addMsg(FacesMessage.SEVERITY_INFO, "Pagamento registrado com sucesso!");

        } catch (Exception e) {
            addFalha(FacesMessage.SEVERITY_ERROR, "Erro: " + e.getMessage());
        }
    }

    public void carregarHistoricoCliente() {
        try {
            LoginBean lb = getLoginBean();

            if (lb == null || lb.getClienteLogado() == null) {
                return;
            }

            List<Pagamento> lista = pagamentoService.listarPorCliente(lb.getClienteLogado().getId());

            qtdPagamentosHistorico = lista.size();
            totalPagoHistorico = lista.stream().mapToDouble(Pagamento::getValor).sum();
            mediaPagamentosHistorico = qtdPagamentosHistorico > 0
                    ? totalPagoHistorico / qtdPagamentosHistorico
                    : 0;

        } catch (Exception e) {
            addMsg(FacesMessage.SEVERITY_ERROR, "Erro ao carregar histórico: " + e.getMessage());
        }
    }

    private void addMsg(FacesMessage.Severity severity, String msg) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, msg, null));
    }

    private void addFalha(FacesMessage.Severity severity, String msg) {
        addMsg(severity, msg);
        FacesContext.getCurrentInstance().validationFailed();
    }

    private String formatarMoeda(double valor) {
        return String.format(new Locale("pt", "BR"), "%,.2f", valor);
    }

    // --- Getters / Setters ---

    public List<Reserva> getReservasFuncionario() {
        return reservasFiltradas;
    }

    public Reserva getReservaSelecionada() {
        return reservaSelecionada;
    }

    public void setReservaSelecionada(Reserva r) {
        this.reservaSelecionada = r;
    }

    public String getFiltroBusca() {
        return filtroBusca;
    }

    public void setFiltroBusca(String s) {
        this.filtroBusca = s;
    }

    public String getFiltroStatus() {
        return filtroStatus;
    }

    public void setFiltroStatus(String s) {
        this.filtroStatus = s;
    }

    public double getValorPagamento() {
        return valorPagamento;
    }

    public void setValorPagamento(double v) {
        this.valorPagamento = v;
    }

    public String getMetodoPagamentoFuncionario() {
        return metodoPagamentoFuncionario;
    }

    public void setMetodoPagamentoFuncionario(String m) {
        this.metodoPagamentoFuncionario = m;
    }

    public double getValorMulta() {
        return valorMulta;
    }

    public void setValorMulta(double v) {
        this.valorMulta = v;
    }

    public String getMotivoMulta() {
        return motivoMulta;
    }

    public void setMotivoMulta(String m) {
        this.motivoMulta = m;
    }

    public List<Pagamento> getPagamentosDetalhe() {
        return pagamentosDetalhe;
    }

    public int getProgressoPagamento() {
        return progressoPagamento;
    }

    public double getValorRestante() {
        return valorRestante;
    }

    public String getCorProgresso() {
        return corProgresso;
    }

    public List<Reserva> getReservasCliente() {
        return reservasCliente;
    }

    public Integer getReservaClienteSelecionadaId() {
        return reservaClienteSelecionadaId;
    }

    public void setReservaClienteSelecionadaId(Integer id) {
        this.reservaClienteSelecionadaId = id;
    }

    public List<Pagamento> getPagamentosCliente() {
        return pagamentosCliente;
    }

    public double getValorTotalReservaCliente() {
        return valorTotalReservaCliente;
    }

    public double getTotalPagoCliente() {
        return totalPagoCliente;
    }

    public double getRestanteCliente() {
        return restanteCliente;
    }

    public int getProgressoCliente() {
        return progressoCliente;
    }

    public String getCorProgressoCliente() {
        return corProgressoCliente;
    }

    public double getValorPagamentoCliente() {
        return valorPagamentoCliente;
    }

    public void setValorPagamentoCliente(double v) {
        this.valorPagamentoCliente = v;
    }

    public String getMetodoPagamentoCliente() {
        return metodoPagamentoCliente;
    }

    public void setMetodoPagamentoCliente(String m) {
        this.metodoPagamentoCliente = m;
    }

    public long getQtdPagamentosHistorico() {
        return qtdPagamentosHistorico;
    }

    public double getTotalPagoHistorico() {
        return totalPagoHistorico;
    }

    public double getMediaPagamentosHistorico() {
        return mediaPagamentosHistorico;
    }
}
