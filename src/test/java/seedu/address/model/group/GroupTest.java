package seedu.address.model.group;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class GroupTest {

    @Test
    public void constructor_validInputs_preservesFields() {
        GroupCode code = new GroupCode("S2-MAT-A");
        String label = "  Secondary  2 Mathematics A  ";
        Group group = new Group(code, label, GroupKind.CLASS);

        assertEquals(code, group.getCode());
        assertEquals(label, group.getLabel());
        assertEquals(GroupKind.CLASS, group.getKind());
    }

    @Test
    public void constructor_nullCode_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Group(null, "Mathematics", GroupKind.SUBJECT));
    }

    @Test
    public void constructor_nullLabel_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Group(new GroupCode("MAT"), null, GroupKind.SUBJECT));
    }

    @Test
    public void constructor_nullKind_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Group(new GroupCode("MAT"), "Mathematics", null));
    }

    @Test
    public void constructor_blankLabel_throwsIllegalArgumentException() {
        GroupCode code = new GroupCode("MAT");
        assertThrows(IllegalArgumentException.class, () -> new Group(code, "", GroupKind.SUBJECT));
        assertThrows(IllegalArgumentException.class, () -> new Group(code, " ", GroupKind.SUBJECT));
        assertThrows(IllegalArgumentException.class, () -> new Group(code, "\t\r\n", GroupKind.SUBJECT));
        assertThrows(IllegalArgumentException.class, () -> new Group(code, "\u2003", GroupKind.SUBJECT));
    }

    @Test
    public void constructor_nonBlankLabel_doesNotRestrictLabelOrInferKindFromCode() {
        Group group = new Group(new GroupCode("MAT"), "2 / Mathematics (A)", GroupKind.CLASS);

        assertEquals("2 / Mathematics (A)", group.getLabel());
        assertEquals(GroupKind.CLASS, group.getKind());
    }

    @Test
    public void equals() {
        Group group = new Group(new GroupCode("MAT"), "Mathematics", GroupKind.SUBJECT);
        Group equivalentGroup = new Group(new GroupCode(" mat "), "Mathematics", GroupKind.SUBJECT);

        assertTrue(group.equals(group));
        assertTrue(group.equals(equivalentGroup));
        assertTrue(equivalentGroup.equals(group));
        assertFalse(group.equals(null));
        assertFalse(group.equals("Mathematics"));
        assertFalse(group.equals(new Group(new GroupCode("SCI"), "Mathematics", GroupKind.SUBJECT)));
        assertFalse(group.equals(new Group(new GroupCode("MAT"), "Other label", GroupKind.SUBJECT)));
        assertFalse(group.equals(new Group(new GroupCode("MAT"), " Mathematics ", GroupKind.SUBJECT)));
        assertFalse(group.equals(new Group(new GroupCode("MAT"), "mathematics", GroupKind.SUBJECT)));
        assertFalse(group.equals(new Group(new GroupCode("MAT"), "Mathematics", GroupKind.CLASS)));
    }

    @Test
    public void hashCode_equalGroups_returnsSameHashCode() {
        Group group = new Group(new GroupCode("MAT"), "Mathematics", GroupKind.SUBJECT);
        Group equivalentGroup = new Group(new GroupCode(" mat "), "Mathematics", GroupKind.SUBJECT);

        assertEquals(group.hashCode(), equivalentGroup.hashCode());
    }
}
