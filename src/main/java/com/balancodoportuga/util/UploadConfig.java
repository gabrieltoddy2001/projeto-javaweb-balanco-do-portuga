package com.balancodoportuga.util;

import java.nio.file.Path;

public class UploadConfig {

    public static Path pastaVeiculos() {
        return Path.of(
                System.getProperty("user.home"),
                "BalancoDoPortugaWeb",
                "uploads",
                "veiculos"
        );
    }
}