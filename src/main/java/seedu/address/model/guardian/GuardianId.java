package seedu.address.model.guardian;

import java.util.Locale;
import java.util.concurrent.atomic.AtomicLong;

/** Stable identifier for a guardian. */
public final class GuardianId implements Comparable<GuardianId> {
    private static final AtomicLong NEXT = new AtomicLong(1);
    private final String value;

    /** Creates and validates a guardian ID, advancing the generator past its numeric value. */
    public GuardianId(String value) {
        if (value == null || !value.matches("G-[0-9]{4,}")) {
            throw new IllegalArgumentException("Guardian ID must have the form G-0001.");
        }
        this.value = value;
        NEXT.accumulateAndGet(Long.parseLong(value.substring(2)) + 1, Math::max);
    }

    /** Returns a unique ID in the {@code G-0001} format. */
    public static GuardianId generate() {
        return new GuardianId(String.format(Locale.ROOT, "G-%04d", NEXT.getAndIncrement()));
    }

    @Override
    public int compareTo(GuardianId other) {
        return Long.compare(Long.parseLong(value.substring(2)), Long.parseLong(other.value.substring(2)));
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof GuardianId id && value.equals(id.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
