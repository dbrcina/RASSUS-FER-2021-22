package hr.fer.tel.rassus.lab2.network;

import java.util.function.Supplier;

public final class OffsetBasedEmulatedSystemClock extends EmulatedSystemClock {

    private final EmulatedSystemClock clock;
    private final Supplier<Long> offsetSupplier;

    public OffsetBasedEmulatedSystemClock(EmulatedSystemClock clock, Supplier<Long> offsetSupplier) {
        this.clock = clock;
        this.offsetSupplier = offsetSupplier;
    }

    @Override
    public long currentTimeMillis() {
        return clock.currentTimeMillis() + offsetSupplier.get();
    }

}
