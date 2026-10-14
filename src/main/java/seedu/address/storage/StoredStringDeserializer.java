package seedu.address.storage;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;

/**
 * Rejects non-string JSON tokens instead of coercing malformed stored fields into valid domain text.
 * Required-field validation handles JSON null values after deserialization.
 */
class StoredStringDeserializer extends JsonDeserializer<String> {
    @Override
    public String deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        if (parser.getCurrentToken() != JsonToken.VALUE_STRING) {
            throw JsonMappingException.from(parser, "Expected a JSON string for " + parser.getCurrentName() + ".");
        }
        return parser.getText();
    }
}
