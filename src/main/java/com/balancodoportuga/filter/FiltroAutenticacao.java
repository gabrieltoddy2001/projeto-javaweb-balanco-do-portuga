package com.balancodoportuga.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@WebFilter("*.xhtml")
public class FiltroAutenticacao implements Filter {

    private final List<String> paginasPublicas = Arrays.asList(
            "/index.xhtml",
            "/login.xhtml"
    );

    private final List<String> paginasFuncionario = Arrays.asList(
            "/funcionario-menu.xhtml"
    );

    private final List<String> paginasCliente = Arrays.asList(
            "/cliente-menu.xhtml"
    );

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        String contextPath = req.getContextPath();
        String uri = req.getRequestURI();
        String pagina = uri.substring(contextPath.length());

        if (pagina.equals("/") || isRecursoJSF(pagina) || isPaginaPublica(pagina)) {
            chain.doFilter(request, response);
            return;
        }

        boolean areaFuncionario = isPaginaFuncionario(pagina);
        boolean areaCliente = isPaginaCliente(pagina);

        if (!areaFuncionario && !areaCliente) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = req.getSession(false);

        Boolean usuarioLogado = null;
        String tipoUsuario = null;

        if (session != null) {
            usuarioLogado = (Boolean) session.getAttribute("usuarioLogado");
            tipoUsuario = (String) session.getAttribute("tipoUsuario");
        }

        if (!Boolean.TRUE.equals(usuarioLogado)) {
            if (areaFuncionario) {
                resp.sendRedirect(contextPath + "/login.xhtml?acesso=funcionario");
                return;
            }

            if (areaCliente) {
                resp.sendRedirect(contextPath + "/login.xhtml?acesso=cliente");
                return;
            }
        }

        if (areaFuncionario && !"admin".equals(tipoUsuario)) {
            resp.sendRedirect(contextPath + "/cliente-menu.xhtml");
            return;
        }

        if (areaCliente && !"cliente".equals(tipoUsuario)) {
            resp.sendRedirect(contextPath + "/funcionario-menu.xhtml");
            return;
        }

        chain.doFilter(request, response);
    }

    private boolean isPaginaPublica(String pagina) {
        return paginasPublicas.contains(pagina);
    }

    private boolean isPaginaFuncionario(String pagina) {
        return paginasFuncionario.contains(pagina);
    }

    private boolean isPaginaCliente(String pagina) {
        return paginasCliente.contains(pagina);
    }

    private boolean isRecursoJSF(String pagina) {
        return pagina.contains("/jakarta.faces.resource/")
                || pagina.contains("/javax.faces.resource/");
    }
}