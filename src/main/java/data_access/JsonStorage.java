package data_access;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/**
 * Small JSON reader/writer using only the Java standard library.
 * Supports JSON objects, arrays, strings, numbers, booleans and null.
 */
public class JsonStorage {

    /** Loads JSON as Maps, Lists, Strings, Numbers, Booleans, or null. */
    public Object load(Path path) {
        if (!Files.exists(path)) {
            return new ArrayList<>();
        }

        try {
            String json = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
            if (json.trim().isEmpty()) return new ArrayList<>();
            return new JsonParser(json).parse();
        } catch (IOException ex) {
            throw new IllegalStateException("Could not read JSON file: " + path, ex);
        } catch (IllegalArgumentException ex) {
            throw new IllegalStateException("Invalid JSON in file: " + path, ex);
        }
    }

    /** Saves supported Java JSON values: Map, Collection, String, Number, Boolean, null. */
    public void save(Path path, Object value) {
        Path absolute = path.toAbsolutePath();
        Path parent = absolute.getParent();
        Path temp = null;

        try {
            if (parent != null) Files.createDirectories(parent);
            temp = Files.createTempFile(parent, "json-", ".tmp");
            String json = JsonWriter.write(value) + System.lineSeparator();
            Files.write(temp, json.getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.TRUNCATE_EXISTING);

            try {
                Files.move(temp, absolute, StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException ex) {
                Files.move(temp, absolute, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Could not save JSON file: " + path, ex);
        } finally {
            if (temp != null) {
                try {
                    Files.deleteIfExists(temp);
                } catch (IOException ignored) {
                    // Best-effort cleanup of the temporary file.
                }
            }
        }
    }

    private static final class JsonWriter {
        private static String write(Object value) {
            StringBuilder out = new StringBuilder();
            append(value, out);
            return out.toString();
        }

        private static void append(Object value, StringBuilder out) {
            if (value == null) {
                out.append("null");
            } else if (value instanceof String) {
                appendString((String) value, out);
            } else if (value instanceof Boolean || value instanceof Number) {
                out.append(value);
            } else if (value instanceof Map<?, ?>) {
                out.append('{');
                boolean first = true;
                for (Map.Entry<?, ?> entry : ((Map<?, ?>) value).entrySet()) {
                    if (!(entry.getKey() instanceof String)) {
                        throw new IllegalArgumentException("JSON object keys must be strings.");
                    }
                    if (!first) out.append(',');
                    appendString((String) entry.getKey(), out);
                    out.append(':');
                    append(entry.getValue(), out);
                    first = false;
                }
                out.append('}');
            } else if (value instanceof Collection<?>) {
                out.append('[');
                boolean first = true;
                for (Object item : (Collection<?>) value) {
                    if (!first) out.append(',');
                    append(item, out);
                    first = false;
                }
                out.append(']');
            } else {
                throw new IllegalArgumentException(
                        "Unsupported JSON value type: " + value.getClass().getName());
            }
        }

        private static void appendString(String value, StringBuilder out) {
            out.append('"');
            for (int i = 0; i < value.length(); i++) {
                char c = value.charAt(i);
                switch (c) {
                    case '"': out.append("\\\""); break;
                    case '\\': out.append("\\\\"); break;
                    case '\b': out.append("\\b"); break;
                    case '\f': out.append("\\f"); break;
                    case '\n': out.append("\\n"); break;
                    case '\r': out.append("\\r"); break;
                    case '\t': out.append("\\t"); break;
                    default:
                        if (c < 0x20) {
                            out.append(String.format("\\u%04x", (int) c));
                        } else {
                            out.append(c);
                        }
                }
            }
            out.append('"');
        }
    }

    private static final class JsonParser {
        private final String input;
        private int index;

        private JsonParser(String input) {
            this.input = input;
        }

        private Object parse() {
            skipWhitespace();
            Object value = parseValue();
            skipWhitespace();
            if (index != input.length()) fail("Unexpected characters after JSON value");
            return value;
        }

        private Object parseValue() {
            skipWhitespace();
            if (index >= input.length()) return fail("Unexpected end of JSON");
            char c = input.charAt(index);

            if (c == '{') return parseObject();
            if (c == '[') return parseArray();
            if (c == '"') return parseString();
            if (c == 't') return parseLiteral("true", Boolean.TRUE);
            if (c == 'f') return parseLiteral("false", Boolean.FALSE);
            if (c == 'n') return parseLiteral("null", null);
            if (c == '-' || Character.isDigit(c)) return parseNumber();
            return fail("Unexpected character: " + c);
        }

        private Map<String, Object> parseObject() {
            Map<String, Object> result = new LinkedHashMap<>();
            expect('{');
            skipWhitespace();
            if (take('}')) return result;

            while (true) {
                skipWhitespace();
                if (index >= input.length() || input.charAt(index) != '"') {
                    return fail("Expected an object key");
                }
                String key = parseString();
                skipWhitespace();
                expect(':');
                Object value = parseValue();
                result.put(key, value);
                skipWhitespace();
                if (take('}')) return result;
                expect(',');
            }
        }

        private List<Object> parseArray() {
            List<Object> result = new ArrayList<>();
            expect('[');
            skipWhitespace();
            if (take(']')) return result;

            while (true) {
                result.add(parseValue());
                skipWhitespace();
                if (take(']')) return result;
                expect(',');
            }
        }

        private String parseString() {
            expect('"');
            StringBuilder result = new StringBuilder();
            while (index < input.length()) {
                char c = input.charAt(index++);
                if (c == '"') return result.toString();
                if (c == '\\') {
                    if (index >= input.length()) return fail("Incomplete string escape");
                    char escaped = input.charAt(index++);
                    switch (escaped) {
                        case '"': result.append('"'); break;
                        case '\\': result.append('\\'); break;
                        case '/': result.append('/'); break;
                        case 'b': result.append('\b'); break;
                        case 'f': result.append('\f'); break;
                        case 'n': result.append('\n'); break;
                        case 'r': result.append('\r'); break;
                        case 't': result.append('\t'); break;
                        case 'u':
                            if (index + 4 > input.length()) return fail("Incomplete unicode escape");
                            try {
                                result.append((char) Integer.parseInt(input.substring(index, index + 4), 16));
                            } catch (NumberFormatException ex) {
                                return fail("Invalid unicode escape");
                            }
                            index += 4;
                            break;
                        default: return fail("Invalid string escape");
                    }
                } else {
                    if (c < 0x20) return fail("Unescaped control character in string");
                    result.append(c);
                }
            }
            return fail("Unterminated string");
        }

        private Object parseNumber() {
            int start = index;
            if (take('-')) { /* sign consumed */ }
            if (take('0')) {
                // A leading zero cannot be followed by another integer digit.
                if (index < input.length() && Character.isDigit(input.charAt(index))) {
                    return fail("Leading zero in number");
                }
            } else {
                if (!hasDigit()) return fail("Invalid number");
                while (hasDigit()) index++;
            }
            if (take('.')) {
                if (!hasDigit()) return fail("Invalid fraction");
                while (hasDigit()) index++;
            }
            if (take('e') || take('E')) {
                if (!take('+')) take('-');
                if (!hasDigit()) return fail("Invalid exponent");
                while (hasDigit()) index++;
            }
            String number = input.substring(start, index);
            try {
                return number.indexOf('.') >= 0 || number.indexOf('e') >= 0
                        || number.indexOf('E') >= 0
                        ? Double.valueOf(number) : Long.valueOf(number);
            } catch (NumberFormatException ex) {
                return fail("Invalid number");
            }
        }

        private Object parseLiteral(String literal, Object value) {
            if (!input.startsWith(literal, index)) return fail("Invalid literal");
            index += literal.length();
            return value;
        }

        private boolean hasDigit() {
            return index < input.length() && Character.isDigit(input.charAt(index));
        }

        private void skipWhitespace() {
            while (index < input.length() &&
                    (input.charAt(index) == ' ' || input.charAt(index) == '\n'
                            || input.charAt(index) == '\r' || input.charAt(index) == '\t')) {
                index++;
            }
        }

        private boolean take(char expected) {
            if (index < input.length() && input.charAt(index) == expected) {
                index++;
                return true;
            }
            return false;
        }

        private void expect(char expected) {
            if (!take(expected)) fail("Expected '" + expected + "'");
        }

        private <T> T fail(String message) {
            throw new IllegalArgumentException(message + " at character " + index);
        }
    }
}
