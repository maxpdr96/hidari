package com.hidarisoft.hidari.utils;

public enum Constants {
    DOWNLOAD_FOLDER("E:\\Animes\\"),
    LANG_BR("pt-BR");
    private String description;

    Constants(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
