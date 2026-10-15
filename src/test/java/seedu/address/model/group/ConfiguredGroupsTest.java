package seedu.address.model.group;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

class ConfiguredGroupsTest {
    @Test
    void getCatalogue_containsExactlyTheApprovedGroupsInOrder() {
        GroupCatalogue catalogue = ConfiguredGroups.getCatalogue();

        assertEquals(5, catalogue.size());
        assertEquals(List.of("ENG", "MAT", "SCI", "CHI", "S2-MAT-A"),
                catalogue.getGroups().stream().map(group -> group.getCode().getValue()).toList());
        assertEquals(List.of("English", "Mathematics", "Science", "Chinese", "Secondary 2 Mathematics A"),
                catalogue.getGroups().stream().map(Group::getLabel).toList());
        assertEquals(List.of(GroupKind.SUBJECT, GroupKind.SUBJECT, GroupKind.SUBJECT,
                GroupKind.SUBJECT, GroupKind.CLASS), catalogue.getGroups().stream().map(Group::getKind).toList());
    }

    @Test
    void getCatalogue_isSharedImmutableAndUsesCanonicalLookup() {
        GroupCatalogue catalogue = ConfiguredGroups.getCatalogue();

        assertSame(catalogue, ConfiguredGroups.getCatalogue());
        assertThrows(UnsupportedOperationException.class, () -> catalogue.getGroups().clear());
        assertEquals("Mathematics", catalogue.findByCode(" mat ").orElseThrow().getLabel());
        assertEquals("S2-MAT-A", catalogue.findByCode("s2-mat-a").orElseThrow().getCode().getValue());
    }
}
