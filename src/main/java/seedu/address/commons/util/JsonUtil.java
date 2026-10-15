package seedu.address.commons.util;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.logging.Logger;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.exceptions.DataLoadingException;

/**
 * Converts a Java object instance to JSON and vice versa
 */
public class JsonUtil {

    private static final Logger logger = LogsCenter.getLogger(JsonUtil.class);

    private static final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules()
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE)
            .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

    static <T> void serializeObjectToJsonFile(Path jsonFile, T objectToSerialize) throws IOException {
        FileUtil.writeToFile(jsonFile, toJsonString(objectToSerialize));
    }

    static <T> T deserializeObjectFromJsonFile(Path jsonFile, Class<T> classOfObjectToDeserialize)
            throws IOException {
        return fromJsonString(FileUtil.readFromFile(jsonFile), classOfObjectToDeserialize);
    }

    /**
     * Returns the JSON object from the given file or {@code Optional.empty()} object if the file is not found.
     * Missing properties follow the target type's validation/defaults. Null roots and incomplete reads are errors.
     *
     * @param <T> Type represented by the JSON document.
     * @param filePath Path to read; cannot be null.
     * @param classOfObjectToDeserialize JSON file has to correspond to the structure in the class given here.
     * @return The deserialized value, or empty only for a confirmed missing path.
     * @throws DataLoadingException If reading or parsing fails, or the root value is null.
     */
    public static <T> Optional<T> readJsonFile(
            Path filePath, Class<T> classOfObjectToDeserialize) throws DataLoadingException {
        requireNonNull(filePath);
        requireNonNull(classOfObjectToDeserialize);

        T jsonFile;

        try {
            jsonFile = deserializeObjectFromJsonFile(filePath, classOfObjectToDeserialize);
            if (jsonFile == null) {
                throw new IOException("The data file must contain a JSON value, not null.");
            }
        } catch (NoSuchFileException e) {
            // An inaccessible path or dangling symbolic link must not be treated as a fresh installation.
            if (Files.notExists(filePath, LinkOption.NOFOLLOW_LINKS)) {
                return Optional.empty();
            }
            throw new DataLoadingException(e);
        } catch (IOException e) {
            logger.warning("Error reading from jsonFile file " + filePath + ": " + e);
            throw new DataLoadingException(e);
        }

        return Optional.of(jsonFile);
    }

    /**
     * Saves the Json object to the specified file.
     * Overwrites existing file if it exists, creates a new file if it doesn't.
     * @param jsonFile cannot be null
     * @param filePath cannot be null
     * @throws IOException if there was an error during writing to the file
     */
    public static <T> void saveJsonFile(T jsonFile, Path filePath) throws IOException {
        requireNonNull(filePath);
        requireNonNull(jsonFile);

        serializeObjectToJsonFile(filePath, jsonFile);
    }


    /**
     * Converts one JSON value to an instance, rejecting duplicate field names and trailing content.
     * Unlike {@link #readJsonFile(Path, Class)}, this low-level conversion permits a null root.
     *
     * @param <T> Type to deserialize.
     * @param json JSON text to parse.
     * @param instanceClass Target type with its own field and domain validation.
     * @return The converted instance, or null for a JSON null value.
     * @throws IOException If parsing or conversion fails, or additional content follows the value.
     */
    public static <T> T fromJsonString(String json, Class<T> instanceClass) throws IOException {
        try (JsonParser parser = objectMapper.getFactory().createParser(json)) {
            parser.enable(JsonParser.Feature.STRICT_DUPLICATE_DETECTION);
            T result = objectMapper.readValue(parser, instanceClass);
            if (parser.nextToken() != null) {
                throw new IOException("Unexpected content after the JSON value.");
            }
            return result;
        }
    }

    /**
     * Converts a given instance of a class into its JSON data string representation
     * @param instance The T object to be converted into the JSON string
     * @param <T> The generic type to create an instance of
     * @return JSON data representation of the given class instance, in string
     */
    public static <T> String toJsonString(T instance) throws JsonProcessingException {
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(instance);
    }

}
