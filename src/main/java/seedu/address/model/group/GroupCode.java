package seedu.address.model.group;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.util.Locale;

/**
 * Represents an immutable group code with case-insensitive identity.
 * Catalogue membership is validated separately.
 */
public final class GroupCode {

    /** Error message for blank group codes. */
    public static final String MESSAGE_CONSTRAINTS = "Group codes must not be blank.";

    private final String value;

    /**
     * Creates a {@code GroupCode} with surrounding whitespace removed and letters normalized to uppercase.
     *
     * @param code A non-blank group code.
     * @throws NullPointerException If {@code code} is null.
     * @throws IllegalArgumentException If {@code code} is blank.
     */
    public GroupCode(String code) {
        requireNonNull(code);
        String trimmedCode = code.strip();
        checkArgument(!trimmedCode.isEmpty(), MESSAGE_CONSTRAINTS);
        value = canonicalize(trimmedCode);
    }

    /**
     * Returns the canonical uppercase group code.
     *
     * @return The canonical uppercase code.
     */
    public String getValue() {
        return value;
    }

    /**
     * Returns the canonical group code.
     */
    @Override
    public String toString() {
        return value;
    }

    /**
     * Returns true if both group codes have the same canonical value.
     */
    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof GroupCode otherCode)) {
            return false;
        }

        return value.equals(otherCode.value);
    }

    /**
     * Returns a hash code based on the canonical group code.
     */
    @Override
    public int hashCode() {
        return value.hashCode();
    }

    /**
     * Returns an uppercase code after folding Unicode case variants.
     * Uses the character mapping from {@link String#equalsIgnoreCase(String)} before uppercasing,
     * so both lowercase and uppercase sharp s become {@code SS}.
     */
    private static String canonicalize(String code) {
        StringBuilder foldedCode = new StringBuilder();
        code.codePoints()
                .map(Character::toUpperCase)
                .map(Character::toLowerCase)
                .forEach(foldedCode::appendCodePoint);
        return foldedCode.toString().toUpperCase(Locale.ROOT);
    }
}
