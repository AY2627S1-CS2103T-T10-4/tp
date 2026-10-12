package seedu.address.model.student;

import java.util.Locale;
import java.util.concurrent.atomic.AtomicLong;

/** Stable identifier for a student. */
public final class StudentId implements Comparable<StudentId> {
    private static final AtomicLong NEXT = new AtomicLong(1);
    public final String value;

    /** Creates and validates a student ID, advancing the generator past its numeric value. */
    public StudentId(String value) {
        if (value == null || !value.matches("S-[0-9]{4,}")) {
            throw new IllegalArgumentException("Student ID must have the form S-0001.");
        }
        this.value = value;
        NEXT.accumulateAndGet(Long.parseLong(value.substring(2)) + 1, Math::max);
    }

    /** Returns a unique ID in the {@code S-0001} format. */
    public static StudentId generate() {
        return new StudentId(String.format(Locale.ROOT, "S-%04d", NEXT.getAndIncrement()));
    }

    @Override
    public String toString() {
        return value;
    }

    @Override public int compareTo(StudentId other) {
        return Long.compare(Long.parseLong(value.substring(2)), Long.parseLong(other.value.substring(2)));
    }
    @Override
    public boolean equals(Object other) {
        return other instanceof StudentId id && value.equals(id.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
