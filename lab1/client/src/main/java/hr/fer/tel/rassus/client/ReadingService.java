package hr.fer.tel.rassus.client;

import hr.fer.tel.rassus.client.dto.reading.RetrieveReadingDto;
import io.grpc.stub.StreamObserver;

import java.util.function.Supplier;
import java.util.logging.Logger;

public class ReadingService extends ReadingGrpc.ReadingImplBase {

    private static final Logger LOGGER = Logger.getLogger(ReadingService.class.getName());

    private final Supplier<RetrieveReadingDto> readingSupplier;

    public ReadingService(Supplier<RetrieveReadingDto> readingSupplier) {
        this.readingSupplier = readingSupplier;
    }

    @Override
    public void requestReading(InputMessage request, StreamObserver<OutputMessage> responseObserver) {
        LOGGER.info(String.format("Got a reading request from a sensor at port %d!", request.getSenderPort()));

        // Get reading.
        RetrieveReadingDto retrieveReadingDto = readingSupplier.get();

        // Build response.
        OutputMessage.Builder builder = OutputMessage.newBuilder();
        builder.setTemperature(retrieveReadingDto.getTemperature());
        builder.setPressure(retrieveReadingDto.getPressure());
        builder.setHumidity(retrieveReadingDto.getHumidity());
        // Try-catch is necessary because gas reading can be null
        // and auto unboxing will throw an exception if it is null.
        // Exceptions are ignored because of optional type in .proto file.
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

        // Send response.
        responseObserver.onNext(builder.build());

        LOGGER.info(String.format("Responding to a sensor at port %d!", request.getSenderPort()));
        responseObserver.onCompleted();
    }

}
