package hr.fer.tel.rassus.lab2.util;

import java.util.Objects;

public class Pair<T1, T2> {

    private T1 v1;
    private T2 v2;

    public Pair(T1 v1, T2 v2) {
        this.v1 = v1;
        this.v2 = v2;
    }

    public T1 getV1() {
        return v1;
    }

    public void setV1(T1 v1) {
        this.v1 = v1;
    }

    public T2 getV2() {
        return v2;
    }

    public void setV2(T2 v2) {
        this.v2 = v2;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Pair<?, ?> pair)) return false;
        return v1.equals(pair.v1) && v2.equals(pair.v2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(v1, v2);
    }

}
