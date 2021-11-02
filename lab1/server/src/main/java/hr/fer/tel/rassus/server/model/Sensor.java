package hr.fer.tel.rassus.server.model;

import javax.persistence.*;
import java.util.Set;

@Entity
public class Sensor {

    @Id
    @GeneratedValue
    private long id;
    private double latitude;
    private double longitude;
    private String ip;
    private int port;
    @OneToMany(mappedBy = "sensor", cascade = CascadeType.ALL)
    private Set<Reading> readings;

    public long getId() {
        return id;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public Set<Reading> getReadings() {
        return readings;
    }

}
