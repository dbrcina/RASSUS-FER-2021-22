package hr.fer.tel.rassus.lab2.util;

import java.io.Serial;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Map;
import java.util.StringJoiner;
import java.util.TreeMap;

public class Vector implements Comparable<Vector>, Serializable {

    @Serial
    private static final long serialVersionUID = 6324089846823106778L;

    private final Map<Integer, Integer> mapper;

    public Vector(int[] ids) {
        mapper = new TreeMap<>();
        for (int id : ids) {
            mapper.put(id, 0);
        }
    }

    private Vector(Map<Integer, Integer> mapper) {
        this.mapper = new TreeMap<>(mapper);
    }

    public synchronized Vector update(int idToIncrement, Vector other, boolean returnNew) {
        if (other == null) {
            mapper.merge(idToIncrement, 1, Integer::sum);
        } else {
            Map<Integer, Integer> otherMapper = other.mapper;
            for (int id : mapper.keySet()) {
                mapper.merge(id, 1, (oldVal, newVal) -> id == idToIncrement
                        ? oldVal + 1
                        : Math.max(oldVal, otherMapper.get(id))
                );
            }
        }
        return returnNew ? new Vector(mapper) : this;
    }

    @Override
    public int compareTo(Vector other) {
        return Arrays.compare(mapper.values().toArray(new Integer[0]), other.mapper.values().toArray(new Integer[0]));
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(", ", "[", "]");
        mapper.forEach((key, value) -> sj.add("%d->%3d".formatted(key, value)));
        return sj.toString();
    }

}
