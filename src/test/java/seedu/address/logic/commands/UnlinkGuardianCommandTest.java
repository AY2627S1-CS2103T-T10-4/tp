package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

class UnlinkGuardianCommandTest {
    @Test
    void equals_sameAndDifferentStudents_obeysEqualityContract() {
        UnlinkGuardianCommand command = new UnlinkGuardianCommand("S-0007");
        UnlinkGuardianCommand equivalent = new UnlinkGuardianCommand("S-0007");
        UnlinkGuardianCommand different = new UnlinkGuardianCommand("S-0008");

        assertEquals(command, command);
        assertEquals(command, equivalent);
        assertEquals(equivalent, command);
        assertNotEquals(command, different);
        assertNotEquals(command, null);
        assertNotEquals(command, new HelpCommand());

        Set<UnlinkGuardianCommand> commands = new HashSet<>();
        commands.add(command);
        commands.add(equivalent);
        commands.add(different);
        assertEquals(2, commands.size());
        assertEquals(command.hashCode(), equivalent.hashCode());
    }

    @Test
    void toString_includesTargetStudentId() {
        UnlinkGuardianCommand command = new UnlinkGuardianCommand("S-0007");

        assertEquals(UnlinkGuardianCommand.class.getCanonicalName() + "{studentId=S-0007}",
                command.toString());
    }

    @Test
    void constructorAndExecute_nullArguments_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new UnlinkGuardianCommand(null));
        assertThrows(NullPointerException.class, () -> new UnlinkGuardianCommand("S-0007").execute(null));
    }
}
