package hr.fer.tel.rassus.lab2.node.message;

import hr.fer.tel.rassus.lab2.util.Vector;

import java.io.Serial;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class DataMessage extends SocketMessage {

    @Serial
    private static final long serialVersionUID = 769634279608093284L;

    private final double data;

    public DataMessage(int senderId, long scalarTimestamp, Vector vectorTimestamp, double data) {
        super(senderId, Type.DATA, scalarTimestamp, vectorTimestamp);
        this.data = data;
    }

    public double getData() {
        return data;
    }

    @Override
    public String toString() {
        return String.format(Locale.US,
                "%d: %7.1f, %d, %s",
                getSenderId(), data, TimeUnit.MILLISECONDS.toSeconds(getScalarTimestamp()), getVectorTimestamp());
    }

}
