import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

class JsonWriter {
    private static final int INDENT_SIZE = 2;
    private boolean pretty;

    public JsonWriter(boolean pretty) {
        this.pretty = pretty;
    }

    public String stringify(Object value) {
        StringBuilder output = new StringBuilder();
        write(output, value, 0);
        return output.toString();
    }

    private void write(StringBuilder output, Object value, int indent) {
        if (value == null) {
            writeNull(output);
        } else {
            Class<?> clazz = value.getClass();
            if (clazz == String.class || clazz == Character.class) {
                writeString(output, value);
            } else if (PRIMITIVE_TYPES.contains(clazz)) {
                writePrimitive(output, value);
            } else if (clazz.isArray()) {
                writeArray(output, (Object[]) value, indent);
            } else {
                writeObject(output, value, indent);
            }
        }
    }

    private void writeNull(StringBuilder output) {
        output.append("null");
    }

    private void writeString(StringBuilder output, Object value) {
        StringBuilder escapedValue = new StringBuilder();
        for (char character : value.toString().toCharArray()) {
            if (character == '\\') {
                escapedValue.append("\\\\");
            } else if (character == '"') {
                escapedValue.append("\\\"");
            } else {
                escapedValue.append(character);
            }
        }
        output.append("\"").append(escapedValue).append("\"");
    }

    private void writePrimitive(StringBuilder output, Object value) {
        output.append(value.toString());
    }

    private void writeArray(StringBuilder output, Object[] value, int indent) {
        output.append("[").append(lineBreak());
        for (int i = 0; i < value.length; i++) {
            output.append(space(indent + 1));
            write(output, value[i], indent + 1);
            if (i + 1 != value.length) {
                output.append(',');
            }
            output.append(lineBreak());
        }
        output.append(space(indent)).append("]");
    }

    private void writeObject(StringBuilder output, Object value, int indent) {
        output.append("{").append(lineBreak());
        Class<?> clazz = value.getClass();
        String[] fieldNames = JsonUtil.getFieldNames(clazz);
        boolean first = true;
        for (String fieldName : fieldNames) {
            Method method = JsonUtil.findGetter(clazz, fieldName);

            try {
                Object fieldValue = method.invoke(value);
                if (first) {
                    first = false;
                } else {
                    output.append(",").append(lineBreak());
                }
                output.append(space(indent + 1)).append("\"").append(fieldName).append("\":");
                if (pretty) {
                    output.append(" ");
                }
                write(output, fieldValue, indent + 1);
            } catch (Exception e) {
                throw new RuntimeException("Unable to serialize field " + fieldName, e);
            }
        }
        output.append(lineBreak()).append(space(indent)).append("}");
    }

    private String lineBreak() {
        return pretty ? "\n" : "";
    }

    private String space(int indent) {
        if (!pretty) {
            return "";
        } else {
            StringBuilder spaces = new StringBuilder();
            for (int i = 0; i < indent * INDENT_SIZE; i++) {
                spaces.append(" ");
            }
            return spaces.toString();
        }
    }

    private static final Set<Class<?>> PRIMITIVE_TYPES = getPrimitiveTypes();

    private static Set<Class<?>> getPrimitiveTypes() {
        Set<Class<?>> set = new HashSet<>();
        set.add(Boolean.class);
        set.add(Byte.class);
        set.add(Short.class);
        set.add(Integer.class);
        set.add(Long.class);
        set.add(Float.class);
        set.add(Double.class);
        return set;
    }
}
