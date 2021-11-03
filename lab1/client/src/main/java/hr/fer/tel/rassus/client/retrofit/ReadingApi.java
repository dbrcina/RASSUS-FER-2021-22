package hr.fer.tel.rassus.client.retrofit;

import hr.fer.tel.rassus.client.dto.reading.RegisterReadingDto;
import hr.fer.tel.rassus.client.dto.reading.RetrieveReadingDto;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

import java.util.Collection;

public interface ReadingApi {

    @POST("/readings/{sensorId}")
    Call<Void> registerReading(@Path("sensorId") long sensorId, @Body RegisterReadingDto registerReadingDto);

    @GET("/readings/{sensorId}")
    Call<Collection<RetrieveReadingDto>> retrieveReadings(@Path("sensorId") long sensorId);

}
