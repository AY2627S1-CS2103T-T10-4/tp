package seedu.address.model.group;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import org.junit.jupiter.api.Test;

public class GroupKindTest {

    @Test
    public void values_containsOnlySubjectAndClass() {
        assertArrayEquals(new GroupKind[] {GroupKind.SUBJECT, GroupKind.CLASS}, GroupKind.values());
    }
}
