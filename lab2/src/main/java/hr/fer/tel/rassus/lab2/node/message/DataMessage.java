package hr.fer.tel.rassus.lab2.node.message;

import java.io.Serial;

public class DataMessage extends SocketMessage {

    @Serial
    private static final long serialVersionUID = 769634279608093284L;

    private final double data;

    public DataMessage(int senderId, long scalarTimestamp, double data) {
        super(senderId, scalarTimestamp, Type.DATA);
        this.data = data;
    }

    public double getData() {
        return data;
    }

}
