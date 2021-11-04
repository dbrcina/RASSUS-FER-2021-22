package hr.fer.tel.rassus.client;

import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class SimpleUnaryRPCServer {

    private static final Logger LOGGER = Logger.getLogger(SimpleUnaryRPCServer.class.getName());

    private Server server;
    private final ReadingService service;
    private final int port;

    public SimpleUnaryRPCServer(ReadingService service, int port) {
        this.service = service;
        this.port = port;
    }

    public void start() throws IOException {
        // Register the service
        server = ServerBuilder
                .forPort(port)
                .addService(service)
                .build()
                .start();

        LOGGER.info("Server started on " + port);

        //  Clean shutdown of server in case of JVM shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.err.println("Shutting down gRPC server since JVM is shutting down");
            try {
                SimpleUnaryRPCServer.this.stop();
            } catch (InterruptedException e) {
                e.printStackTrace(System.err);
            }
            System.err.println("Server shut down");
        }));
    }

    public void stop() throws InterruptedException {
        if (server != null) {
            server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
        }
    }

    public void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        final SimpleUnaryRPCServer server = new SimpleUnaryRPCServer(new ReadingService(1), 3000);
        server.start();
        server.blockUntilShutdown();
    }

}