package hr.fer.tel.rassus.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;

import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class SimpleUnaryRPCClient {

    private static final Logger LOGGER = Logger.getLogger(SimpleUnaryRPCClient.class.getName());

    private final ManagedChannel channel;
    private final ReadingGrpc.ReadingBlockingStub readingBlockingStub;

    public SimpleUnaryRPCClient(String host, int port) {
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

    public void requestReading() {
        InputMessage request = InputMessage.newBuilder()
                .setSensorId(2)
                .build();

        LOGGER.info(String.format("%d: Request reading from sensor %d!", 2, 1));

        try {
            OutputMessage response = readingBlockingStub.requestReading(request);
            LOGGER.info(String.format("%d: Received reading from sensor %d!", 2, 1));
        } catch (StatusRuntimeException e) {
            LOGGER.severe(String.format("%d: RPC to sensor %d failed: %s!", 2, 1, e.getMessage()));
        }
    }

    public static void main(String[] args) throws InterruptedException {
        SimpleUnaryRPCClient client = new SimpleUnaryRPCClient("127.0.0.1", 3000);
        client.requestReading();
        client.stop();
    }

}
