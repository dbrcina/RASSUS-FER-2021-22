package hr.fer.tel.rassus.client.retrofit;

import hr.fer.tel.rassus.client.dto.sensor.RegisterSensorDto;
import hr.fer.tel.rassus.client.dto.sensor.RetrieveSensorDto;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

import java.util.Collection;

public interface SensorApi {

    @POST("/sensors")
    Call<Void> registerSensor(@Body RegisterSensorDto registerSensorDto);

    @GET("/sensors/closest/{id}")
    Call<RetrieveSensorDto> retrieveClosestSensor(@Path("id") long id);

    @GET("/sensors")
    Call<Collection<RetrieveSensorDto>> retrieveSensors();

}
