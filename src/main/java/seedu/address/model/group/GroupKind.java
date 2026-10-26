package seedu.address.model.group;

/**
 * Represents the kind of a configured group.
 */
public enum GroupKind {
    /** A configured subject group. */
    SUBJECT,

    /** A configured class group. */
    CLASS;

    /** Returns the user-facing canonical spelling. */
    public String toDisplayString() {
        return switch (this) {
            case SUBJECT -> "Subject";
            case CLASS -> "Class";
        };
    }
}
