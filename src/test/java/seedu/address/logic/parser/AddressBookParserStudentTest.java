package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import seedu.address.model.ModelManager;

class AddressBookParserStudentTest {
    @Test
    void parseCommand_addStudentCaseInsensitive_executesStudentCommand() throws Exception {
        ModelManager model = new ModelManager();
        var result = new AddressBookParser().parseCommand("ADD-STUDENT al/IP 2 n/Anita Lim").execute(model);
        assertEquals(1, model.getStudentList().size());
        assertEquals("Anita Lim", model.getStudentList().get(0).getName().toString());
        assertEquals("Added student " + model.getStudentList().get(0).getId() + ": Anita Lim (IP 2).",
                result.getFeedbackToUser());
    }

    @Test
    void parseCommand_deleteStudentCaseInsensitive_executesStudentCommand() throws Exception {
        ModelManager model = new ModelManager();
        var added = new AddressBookParser().parseCommand("add-student n/Anita Lim al/IP 2").execute(model);

        var result = new AddressBookParser().parseCommand("DELETE-STUDENT sid/" + added.getSelectedStudentId())
                .execute(model);

        assertEquals(0, model.getStudentList().size());
        assertEquals("Deleted student " + added.getSelectedStudentId() + ": Anita Lim.",
                result.getFeedbackToUser());
    }
}
