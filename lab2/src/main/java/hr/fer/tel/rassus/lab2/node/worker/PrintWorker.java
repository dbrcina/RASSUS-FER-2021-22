package hr.fer.tel.rassus.lab2.node.worker;

import hr.fer.tel.rassus.lab2.node.message.DataMessage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public class PrintWorker implements Runnable {

    private static int invokedCounter;

    private final Collection<DataMessage> tempMessages;
    private final Collection<DataMessage> scalarTimestampSorted;
    private final Collection<DataMessage> vectorTimestampSorted;

    public PrintWorker(
            Collection<DataMessage> tempMessages,
            Collection<DataMessage> scalarTimestampSorted,
            Collection<DataMessage> vectorTimestampSorted) {
        this.tempMessages = tempMessages;
        this.scalarTimestampSorted = scalarTimestampSorted;
        this.vectorTimestampSorted = vectorTimestampSorted;
    }

    @Override
    public void run() {
        invokedCounter++;
        List<DataMessage> copied = new ArrayList<>(tempMessages);
        tempMessages.clear();

        StringBuilder sb = new StringBuilder();
        sb.append("-------------------------------------\n");
        sb.append("Print counter: ").append(invokedCounter);

        sb.append("\n\n");

        // Average
        double avg = copied.stream().mapToDouble(DataMessage::getData).sum() / copied.size();
        sb.append("Average reading: ").append(avg);

        sb.append("\n\n");

        // Scalar timestamps
        sb.append("Sorted by scalar timestamps:\n");
        copied.sort(Comparator.comparingLong(DataMessage::getScalarTimestamp));
        copied.forEach(m -> sb.append(m).append("\n"));
        scalarTimestampSorted.addAll(copied);

        sb.append("\n\n");

        // Vector timestamps
        sb.append("Sorted by vector timestamps:\n");
        copied.sort(Comparator.comparing(DataMessage::getVectorTimestamp));
        copied.forEach(m -> sb.append(m).append("\n"));
        vectorTimestampSorted.addAll(copied);

        sb.append("-------------------------------------\n");
        System.out.println(sb);
    }

}
