package hr.fer.tel.rassus.lab2.node.message;

import hr.fer.tel.rassus.lab2.util.Vector;

import java.io.*;
import java.util.Objects;

public abstract class SocketMessage implements Serializable {

    @Serial
    private static final long serialVersionUID = 7163353894428761969L;

    private static int messageCounter;

    public enum Type {
        ACK, DATA
    }

    private final int messageId;
    private final int senderId;
    private final Type type;
    private long scalarTimestamp;
    private Vector vectorTimestamp;

    protected SocketMessage(int senderId, Type type, long scalarTimestamp, Vector vectorTimestamp) {
        messageId = messageCounter++;
        this.senderId = senderId;
        this.type = type;
        this.scalarTimestamp = scalarTimestamp;
        this.vectorTimestamp = vectorTimestamp;
    }

    public int getMessageId() {
        return messageId;
    }

    public int getSenderId() {
        return senderId;
    }

    public Type getType() {
        return type;
    }

    public long getScalarTimestamp() {
        return scalarTimestamp;
    }

    public void setScalarTimestamp(long scalarTimestamp) {
        this.scalarTimestamp = scalarTimestamp;
    }

    public Vector getVectorTimestamp() {
        return vectorTimestamp;
    }

    public void setVectorTimestamp(Vector vectorTimestamp) {
        this.vectorTimestamp = vectorTimestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SocketMessage that)) return false;
        return messageId == that.messageId && senderId == that.senderId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(messageId, senderId);
    }

    public static byte[] serialize(SocketMessage m) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream objos = new ObjectOutputStream(bos)) {
            objos.writeObject(m);
            return bos.toByteArray();
        }
    }

    public static SocketMessage deserialize(byte[] buf) throws IOException, ClassNotFoundException {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(buf);
             ObjectInputStream objis = new ObjectInputStream(bis)) {
            return (SocketMessage) objis.readObject();
        }
    }

}
