package seedu.address.model.student;

import java.text.Normalizer;

/** A normalized, validated student name retaining supplied casing. */
public final class StudentName implements Comparable<StudentName> {
    public static final String MESSAGE_CONSTRAINTS =
            "Name must be 1-80 characters and contain letters, spaces, apostrophes, hyphens, or periods only.";
    private final String value;
    private final String normalized;

    public StudentName(String value) {
        String cleaned = value == null ? "" : value.trim().replaceAll("\\s+", " ");
        if (!isValid(cleaned)) { throw new IllegalArgumentException(MESSAGE_CONSTRAINTS); }
        this.value = cleaned;
        this.normalized = Normalizer.normalize(cleaned, Normalizer.Form.NFKC).toLowerCase(java.util.Locale.ROOT);
    }

    public static boolean isValid(String value) {
        return value != null && value.length() <= 80
                && value.matches("(?U)\\p{L}+(?:(?:[ '-]|\\.\\s?)\\p{L}+)*");
    }
    public String normalized() { return normalized; }
    @Override public String toString() { return value; }
    @Override public int compareTo(StudentName other) { return normalized.compareTo(other.normalized); }
    @Override
    public boolean equals(Object other) {
        return other instanceof StudentName name && normalized.equals(name.normalized);
    }
    @Override public int hashCode() { return normalized.hashCode(); }
}
