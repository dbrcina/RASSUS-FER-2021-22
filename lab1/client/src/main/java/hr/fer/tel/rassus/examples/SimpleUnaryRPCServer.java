package hr.fer.tel.rassus.examples;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;


/**
 * The type Simple unary rpc server.
 */
public class SimpleUnaryRPCServer {
  private static final Logger logger = Logger.getLogger(SimpleUnaryRPCServer.class.getName());

  private Server server;
  private final UppercaseService service;
  private final int port;

  /**
   * Instantiates a new Simple unary rpc server.
   *
   * @param service the service
   * @param port    the port
   */
  public SimpleUnaryRPCServer(UppercaseService service, int port) {
    this.service = service;
    this.port = port;
  }

  /**
   * Start the server.
   *
   * @throws IOException the io exception
   */
  public void start() throws IOException {
    // Register the service
    server = ServerBuilder.forPort(port)
        .addService(service)
        .build()
        .start();
    logger.info("Server started on " + port);

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

  /**
   * Stops the server.
   *
   * @throws InterruptedException the interrupted exception
   */
  public void stop() throws InterruptedException {
    if (server != null) {
      server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
    }
  }

  /**
   * Await termination on the main thread
   *
   * @throws InterruptedException the interrupted exception
   */
  public void blockUntilShutdown() throws InterruptedException {
    if (server != null) {
      server.awaitTermination();
    }
  }

  /**
   * The entry point of application.
   *
   * @param args the input arguments
   * @throws IOException          the io exception
   * @throws InterruptedException the interrupted exception
   */
  public static void main(String[] args) throws IOException, InterruptedException {
    final SimpleUnaryRPCServer server = new SimpleUnaryRPCServer(new UppercaseService(), 3000);
    server.start();
    server.blockUntilShutdown();
  }
}