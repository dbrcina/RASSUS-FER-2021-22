package hr.fer.tel.rassus.lab2.node.communication.messages;

import java.io.Serial;
import java.io.Serializable;

public record DataMessage(int id, double reading) implements Serializable {

    @Serial
    private static final long serialVersionUID = 9184067709671078489L;

}
