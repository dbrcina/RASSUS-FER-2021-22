package hr.fer.tel.rassus.client;

import hr.fer.tel.rassus.client.dto.reading.RetrieveReadingDto;
import io.grpc.stub.StreamObserver;

import java.util.logging.Logger;

public class ReadingService extends ReadingGrpc.ReadingImplBase {

    private static final Logger LOGGER = Logger.getLogger(ReadingService.class.getName());

    // Sensor id that returns reading.
    private final long id;
    // Reading that needs to be returned.
    private RetrieveReadingDto retrieveReadingDto;

    public ReadingService(long id) {
        this.id = id;
    }

    public void setRetrieveReadingDto(RetrieveReadingDto retrieveReadingDto) {
        this.retrieveReadingDto = retrieveReadingDto;
    }

    @Override
    public void requestReading(InputMessage request, StreamObserver<OutputMessage> responseObserver) {
        LOGGER.info(String.format("%d: Got a reading request from sensor %d!", id, request.getSensorId()));

        // Build response
        OutputMessage.Builder builder = OutputMessage.newBuilder();
        builder.setTemperature(retrieveReadingDto.getTemperature());
        builder.setPressure(retrieveReadingDto.getPressure());
        builder.setHumidity(retrieveReadingDto.getHumidity());
        try {
            builder.setCo(retrieveReadingDto.getCo());
        } catch (NullPointerException ignored) {
        }
        try {
            builder.setNo2(retrieveReadingDto.getNo2());
        } catch (NullPointerException ignored) {
        }
        try {
            builder.setSo2(retrieveReadingDto.getSo2());
        } catch (NullPointerException ignored) {
        }

        // Send response
        responseObserver.onNext(builder.build());

        LOGGER.info(String.format("%d: Responding to sensor %d!", id, request.getSensorId()));
        responseObserver.onCompleted();
    }

}
