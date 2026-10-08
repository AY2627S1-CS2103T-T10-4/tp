package seedu.address.model.group;

import static seedu.address.commons.util.AppUtil.checkArgument;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Represents an immutable catalogue of groups with unique canonical codes.
 */
public final class GroupCatalogue {

    /** Error message for duplicate canonical group codes. */
    public static final String MESSAGE_DUPLICATE_CODES = "Configured group codes must be unique.";

    private final List<Group> groups;

    /**
     * Creates a catalogue by copying groups in the collection's iteration order.
     *
     * @param groups The configured groups.
     * @throws NullPointerException If {@code groups} or any group is null.
     * @throws IllegalArgumentException If any groups have the same canonical code.
     */
    public GroupCatalogue(Collection<Group> groups) {
        this.groups = List.copyOf(groups);
        Set<GroupCode> codes = new HashSet<>();
        for (Group group : this.groups) {
            checkArgument(codes.add(group.getCode()), MESSAGE_DUPLICATE_CODES);
        }
    }

    /**
     * Returns an unmodifiable list of groups in the input collection's iteration order.
     *
     * @return The ordered, unmodifiable group list.
     */
    public List<Group> getGroups() {
        return groups;
    }

    /**
     * Returns the number of configured groups.
     *
     * @return The catalogue size.
     */
    public int size() {
        return groups.size();
    }

    /**
     * Returns the group matching the supplied code, or an empty optional if no group matches.
     *
     * @param code The code to look up, ignoring case and surrounding whitespace.
     * @return The matching group, or an empty optional if the code is not configured.
     * @throws NullPointerException If {@code code} is null.
     * @throws IllegalArgumentException If {@code code} is blank.
     */
    public Optional<Group> findByCode(String code) {
        GroupCode groupCode = new GroupCode(code);
        return groups.stream()
                .filter(group -> group.getCode().equals(groupCode))
                .findFirst();
    }
}
