package seedu.address.model.group;

import java.util.List;

/**
 * Supplies the single application-owned, validated five-group catalogue for the v1.3 MVP.
 * This immutable configuration is separate from persisted student data.
 */
public final class ConfiguredGroups {
    private static final GroupCatalogue CATALOGUE = new GroupCatalogue(List.of(
            new Group(new GroupCode("ENG"), "English", GroupKind.SUBJECT),
            new Group(new GroupCode("MAT"), "Mathematics", GroupKind.SUBJECT),
            new Group(new GroupCode("SCI"), "Science", GroupKind.SUBJECT),
            new Group(new GroupCode("CHI"), "Chinese", GroupKind.SUBJECT),
            new Group(new GroupCode("S2-MAT-A"), "Secondary 2 Mathematics A", GroupKind.CLASS)));

    private ConfiguredGroups() { }

    /**
     * Returns the shared immutable catalogue in its configured display order.
     *
     * @return The fixed catalogue shared by validation, commands and display.
     */
    public static GroupCatalogue getCatalogue() {
        return CATALOGUE;
    }
}
