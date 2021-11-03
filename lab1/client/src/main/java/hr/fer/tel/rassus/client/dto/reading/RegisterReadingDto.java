package hr.fer.tel.rassus.client.dto.reading;

public class RegisterReadingDto {

    private final double temperature;
    private final double pressure;
    private final double humidity;
    private final Double co;
    private final Double no2;
    private final Double so2;

    public RegisterReadingDto(
            double temperature, double pressure, double humidity, Double co, Double no2, Double so2) {
        this.temperature = temperature;
        this.pressure = pressure;
        this.humidity = humidity;
        this.co = co;
        this.no2 = no2;
        this.so2 = so2;
    }

    public double getTemperature() {
        return temperature;
    }

    public double getPressure() {
        return pressure;
    }

    public double getHumidity() {
        return humidity;
    }

    public Double getCo() {
        return co;
    }

    public Double getNo2() {
        return no2;
    }

    public Double getSo2() {
        return so2;
    }

}
