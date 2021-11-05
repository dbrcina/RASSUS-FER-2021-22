package hr.fer.tel.rassus.client.retrofit;

import hr.fer.tel.rassus.client.dto.reading.RegisterReadingDto;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ReadingApi {

    @POST("/readings/{sensorId}")
    Call<Void> registerReading(@Path("sensorId") long sensorId, @Body RegisterReadingDto registerReadingDto);

}
