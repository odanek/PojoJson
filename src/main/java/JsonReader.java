import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class JsonReader {
    int cursor;
    String input;

    public <T> T parse(Class<T> clazz, String input) {
        this.input = input;
        cursor = 0;
        return (T) readValue(clazz);
    }

    private Object readValue(Class<?> clazz) {
        consumeWhitespace();

        char next = checkedCharAtCursor();
        if (next == 'n') {
            consumeText("null");
            return null;
        } else if (clazz == Boolean.class || clazz == boolean.class) {
            return readBoolean();
        } else if (clazz == String.class) {
            return readString();
        } else if (clazz == Character.class) {
            return readCharacter();
        } else if (clazz.isArray()) {
            return readArray(clazz);
        } else if (clazz == Byte.class || clazz == byte.class) {
            return Byte.valueOf(readNumber());
        } else if (clazz == Short.class || clazz == short.class) {
            return Short.valueOf(readNumber());
        } else if (clazz == Integer.class || clazz == int.class) {
            return Integer.valueOf(readNumber());
        } else if (clazz == Long.class || clazz == long.class) {
            return Long.valueOf(readNumber());
        } else if (clazz == Float.class || clazz == float.class) {
            return Float.valueOf(readNumber());
        } else if (clazz == Double.class || clazz == double.class) {
            return Double.valueOf(readNumber());
        } else {
            return readObject(clazz);
        }
    }

    private Boolean readBoolean() {
        char next = checkedCharAtCursor();
        if (next == 't') {
            consumeText("true");
            return true;
        } else if (next == 'f') {
            consumeText("false");
            return false;
        } else {
            throw new RuntimeException("Unexpected token encountered: " + next + ", expected boolean value");
        }
    }

    private String readString() {
        return consumeString();
    }

    private Character readCharacter() {
        String output = consumeString();
        if (output.length() != 1) {
            throw new RuntimeException("Unexpected string length, expected single character");
        }
        return input.charAt(0);
    }

    private String readNumber() {
        int start = cursor;

        if (checkedCharAtCursor() == '-') {
            consumeCharacter('-');
        }

        while (true) {
            char next = checkedCharAtCursor();
            if (Character.isDigit(next) || next == '.') {
                cursor++;
            } else {
                break;
            }
        }

        if (cursor - start == 1 && input.charAt(start) == '-') {
            throw new RuntimeException("Invalid number encountered");
        }

        return input.substring(start, cursor);
    }

    private <T> T[] readArray(Class<T> clazz) {
        ArrayList<T> values = new ArrayList<>();

        consumeCharacter('[');

        while (consumeSeparator(values.size() == 0, ']')) {
            T item = (T)readValue(clazz.getComponentType());
            values.add(item);
        }

        T[] result = (T[]) Array.newInstance(clazz.getComponentType(), 0);
        return values.toArray(result);
    }

    private Object readObject(Class<?> clazz) {
        Map<String, Object> fields = new HashMap<>();

        consumeText("{");

        try {
            while (consumeSeparator(fields.size() == 0, '}')) {
                String fieldName = consumeFieldName();
                Class<?> fieldClass = JsonUtil.findGetter(clazz, fieldName).getReturnType();
                Object fieldValue = readValue(fieldClass);
                fields.put(fieldName, fieldValue);
            }

            Constructor<?> constructor = clazz.getConstructors()[0];
            return createInstance(clazz, constructor, fields);
        } catch (Exception e) {
            throw new RuntimeException("Unable to deserialize object: " + clazz.getName(), e);
        }
    }

    private Object createInstance(Class<?> clazz, Constructor<?> constructor, Map<String, Object> fields) {
        List<Object> parameters = new ArrayList<>();
        String[] fieldNames = JsonUtil.getFieldNames(clazz);
        for (String fieldName : fieldNames) {
            if (!fields.containsKey(fieldName)) {
                throw new RuntimeException("Parameter value missing: " + fieldName);
            }
            parameters.add(fields.get(fieldName));
        }

        try {
            return constructor.newInstance(parameters.toArray());
        } catch (Exception e) {
            throw new RuntimeException("Unable to construct: " + clazz.getName(), e);
        }
    }

    private void consumeWhitespace() {
        while (!endOfInput() && Character.isWhitespace(charAtCursor())) {
            cursor++;
        }
    }

    private void consumeCharacter(char character) {
        char inputCharacter = checkedCharAtCursor();
        if (inputCharacter != character) {
            throw new RuntimeException("Unexpected character: '" + inputCharacter + "', expected: " + character);
        }
        cursor++;
    }

    private void consumeText(String text) {
        for (char character : text.toCharArray()) {
            consumeCharacter(character);
        }
    }

    private boolean consumeSeparator(boolean first, char closeCharacter) {
        consumeWhitespace();
        char next = checkedCharAtCursor();

        if (next == closeCharacter) {
            consumeCharacter(closeCharacter);
            return false;
        }
        if (!first) {
            if (next == ',') {
                consumeText(",");
            } else {
                throw new RuntimeException("Expected item separator, got " + next);
            }
        }
        return true;
    }

    private String consumeString() {
        StringBuilder result = new StringBuilder();
        consumeCharacter('"');
        boolean escaping = false;
        while (cursor < input.length()) {
            char token = input.charAt(cursor);
            if (token == '"' && !escaping) {
                cursor++;
                return result.toString();
            } else if (token == '\\' && !escaping) {
                escaping = true;
            } else {
                escaping = false;
                result.append(token);
            }
            cursor++;
        }
        throw new RuntimeException("Unexpected end of input");
    }

    private String consumeFieldName() {
        consumeWhitespace();
        String fieldName = readString();
        consumeWhitespace();
        consumeCharacter(':');
        return fieldName;
    }

    private boolean endOfInput() {
        return cursor >= input.length();
    }

    private char charAtCursor() {
        return input.charAt(cursor);
    }

    private char checkedCharAtCursor() {
        if (endOfInput()) {
            throw new RuntimeException("Unexpected end of input");
        }
        return charAtCursor();
    }
}