package seedu.address.model.guardian;

import java.text.Normalizer;
import java.util.Locale;

/** A normalized, validated guardian name retaining supplied casing. */
public final class GuardianName implements Comparable<GuardianName> {
    public static final String MESSAGE_CONSTRAINTS =
            "Name must be 1-80 characters and contain letters, spaces, apostrophes, hyphens, or periods only.";
    private static final int MAX_LENGTH = 80;

    private final String value;
    private final String normalized;

    /** Creates a normalized name, retaining the supplied letter casing for display. */
    public GuardianName(String value) {
        String cleaned = value == null ? "" : value.trim().replaceAll("\\s+", " ");
        if (!isValid(cleaned)) {
            throw new IllegalArgumentException(MESSAGE_CONSTRAINTS);
        }
        this.value = cleaned;
        normalized = Normalizer.normalize(cleaned, Normalizer.Form.NFKC).toLowerCase(Locale.ROOT);
    }

    /** Returns whether the value follows the shared name character and length rules. */
    public static boolean isValid(String value) {
        return value != null && value.codePointCount(0, value.length()) <= MAX_LENGTH
                && value.matches("(?U)\\p{L}+(?:(?:[ '-]|\\.\\s?)\\p{L}+)*");
    }

    public String getNormalized() {
        return normalized;
    }

    @Override
    public int compareTo(GuardianName other) {
        return normalized.compareTo(other.normalized);
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof GuardianName name && normalized.equals(name.normalized);
    }

    @Override
    public int hashCode() {
        return normalized.hashCode();
    }
}
