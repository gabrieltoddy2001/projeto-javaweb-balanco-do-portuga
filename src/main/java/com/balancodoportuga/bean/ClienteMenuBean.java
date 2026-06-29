package com.balancodoportuga.bean;

import com.balancodoportuga.model.Reserva;
import com.balancodoportuga.service.ClienteService;
import jakarta.annotation.PostConstruct;
import jakarta.faces.context.FacesContext;
import java.io.Serializable;

@jakarta.faces.view.ViewScoped
@jakarta.inject.Named("clienteMenuBean")
public class ClienteMenuBean implements Serializable {

    private ClienteService clienteService;

    private Reserva reservaAtual;
    private boolean contatoVisivel;

    public ClienteMenuBean() {
        this.clienteService = new ClienteService();
    }

    @PostConstruct
    public void init() {
        carregarReservaAtual();
        contatoVisivel = false;
    }

    public void carregarReservaAtual() {
        try {
            FacesContext ctx = FacesContext.getCurrentInstance();
            LoginBean lb = (LoginBean) ctx.getApplication()
                    .evaluateExpressionGet(ctx, "#{loginBean}", LoginBean.class);
            if (lb != null && lb.getClienteLogado() != null) {
                reservaAtual = clienteService.buscarReservaAtiva(lb.getClienteLogado().getId());
            } else {
                reservaAtual = null;
            }
        } catch (Exception ex) {
            reservaAtual = null;
        }
    }

    public void mostrarContato() {
        contatoVisivel = true;
    }

    public Reserva getReservaAtual() { return reservaAtual; }
    public boolean isContatoVisivel() { return contatoVisivel; }
    public void setContatoVisivel(boolean contatoVisivel) { this.contatoVisivel = contatoVisivel; }
}
