package com.balancodoportuga.bean;

import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import java.io.Serializable;

@Named("painelBean")
@SessionScoped
public class PainelBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private String paginaFuncionario = "/WEB-INF/includes/funcionario/dashboard.xhtml";
    private String menuFuncionarioAtivo = "dashboard";

    private String paginaCliente = "/WEB-INF/includes/cliente/inicio.xhtml";
    private String menuClienteAtivo = "inicio";

    public void abrirFuncionario(String pagina) {
        switch (pagina) {
            case "dashboard":
                paginaFuncionario = "/WEB-INF/includes/funcionario/dashboard.xhtml";
                menuFuncionarioAtivo = "dashboard";
                break;

            case "clientes":
                paginaFuncionario = "/WEB-INF/includes/funcionario/clientes.xhtml";
                menuFuncionarioAtivo = "clientes";
                break;

            case "veiculos":
                paginaFuncionario = "/WEB-INF/includes/funcionario/veiculos.xhtml";
                menuFuncionarioAtivo = "veiculos";
                break;

            case "reservas":
                paginaFuncionario = "/WEB-INF/includes/funcionario/reservas.xhtml";
                menuFuncionarioAtivo = "reservas";
                break;

            case "pagamentos":
                paginaFuncionario = "/WEB-INF/includes/funcionario/pagamentos.xhtml";
                menuFuncionarioAtivo = "pagamentos";
                break;

            default:
                paginaFuncionario = "/WEB-INF/includes/funcionario/dashboard.xhtml";
                menuFuncionarioAtivo = "dashboard";
                break;
        }
    }

    public void abrirCliente(String pagina) {
        switch (pagina) {
            case "inicio":
                paginaCliente = "/WEB-INF/includes/cliente/inicio.xhtml";
                menuClienteAtivo = "inicio";
                break;

            case "reservas":
                paginaCliente = "/WEB-INF/includes/cliente/reservas.xhtml";
                menuClienteAtivo = "reservas";
                break;

            case "pagamentos":
                paginaCliente = "/WEB-INF/includes/cliente/pagamentos.xhtml";
                menuClienteAtivo = "pagamentos";
                break;

            default:
                paginaCliente = "/WEB-INF/includes/cliente/inicio.xhtml";
                menuClienteAtivo = "inicio";
                break;
        }
    }

    public String getPaginaFuncionario() {
        return paginaFuncionario;
    }

    public String getPaginaCliente() {
        return paginaCliente;
    }

    public String menuFuncionarioClasse(String pagina) {
        return pagina.equals(menuFuncionarioAtivo) ? "active" : "";
    }

    public String menuClienteClasse(String pagina) {
        return pagina.equals(menuClienteAtivo) ? "active" : "";
    }
}