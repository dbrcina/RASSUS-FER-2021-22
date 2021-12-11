package hr.fer.tel.rassus.lab2.node.message;

import java.io.Serial;

public class AckMessage extends SocketMessage {

    @Serial
    private static final long serialVersionUID = 1337962199126122096L;

    private final int messageIdToBeAck;

    public AckMessage(int senderId, long scalarTimestamp, int messageIdToBeAck) {
        super(senderId, scalarTimestamp, Type.ACK);
        this.messageIdToBeAck = messageIdToBeAck;
    }

    public int getMessageIdToBeAck() {
        return messageIdToBeAck;
    }

}
