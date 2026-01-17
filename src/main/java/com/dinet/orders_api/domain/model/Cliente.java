package com.dinet.orders_api.domain.model;

public class Cliente {
    private String id;
    private boolean activo;

    public Cliente(){

    }

    public Cliente(String id, boolean activo){
        this.id = id;
        this.activo = activo;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}
