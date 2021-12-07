package hr.fer.tel.rassus.lab2;

import com.google.gson.Gson;

public final class Sensor {

    private static final Gson gson = new Gson();

    private int id;
    private String address;
    private int port;

    public Sensor(int id, String address, int port) {
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

    public static String toJson(Sensor sensor) {
        return gson.toJson(sensor);
    }

    public static Sensor fromJson(String json) {
        return gson.fromJson(json, Sensor.class);
    }

}
