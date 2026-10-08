package seedu.address.logic;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Person;
import seedu.address.model.student.Student;

/** API of the application logic component. */
public interface Logic {
    CommandResult execute(String commandText) throws CommandException, ParseException;
    default ObservableList<Student> getStudentList() {
        return FXCollections.unmodifiableObservableList(FXCollections.observableArrayList());
    }
    /** Empty compatibility view for callers compiled against the retired Person UI. */
    @Deprecated
    default ObservableList<Person> getFilteredPersonList() {
        return FXCollections.observableArrayList();
    }
    GuiSettings getGuiSettings();
    void setGuiSettings(GuiSettings guiSettings);
}
