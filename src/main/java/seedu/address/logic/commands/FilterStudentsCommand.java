package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.model.AddressBook.MESSAGE_UNKNOWN_GROUP;

import java.util.Objects;
import java.util.function.Predicate;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.group.Group;
import seedu.address.model.group.GroupAssignment;
import seedu.address.model.group.GroupCode;
import seedu.address.model.group.GroupKind;
import seedu.address.model.student.AcademicLevel;
import seedu.address.model.student.Student;

/** Filters the Student workspace by one subject group or academic level. */
public class FilterStudentsCommand extends Command {
    public static final String COMMAND_WORD = "filter-students";
    public static final String MESSAGE_USAGE_GROUP = "Invalid command format. Use: filter-students g/GROUP_CODE";
    public static final String MESSAGE_USAGE_ACADEMIC_LEVEL =
            "Invalid command format. Use: filter-students al/ACADEMIC_LEVEL";
    public static final String MESSAGE_FOUND_GROUP = "Found %d student(s) assigned to %s.";
    public static final String MESSAGE_NO_GROUP_MATCHES = "No students are assigned to %s.";
    public static final String MESSAGE_FOUND_ACADEMIC_LEVEL = "Found %d student(s) at %s.";
    public static final String MESSAGE_NO_ACADEMIC_LEVEL_MATCHES = "No students are at %s.";
    public static final String MESSAGE_CLASS_GROUP = "Group %s is a Class group; use the academic-level filter.";
    public static final String GROUP_FILTER_SUMMARY = "Filtered by group: %s — %s";
    public static final String ACADEMIC_LEVEL_FILTER_SUMMARY = "Filtered by academic level: %s";

    private final GroupCode groupCode;
    private final AcademicLevel academicLevel;

    /**
     * Creates a subject-group filter. Group existence and kind are checked during
     * execution.
     */
    public FilterStudentsCommand(GroupCode groupCode) {
        this.groupCode = requireNonNull(groupCode);
        academicLevel = null;
    }

    /** Creates an academic-level filter. */
    public FilterStudentsCommand(AcademicLevel academicLevel) {
        groupCode = null;
        this.academicLevel = requireNonNull(academicLevel);
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        return groupCode == null ? filterByAcademicLevel(model) : filterByGroup(model);
    }

    private CommandResult filterByGroup(Model model) throws CommandException {
        Group group = model.getGroupCatalogue().findByCode(groupCode.getValue())
                .orElseThrow(() -> new CommandException(String.format(MESSAGE_UNKNOWN_GROUP, groupCode)));
        if (group.getKind() == GroupKind.CLASS) {
            throw new CommandException(String.format(MESSAGE_CLASS_GROUP, group.getCode()));
        }
        Predicate<Student> predicate = student -> model.getAddressBook().getGroupAssignments()
                .contains(new GroupAssignment(student.getId(), group.getCode()));
        model.updateFilteredStudentList(
                predicate,
                String.format(GROUP_FILTER_SUMMARY, group.getCode(), group.getLabel()));
        int matchCount = model.getStudentList().size();
        String feedback = matchCount == 0
                ? String.format(MESSAGE_NO_GROUP_MATCHES, group.getCode())
                : String.format(MESSAGE_FOUND_GROUP, matchCount, group.getCode());
        return new CommandResult(feedback, CommandResult.Workspace.STUDENTS);
    }

    private CommandResult filterByAcademicLevel(Model model) {
        model.updateFilteredStudentList(student -> student.getAcademicLevel().equals(academicLevel),
                String.format(ACADEMIC_LEVEL_FILTER_SUMMARY, academicLevel));
        int matchCount = model.getStudentList().size();
        String feedback = matchCount == 0
                ? String.format(MESSAGE_NO_ACADEMIC_LEVEL_MATCHES, academicLevel)
                : String.format(MESSAGE_FOUND_ACADEMIC_LEVEL, matchCount, academicLevel);
        return new CommandResult(feedback, CommandResult.Workspace.STUDENTS);
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof FilterStudentsCommand command
                && Objects.equals(groupCode, command.groupCode)
                && Objects.equals(academicLevel, command.academicLevel);
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupCode, academicLevel);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("groupCode", groupCode)
                .add("academicLevel", academicLevel).toString();
    }
}
