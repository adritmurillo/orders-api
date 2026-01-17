package com.dinet.orders_api.domain.model;

public class Zona {
    private String id;
    private boolean soporteRefrigeracion;

    public Zona(){

    }

    public Zona(String id, boolean soporteRefrigeracion){
        this.id = id;
        this.soporteRefrigeracion = soporteRefrigeracion;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isSoporteRefrigeracion() {
        return soporteRefrigeracion;
    }

    public void setSoporteRefrigeracion(boolean soporteRefrigeracion) {
        this.soporteRefrigeracion = soporteRefrigeracion;
    }
}
