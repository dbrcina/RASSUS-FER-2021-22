package hr.fer.tel.rassus.lab2;

public class Sensor {

    private final int id;
    private final String address;
    private final int port;

    public Sensor(int id, String address, int port) {
        this.id = id;
        this.address = address;
        this.port = port;
    }

    public int getId() {
        return id;
    }

    public String getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

    @Override
    public String toString() {
        return "{\"id\":%d, \"address\":%s, \"port\":%d}".formatted(id, address, port);
    }

}
