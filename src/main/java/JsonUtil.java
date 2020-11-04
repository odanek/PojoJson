import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class JsonUtil {
    static String[] getFieldNames(Class<?> clazz) {
        JsonConfig config = clazz.getAnnotation(JsonConfig.class);
        if (config != null) {
            return config.fields();
        } else {
            Field[] fields = clazz.getDeclaredFields();
            String[] names = new String[fields.length];
            for (int i = 0; i < fields.length; i++) {
                names[i] = fields[i].getName();
            }
            return names;
        }
    }

    static Method findGetter(Class<?> clazz, String fieldName) {
        try {
            return clazz.getMethod("get" + capitalize(fieldName));
        } catch (NoSuchMethodException e) {
            try {
                return clazz.getMethod("is" + capitalize(fieldName));
            } catch (NoSuchMethodException e2) {
                throw new RuntimeException("Unable to find getter method for: " + fieldName);
            }
        }
    }

    static String capitalize(String input) {
        return Character.toUpperCase(input.charAt(0)) + input.substring(1);
    }
}
