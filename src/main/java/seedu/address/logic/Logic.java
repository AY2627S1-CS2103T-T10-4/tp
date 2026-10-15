package seedu.address.logic;

import java.util.Optional;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.guardian.Guardian;
import seedu.address.model.person.Person;
import seedu.address.model.student.Student;

/** API of the application logic component. */
public interface Logic {
    CommandResult execute(String commandText) throws CommandException, ParseException;
    default ObservableList<Student> getStudentList() {
        return FXCollections.unmodifiableObservableList(FXCollections.observableArrayList());
    }

    /** Returns current guardian details for a student profile, or empty if there is no linked guardian. */
    default Optional<Guardian> getLinkedGuardian(String studentId) {
        return Optional.empty();
    }
    /** Empty compatibility view for callers compiled against the retired Person UI. */
    @Deprecated
    default ObservableList<Person> getFilteredPersonList() {
        return FXCollections.observableArrayList();
    }
    GuiSettings getGuiSettings();
    void setGuiSettings(GuiSettings guiSettings);
}
