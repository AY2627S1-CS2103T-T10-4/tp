package seedu.address.logic;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.stream.Collectors;

/** Bundled, offline command help shared by parsing and the help window. */
public final class HelpContent {
    private static final Properties TOPICS = loadTopics();

    private HelpContent() { }

    private static Properties loadTopics() {
        Properties topics = new Properties();
        try (InputStreamReader reader = new InputStreamReader(Objects.requireNonNull(
                HelpContent.class.getResourceAsStream("/help/commands.properties")), StandardCharsets.UTF_8)) {
            topics.load(reader);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not load bundled command help.", e);
        }
        return topics;
    }

    /** Returns supported command names in display order. */
    public static List<String> getCommands() {
        return TOPICS.stringPropertyNames().stream().sorted().toList();
    }

    /** Returns whether help exists for a canonical command name. */
    public static boolean contains(String command) {
        return TOPICS.containsKey(command);
    }

    /** Returns detailed help for a canonical command name. */
    public static String getTopic(String command) {
        return Objects.requireNonNull(TOPICS.getProperty(command), "Unknown help command");
    }

    /** Lists every supported syntax and explains how to open detailed help. */
    public static String getOverview() {
        return "Mentora command help\n\nUse help COMMAND for examples, validation and common errors.\n\n"
                + getCommands().stream().map(command -> getTopic(command).split("\n", 2)[0])
                        .collect(Collectors.joining("\n\n"));
    }
}
