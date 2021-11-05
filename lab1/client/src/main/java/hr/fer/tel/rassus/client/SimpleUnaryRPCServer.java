package hr.fer.tel.rassus.client;

import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class SimpleUnaryRPCServer {

    private static final Logger LOGGER = Logger.getLogger(SimpleUnaryRPCServer.class.getName());

    private final ReadingService service;
    private final int port;
    private Server server;

    public SimpleUnaryRPCServer(ReadingService service, int port) {
        this.service = service;
        this.port = port;
    }

    public void start() throws IOException {
        // Register the service.
        server = ServerBuilder
                .forPort(port)
                .addService(service)
                .build()
                .start();

        LOGGER.info(String.format("Server started on port %d!", port));

        //  Clean shutdown of server in case of JVM shutdown.
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.err.println("Shutting down gRPC server since JVM is shutting down!");
            try {
                stop();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.err.println("Server shut down!");
        }));
    }

    public void stop() throws InterruptedException {
        if (server != null) {
            server.shutdown().awaitTermination(10, TimeUnit.SECONDS);
        }
    }

}