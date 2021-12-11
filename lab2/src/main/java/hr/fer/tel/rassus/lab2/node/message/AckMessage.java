package hr.fer.tel.rassus.lab2.node.message;

import java.io.Serial;

public final class AckMessage extends SocketMessage {

    @Serial
    private static final long serialVersionUID = 1337962199126122096L;

    private final int ackMessageId;

    public AckMessage(int senderId, int ackMessageId) {
        super(senderId, Type.ACK);
        this.ackMessageId = ackMessageId;
    }

}
