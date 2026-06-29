package com.balancodoportuga.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import com.balancodoportuga.util.UploadConfig;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@WebServlet("/imagens/veiculos/*")
public class ImagemVeiculoServlet extends HttpServlet {

    private Path pastaUpload() {
    return UploadConfig.pastaVeiculos();
}

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String arquivo = request.getPathInfo();

        if (arquivo == null || arquivo.equals("/") || arquivo.contains("..")) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        arquivo = arquivo.substring(1);

        Path caminhoImagem = pastaUpload().resolve(arquivo).normalize();

        if (!Files.exists(caminhoImagem) || !Files.isRegularFile(caminhoImagem)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String contentType = Files.probeContentType(caminhoImagem);

        if (contentType == null || !contentType.startsWith("image/")) {
            response.sendError(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
            return;
        }

        response.setContentType(contentType);
        response.setContentLengthLong(Files.size(caminhoImagem));

        try (OutputStream out = response.getOutputStream()) {
            Files.copy(caminhoImagem, out);
        }
    }
}