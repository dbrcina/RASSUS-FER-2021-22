package hr.fer.tel.rassus.client;

import hr.fer.tel.rassus.client.dto.reading.RetrieveReadingDto;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;

import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class SimpleUnaryRPCClient {

    private static final Logger LOGGER = Logger.getLogger(SimpleUnaryRPCClient.class.getName());

    private final int myPort;
    private final int port;
    private final ManagedChannel channel;
    private final ReadingGrpc.ReadingBlockingStub readingBlockingStub;

    public SimpleUnaryRPCClient(int myPort, String host, int port) {
        this.myPort = myPort;
        this.port = port;
        channel = ManagedChannelBuilder
                .forAddress(host, port)
                .usePlaintext()
                .build();
        readingBlockingStub = ReadingGrpc.newBlockingStub(channel);
    }

    public void stop() throws InterruptedException {
        // Initiates an orderly shutdown in which preexisting calls continue but new calls are
        // immediately cancelled. Waits for the channel to become terminated,
        // giving up if the timeout is reached.
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    public RetrieveReadingDto requestReading() {
        InputMessage request = InputMessage.newBuilder()
                .setSenderPort(myPort)
                .build();

        LOGGER.info(String.format("Request reading from a sensor at port %d!", port));

        try {
            OutputMessage response = readingBlockingStub.requestReading(request);
            RetrieveReadingDto reading = new RetrieveReadingDto();
            reading.setTemperature(response.getTemperature());
            reading.setPressure(response.getPressure());
            reading.setHumidity(response.getHumidity());
            // RPC will return 0 if an optional parameter was not provided.
            // It uses default value for double which is 0...
            // This is not a great design, but it works for this project.
            reading.setCo(response.getCo() == 0 ? null : response.getCo());
            reading.setNo2(response.getNo2() == 0 ? null : response.getNo2());
            reading.setSo2(response.getSo2() == 0 ? null : response.getSo2());
            LOGGER.info(String.format("Received reading from a sensor at port %d %s!", port, reading));
            return reading;
        } catch (StatusRuntimeException e) {
            LOGGER.severe(String.format("RPC to a sensor at port %d failed: %s!", port, e.getMessage()));
            return null;
        }
    }

}
