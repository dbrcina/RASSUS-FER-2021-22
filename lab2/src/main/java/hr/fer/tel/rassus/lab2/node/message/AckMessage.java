package hr.fer.tel.rassus.lab2.node.message;

import hr.fer.tel.rassus.lab2.util.Vector;

import java.io.Serial;

public class AckMessage extends SocketMessage {

    @Serial
    private static final long serialVersionUID = 1337962199126122096L;

    private final int messageIdToBeAck;

    public AckMessage(int senderId, long scalarTimestamp, Vector vectorTimestamp, int messageIdToBeAck) {
        super(senderId, Type.ACK, scalarTimestamp, vectorTimestamp);
        this.messageIdToBeAck = messageIdToBeAck;
    }

    public int getMessageIdToBeAck() {
        return messageIdToBeAck;
    }

}
