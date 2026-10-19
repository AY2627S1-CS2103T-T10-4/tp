package seedu.address.logic;

import java.util.List;
import java.util.Optional;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.group.Group;
import seedu.address.model.group.GroupCatalogue;
import seedu.address.model.guardian.Guardian;
import seedu.address.model.person.Person;
import seedu.address.model.student.Student;

/**
 * API of the application logic component.
 */
public interface Logic {
    /**
     * Parses and executes a command, saving successful mutations before returning their result.
     * Read-only commands do not save. Recovery rejects mutations before execution, and a failed save
     * restores the previous in-memory register before reporting the error.
     * A successful result selecting a hidden student clears the display filter after any required save.
     * Failed commands preserve the previous filter.
     *
     * @param commandText The complete command input.
     * @return Feedback and UI requests for the successfully completed command.
     * @throws ParseException If the command input is invalid.
     * @throws CommandException If validation, recovery protection or saving prevents completion.
     */
    CommandResult execute(String commandText) throws CommandException, ParseException;

    /**
     * Returns the immutable catalogue used by group commands and the Groups workspace.
     *
     * @return The shared catalogue in configured display order.
     */
    GroupCatalogue getGroupCatalogue();

    /**
     * Returns the recovery reason retained for this session when local data writes are disabled.
     *
     * @return The load-failure reason, or an empty optional when recovery protection is inactive.
     */
    Optional<String> getDataRecoveryReason();

    /**
     * Returns a student's assigned groups from the authoritative model in catalogue order.
     *
     * @param studentId The exact stable ID text to look up without allocating an ID.
     * @return An immutable snapshot, empty for an unknown student or a student without assignments.
     * @throws NullPointerException If {@code studentId} is null.
     */
    List<Group> getAssignedGroups(String studentId);

    default ObservableList<Student> getStudentList() {
        return FXCollections.unmodifiableObservableList(FXCollections.observableArrayList());
    }

    /** Returns the observable Guardian view displayed by the Guardians workspace. */
    default ObservableList<Guardian> getGuardianList() {
        return FXCollections.unmodifiableObservableList(FXCollections.observableArrayList());
    }
    /** Finds a student for profile display, including students hidden by the current search. */
    default Optional<Student> findStudent(String studentId) {
        return getStudentList().stream().filter(student -> student.getId().value.equals(studentId)).findFirst();
    }

    /** Returns current guardian details for a student profile, or empty if there is no linked guardian. */
    default Optional<Guardian> getLinkedGuardian(String studentId) {
        return Optional.empty();
    }

    /** Returns the students currently linked to the supplied guardian. */
    default List<Student> getLinkedStudents(String guardianId) {
        return List.of();
    }
    /** Empty compatibility view for callers compiled against the retired Person UI. */
    @Deprecated
    default ObservableList<Person> getFilteredPersonList() {
        return FXCollections.observableArrayList();
    }
    GuiSettings getGuiSettings();
    void setGuiSettings(GuiSettings guiSettings);
}
