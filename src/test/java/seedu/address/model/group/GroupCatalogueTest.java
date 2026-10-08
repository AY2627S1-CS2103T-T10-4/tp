package seedu.address.model.group;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

public class GroupCatalogueTest {

    private static final String DUPLICATE_CODE_MESSAGE = "Configured group codes must be unique.";
    private static final Group MATHEMATICS = new Group(new GroupCode("MAT"), "Mathematics", GroupKind.SUBJECT);
    private static final Group ENGLISH = new Group(new GroupCode("ENG"), "English", GroupKind.SUBJECT);
    private static final Group MATH_CLASS =
            new Group(new GroupCode("S2-MAT-A"), "Secondary 2 Mathematics A", GroupKind.CLASS);

    @Test
    public void constructor_nullCollection_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new GroupCatalogue(null));
    }

    @Test
    public void constructor_nullEntry_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new GroupCatalogue(Arrays.asList(MATHEMATICS, null)));
    }

    @Test
    public void constructor_repeatedGroup_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, DUPLICATE_CODE_MESSAGE, () ->
                new GroupCatalogue(List.of(MATHEMATICS, MATHEMATICS)));
    }

    @Test
    public void constructor_duplicateCanonicalCodes_throwsIllegalArgumentException() {
        Group differentLabel = new Group(new GroupCode("mat"), "Other label", GroupKind.SUBJECT);
        Group differentKind = new Group(new GroupCode(" MAT "), "Mathematics", GroupKind.CLASS);
        Group differentLabelAndKind = new Group(new GroupCode(" \tmAt\n "), "Another label", GroupKind.CLASS);

        assertThrows(IllegalArgumentException.class, DUPLICATE_CODE_MESSAGE, () ->
                new GroupCatalogue(List.of(MATHEMATICS, differentLabel)));
        assertThrows(IllegalArgumentException.class, DUPLICATE_CODE_MESSAGE, () ->
                new GroupCatalogue(List.of(MATHEMATICS, differentKind)));
        assertThrows(IllegalArgumentException.class, DUPLICATE_CODE_MESSAGE, () ->
                new GroupCatalogue(List.of(MATHEMATICS, differentLabelAndKind)));
    }

    @Test
    public void constructor_unicodeCaseVariants_throwsIllegalArgumentException() {
        Group lowercaseGroup = new Group(new GroupCode("\u00df"), "First label", GroupKind.SUBJECT);
        Group uppercaseGroup = new Group(new GroupCode("\u1e9e"), "Second label", GroupKind.CLASS);

        assertThrows(IllegalArgumentException.class, DUPLICATE_CODE_MESSAGE, () ->
                new GroupCatalogue(List.of(lowercaseGroup, uppercaseGroup)));
    }

    @Test
    public void constructor_distinctCodesWithSameMetadata_acceptsGroups() {
        Group otherCode = new Group(new GroupCode("OTHER"), "Mathematics", GroupKind.SUBJECT);
        GroupCatalogue catalogue = new GroupCatalogue(List.of(MATHEMATICS, otherCode));

        assertEquals(List.of(MATHEMATICS, otherCode), catalogue.getGroups());
        assertEquals(2, catalogue.size());
    }

    @Test
    public void constructor_emptyCollection_createsEmptyCatalogue() {
        GroupCatalogue catalogue = new GroupCatalogue(List.of());

        assertTrue(catalogue.getGroups().isEmpty());
        assertEquals(0, catalogue.size());
        assertEquals(Optional.empty(), catalogue.findByCode("MAT"));
    }

    @Test
    public void constructor_orderedCollection_preservesIterationOrder() {
        LinkedHashSet<Group> groups = new LinkedHashSet<>(List.of(MATH_CLASS, MATHEMATICS, ENGLISH));
        GroupCatalogue catalogue = new GroupCatalogue(groups);

        assertEquals(List.of(MATH_CLASS, MATHEMATICS, ENGLISH), catalogue.getGroups());
        assertEquals(3, catalogue.size());
    }

    @Test
    public void constructor_originalListModified_preservesCatalogue() {
        List<Group> groups = new ArrayList<>(List.of(MATHEMATICS, ENGLISH));
        GroupCatalogue catalogue = new GroupCatalogue(groups);

        groups.clear();
        groups.add(MATH_CLASS);

        assertEquals(List.of(MATHEMATICS, ENGLISH), catalogue.getGroups());
        assertEquals(2, catalogue.size());
        assertEquals(Optional.of(MATHEMATICS), catalogue.findByCode("mat"));
        assertEquals(Optional.empty(), catalogue.findByCode("S2-MAT-A"));
    }

    @Test
    public void getGroups_modifications_throwsUnsupportedOperationException() {
        GroupCatalogue catalogue = new GroupCatalogue(List.of(MATHEMATICS, ENGLISH));
        List<Group> groups = catalogue.getGroups();

        assertThrows(UnsupportedOperationException.class, () -> groups.add(MATH_CLASS));
        assertThrows(UnsupportedOperationException.class, () -> groups.remove(0));
        assertThrows(UnsupportedOperationException.class, () -> groups.set(0, MATH_CLASS));
        assertThrows(UnsupportedOperationException.class, groups::clear);
        assertEquals(List.of(MATHEMATICS, ENGLISH), catalogue.getGroups());
        assertEquals(2, catalogue.size());
    }

    @Test
    public void findByCode_caseAndWhitespaceVariants_returnsConfiguredGroup() {
        GroupCatalogue catalogue = new GroupCatalogue(List.of(MATHEMATICS, MATH_CLASS));

        assertEquals(Optional.of(MATHEMATICS), catalogue.findByCode("MAT"));
        assertEquals(Optional.of(MATHEMATICS), catalogue.findByCode("mat"));
        assertEquals(Optional.of(MATHEMATICS), catalogue.findByCode(" \tmAt\n "));
        assertEquals(Optional.of(MATH_CLASS), catalogue.findByCode("\u2003s2-mat-a\u2003"));
    }

    @Test
    public void findByCode_unicodeCaseVariant_returnsConfiguredGroup() {
        Group lowercaseGroup = new Group(new GroupCode("\u00df"), "First label", GroupKind.SUBJECT);
        Group uppercaseGroup = new Group(new GroupCode("\u1e9e"), "Second label", GroupKind.CLASS);
        GroupCatalogue lowercaseCatalogue = new GroupCatalogue(List.of(lowercaseGroup));
        GroupCatalogue uppercaseCatalogue = new GroupCatalogue(List.of(uppercaseGroup));

        assertEquals(Optional.of(lowercaseGroup), lowercaseCatalogue.findByCode(" \u1e9e "));
        assertEquals(Optional.of(uppercaseGroup), uppercaseCatalogue.findByCode("\u00df"));
    }

    @Test
    public void findByCode_missingCode_returnsEmptyOptional() {
        GroupCatalogue catalogue = new GroupCatalogue(List.of(MATHEMATICS));

        assertEquals(Optional.empty(), catalogue.findByCode("SCI"));
        assertEquals(Optional.empty(), catalogue.findByCode("MA"));
        assertEquals(Optional.empty(), catalogue.findByCode("MAT-A"));
    }

    @Test
    public void findByCode_nullCode_throwsNullPointerException() {
        GroupCatalogue catalogue = new GroupCatalogue(List.of(MATHEMATICS));
        GroupCatalogue emptyCatalogue = new GroupCatalogue(List.of());

        assertThrows(NullPointerException.class, () -> catalogue.findByCode(null));
        assertThrows(NullPointerException.class, () -> emptyCatalogue.findByCode(null));
    }

    @Test
    public void findByCode_blankCode_throwsIllegalArgumentException() {
        GroupCatalogue catalogue = new GroupCatalogue(List.of(MATHEMATICS));
        GroupCatalogue emptyCatalogue = new GroupCatalogue(List.of());

        assertThrows(IllegalArgumentException.class, () -> catalogue.findByCode(""));
        assertThrows(IllegalArgumentException.class, () -> catalogue.findByCode(" \t\n "));
        assertThrows(IllegalArgumentException.class, () -> emptyCatalogue.findByCode("\u2003"));
    }
}
