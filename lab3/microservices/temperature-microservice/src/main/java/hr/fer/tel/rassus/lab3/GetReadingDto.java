package hr.fer.tel.rassus.lab3;

public class GetReadingDto {

    private String name;
    private String unit;
    private double value;

    public String getName() {
        return name;
    }

    public String getUnit() {
        return unit;
    }

    public double getValue() {
        return value;
    }

    public static GetReadingDto fromReading(Reading reading) {
        GetReadingDto getReadingDto = new GetReadingDto();
        getReadingDto.name = reading.getName();
        getReadingDto.unit = reading.getUnit();
        getReadingDto.value = reading.getValue();
        return getReadingDto;
    }

}

