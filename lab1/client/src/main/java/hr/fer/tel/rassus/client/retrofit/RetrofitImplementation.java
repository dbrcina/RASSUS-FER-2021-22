package hr.fer.tel.rassus.client.retrofit;

import hr.fer.tel.rassus.client.RestInterface;
import hr.fer.tel.rassus.client.dto.reading.RegisterReadingDto;
import hr.fer.tel.rassus.client.dto.sensor.RegisterSensorDto;
import hr.fer.tel.rassus.client.dto.sensor.RetrieveSensorDto;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.io.IOException;
import java.util.logging.Logger;

public class RetrofitImplementation implements RestInterface {

    private static final Logger LOGGER = Logger.getLogger(RetrofitImplementation.class.getName());

    private final SensorApi sensorApi;
    private final ReadingApi readingApi;

    public RetrofitImplementation(String url) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(JacksonConverterFactory.create())
                .build();
        sensorApi = retrofit.create(SensorApi.class);
        readingApi = retrofit.create(ReadingApi.class);
    }

    @Override
    public long registerSensor(RegisterSensorDto registerSensorDto) {
        try {
            Response<Void> response = sensorApi.registerSensor(registerSensorDto).execute();
            if (response.code() != 201) {
                LOGGER.severe("Response for registerSensor endpoint isn't successful! Exiting this program...");
                System.exit(-1);
            }
            String location = response.headers().get("Location");
            return Long.parseLong(location.substring(location.lastIndexOf("/") + 1));
        } catch (IOException | NullPointerException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public RetrieveSensorDto retrieveClosestSensor(long id) {
        try {
            return sensorApi.retrieveClosestSensor(id).execute().body();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public long registerReading(long sensorId, RegisterReadingDto registerReadingDto) {
        try {
            Response<Void> response = readingApi.registerReading(sensorId, registerReadingDto).execute();
            if (response.code() != 201) {
                LOGGER.severe("Response for registerReading isn't successful! Returning invalid id 0!");
                return 0;
            }
            String location = response.headers().get("Location");
            return Long.parseLong(location.substring(location.lastIndexOf("/") + 1));
        } catch (IOException | NullPointerException e) {
            throw new RuntimeException(e);
        }
    }

}
