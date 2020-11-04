public class Json {
    public static String stringify(Object value) {
        return stringify(value, false);
    }

    public static String stringify(Object value, boolean pretty) {
        JsonWriter writer = new JsonWriter(pretty);
        return writer.stringify(value);
    }

    public static <T> T parse(Class<T> clazz, String input) {
        if (clazz == Void.class && input.isEmpty()) {
            return null;
        }
        JsonReader reader = new JsonReader();
        return reader.parse(clazz, input);
    }
}
