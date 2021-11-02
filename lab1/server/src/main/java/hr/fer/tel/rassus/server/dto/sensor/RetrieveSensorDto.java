package hr.fer.tel.rassus.server.dto.sensor;

public class RetrieveSensorDto {

    private final long id;
    private final double latitude;
    private final double longitude;
    private final String ip;
    private final int port;

    public RetrieveSensorDto(long id, double latitude, double longitude, String ip, int port) {
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.ip = ip;
        this.port = port;
    }

    public long getId() {
        return id;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

}
