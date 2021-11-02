package hr.fer.tel.rassus.server.dto.sensor;

public class RegisterSensorDto {

    private final double latitude;
    private final double longitude;
    private final String ip;
    private final int port;

    public RegisterSensorDto(double latitude, double longitude, String ip, int port) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.ip = ip;
        this.port = port;
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
