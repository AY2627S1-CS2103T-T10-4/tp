package seedu.address.model.group;

import static seedu.address.commons.util.AppUtil.checkArgument;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Objects;

/**
 * Represents an immutable configured group with a code, label and kind.
 */
public final class Group {

    /** Error message for blank group labels. */
    public static final String MESSAGE_CONSTRAINTS = "Group labels must not be blank.";

    private final GroupCode code;
    private final String label;
    private final GroupKind kind;

    /**
     * Creates a {@code Group} and preserves the supplied label.
     *
     * @param code The group's code.
     * @param label The group's non-blank label.
     * @param kind The group's kind.
     * @throws NullPointerException If any argument is null.
     * @throws IllegalArgumentException If {@code label} is blank.
     */
    public Group(GroupCode code, String label, GroupKind kind) {
        requireAllNonNull(code, label, kind);
        checkArgument(!label.isBlank(), MESSAGE_CONSTRAINTS);
        this.code = code;
        this.label = label;
        this.kind = kind;
    }

    /**
     * Returns the group's code.
     *
     * @return The group's canonical code.
     */
    public GroupCode getCode() {
        return code;
    }

    /**
     * Returns the label exactly as supplied at construction.
     *
     * @return The original group label.
     */
    public String getLabel() {
        return label;
    }

    /**
     * Returns the group's kind.
     *
     * @return The group's kind.
     */
    public GroupKind getKind() {
        return kind;
    }

    /**
     * Returns true if both groups have the same code, label and kind.
     */
    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof Group otherGroup)) {
            return false;
        }

        return code.equals(otherGroup.code)
                && label.equals(otherGroup.label)
                && kind == otherGroup.kind;
    }

    /**
     * Returns a hash code based on the group's code, label and kind.
     */
    @Override
    public int hashCode() {
        return Objects.hash(code, label, kind);
    }
}
