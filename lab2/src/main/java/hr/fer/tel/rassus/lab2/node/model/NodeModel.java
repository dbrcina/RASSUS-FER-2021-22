package hr.fer.tel.rassus.lab2.node.model;

import com.google.gson.Gson;

import java.util.Objects;

public final class NodeModel {

    private static final Gson gson = new Gson();

    private int id;
    private String address;
    private int port;

    public NodeModel(int id, String address, int port) {
        this.id = id;
        this.address = address;
        this.port = port;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NodeModel sensor)) return false;
        return id == sensor.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public static String toJson(NodeModel sensor) {
        return gson.toJson(sensor);
    }

    public static NodeModel fromJson(String json) {
        return gson.fromJson(json, NodeModel.class);
    }

}