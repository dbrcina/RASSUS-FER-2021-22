package hr.fer.tel.rassus.client.dto.reading;

public class RetrieveReadingDto {

    private Long id;
    private double temperature;
    private double pressure;
    private double humidity;
    private Double co;
    private Double no2;
    private Double so2;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public double getPressure() {
        return pressure;
    }

    public void setPressure(double pressure) {
        this.pressure = pressure;
    }

    public double getHumidity() {
        return humidity;
    }

    public void setHumidity(double humidity) {
        this.humidity = humidity;
    }

    public Double getCo() {
        return co;
    }

    public void setCo(Double co) {
        this.co = co;
    }

    public Double getNo2() {
        return no2;
    }

    public void setNo2(Double no2) {
        this.no2 = no2;
    }

    public Double getSo2() {
        return so2;
    }

    public void setSo2(Double so2) {
        this.so2 = so2;
    }

    @Override
    public String toString() {
        return "RetrieveReadingDto{" +
                (id != null ? "id=" + id : "") +
                ", temperature=" + temperature +
                ", pressure=" + pressure +
                ", humidity=" + humidity +
                (co != null ? ", co=" + co : "") +
                (no2 != null ? ", no2=" + no2 : "") +
                (so2 != null ? ", so2=" + so2 : "") +
                '}';
    }

}
