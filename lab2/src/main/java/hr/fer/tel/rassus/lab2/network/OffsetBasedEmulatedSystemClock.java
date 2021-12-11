package hr.fer.tel.rassus.lab2.network;

import java.util.concurrent.atomic.AtomicLong;

public class OffsetBasedEmulatedSystemClock extends EmulatedSystemClock {

    private final EmulatedSystemClock clock;
    private final AtomicLong offset;

    public OffsetBasedEmulatedSystemClock(EmulatedSystemClock clock, AtomicLong offset) {
        this.clock = clock;
        this.offset = offset;
    }

    @Override
    public long currentTimeMillis() {
        return clock.currentTimeMillis() + offset.get();
    }

}
