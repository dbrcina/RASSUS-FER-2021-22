package hr.fer.tel.rassus.lab2.node.messages;

import java.io.*;

public record DataMessage(int id, double reading) implements Serializable {

    @Serial
    private static final long serialVersionUID = 9184067709671078489L;

    public static byte[] serialize(DataMessage dataMessage) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream objos = new ObjectOutputStream(bos)) {
            objos.writeObject(dataMessage);
            return bos.toByteArray();
        }
    }

    public static DataMessage deserialize(byte[] buf) throws IOException, ClassNotFoundException {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(buf);
             ObjectInputStream objis = new ObjectInputStream(bis)) {
            return (DataMessage) objis.readObject();
        }
    }

}
