package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.PersonBuilder;

public class RemarkTest {
    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Remark(null));
    }

    @Test
    public void value_emptyAndFreeText_supported() {
        assertEquals("", new Remark("").value);
        Remark remark = new Remark("Likes baseball! 日本語");
        assertEquals("Likes baseball! 日本語", remark.toString());
        assertEquals(remark, new Remark(remark.value));
        assertEquals(remark.hashCode(), new Remark(remark.value).hashCode());
        assertNotEquals(remark, new Remark("other"));
        assertNotEquals(remark, null);
        assertNotEquals(remark, "text");
    }

    @Test
    public void person_differentRemark_changesEqualityButNotIdentity() {
        Person remarkedAlice = new PersonBuilder(ALICE).withRemark("text").build();
        assertNotEquals(ALICE, remarkedAlice);
        assertTrue(ALICE.isSamePerson(remarkedAlice));
    }
}
