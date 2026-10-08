package seedu.address.model.student;

import java.util.Locale;

/** One of the supported secondary or integrated-programme levels. */
public final class AcademicLevel {
    public static final String MESSAGE_CONSTRAINTS = "Academic level must be Sec 1-5 or IP 1-6.";
    private final String value;
    public AcademicLevel(String input) {
        String normalized = input == null ? "" : input.trim().replaceAll("\\s+", " ").toUpperCase(Locale.ROOT);
        if (!normalized.matches("(?:SEC [1-5]|IP [1-6])")) { throw new IllegalArgumentException(MESSAGE_CONSTRAINTS); }
        value = (normalized.startsWith("SEC") ? "Sec" : "IP") + normalized.substring(normalized.indexOf(' '));
    }
    @Override public String toString() { return value; }
    @Override
    public boolean equals(Object other) {
        return other instanceof AcademicLevel level && value.equals(level.value);
    }

    @Override
    public int hashCode() { return value.hashCode(); }
}
