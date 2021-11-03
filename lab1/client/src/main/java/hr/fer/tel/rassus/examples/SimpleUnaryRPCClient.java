package hr.fer.tel.rassus.examples;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * The type Simple unary rpc client.
 */
public class SimpleUnaryRPCClient {

  private static final Logger logger = Logger.getLogger(SimpleUnaryRPCClient.class.getName());


  private final ManagedChannel channel;
  private final UppercaseGrpc.UppercaseBlockingStub uppercaseBlockingStub;

  /**
   * Instantiates a new Simple unary rpc client.
   *
   * @param host the host
   * @param port the port
   */
  public SimpleUnaryRPCClient(String host, int port) {
    this.channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();

    uppercaseBlockingStub = UppercaseGrpc.newBlockingStub(channel);

  }

  /**
   * Stop the client.
   *
   * @throws InterruptedException the interrupted exception
   */
  public void stop() throws InterruptedException {
//    Initiates an orderly shutdown in which preexisting calls continue but new calls are
//    immediately cancelled. Waits for the channel to become terminated, giving up if the timeout
//    is reached.
    channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
  }

  /**
   * Request uppercase.
   */
  public void requestUppercase() {

    final String payload = "message";

    Message request = Message.newBuilder()
        .setPayload(payload)
        .build();

    logger.info("Sending: " + request.getPayload());
    try {
      Message response = uppercaseBlockingStub.requestUppercase(request);
      logger.info("Received: " + response.getPayload());
    } catch (StatusRuntimeException e) {
      logger.info("RPC failed: " + e.getMessage());
    }
  }


  /**
   * The entry point of application.
   *
   * @param args the input arguments
   * @throws InterruptedException the interrupted exception
   */
  public static void main(String[] args) throws InterruptedException {
    SimpleUnaryRPCClient client = new SimpleUnaryRPCClient("127.0.0.1", 3000);

    client.requestUppercase();

    client.stop();
  }
}
