package hr.fer.tel.rassus.lab2.node.message;

import java.io.*;

public abstract class SocketMessage implements Serializable {

    @Serial
    private static final long serialVersionUID = 7163353894428761969L;

    private static int messageCounter;

    public enum Type {
        ACK, DATA
    }

    private final int senderId;
    private final Type type;
    private final int messageId;
    private long scalarTimestamp;

    protected SocketMessage(int senderId, long scalarTimestamp, Type type) {
        this.senderId = senderId;
        this.scalarTimestamp = scalarTimestamp;
        this.type = type;
        messageId = messageCounter++;
    }

    public int getSenderId() {
        return senderId;
    }

    public long getScalarTimestamp() {
        return scalarTimestamp;
    }

    public void setScalarTimestamp(long scalarTimestamp) {
        this.scalarTimestamp = scalarTimestamp;
    }

    public Type getType() {
        return type;
    }

    public int getMessageId() {
        return messageId;
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