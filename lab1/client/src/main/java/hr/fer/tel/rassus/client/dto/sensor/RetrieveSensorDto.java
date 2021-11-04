package hr.fer.tel.rassus.client.dto.sensor;

public class RetrieveSensorDto {

    private Long id;
    private double latitude;
    private double longitude;
    private String ip;
    private int port;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    @Override
    public String toString() {
        return "{" +
                (id != null ? "id=" + id + ", " : "") +
                "latitude=" + latitude +
                ", longitude=" + longitude +
                ", ip='" + ip + '\'' +
                ", port=" + port +
                '}';
    }

}
