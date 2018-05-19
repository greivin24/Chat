package com.example.chat.models;

public class Message {
    private String texto;
    private boolean emisor;

    public Message() {}

    public Message(String texto, Boolean emisor) {
        this.texto = texto;
        this.emisor = emisor;
    }

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }


    public boolean isSalida() {
        return emisor;
    }

    public void setSalida(boolean salida) {
        this.emisor = salida;
    }
}
