package com.balancodoportuga.util;

import java.nio.file.Path;

public class UploadConfig {

    public static Path pastaVeiculos() {
        String pasta = Ambiente.get("UPLOAD_DIR", null);

        if (pasta != null) {
            return Path.of(pasta);
        }

        return Path.of(
                System.getProperty("user.home"),
                "BalancoDoPortugaWeb",
                "uploads",
                "veiculos"
        );
    }
}
