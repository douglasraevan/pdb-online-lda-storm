package gr.ntua.olda.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.Set;


/**
 * LocalConfig Parser
 *
 */
public class LocalConfig {

    private static final String propertyFile = "config.properties";
    private static Properties res;

    static {
        try {
            res = new Properties();
            load();
        } catch (MissingResourceException e) {
            throw new java.lang.RuntimeException("Property file \"" + propertyFile + "\" not found in the classpath.");
        } catch (IOException e) {
            throw new java.lang.RuntimeException("Unable to load properties from file \"" + propertyFile + "\"", e);
        }
    }

    private LocalConfig() {
        super();
    }

    private static void load() throws IOException {
        InputStream is = LocalConfig.class.getClassLoader().getResourceAsStream(propertyFile);
        if (is == null) {
            throw new java.lang.RuntimeException("Property file \"" + propertyFile + "\" not found in the classpath.");
        }

        InputStreamReader r;
        try {
            r = new InputStreamReader(is, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            r = new InputStreamReader(is);
        }

        res.clear();
        res.load(r);

        r.close();
        is.close();
    }

    public static Set<String> getKeys() {
        return res.stringPropertyNames();
    }

    /**
     * Returns the value of a key parsed as String, if parse fails returns null
     * @param key property key
     * @return value of key
     */
    public static String get(String key) {
        return get(key,null);
    }

    /**
     * Returns the value of a key parsed as String;
     * @param key property key
     * @param def default value
     * @return value of key
     */
    public static String get(String key, String def) {
        return res.getProperty(key, def);
    }

    /**
     * Returns the value of a key parsed as long;
     * @param key property key
     * @param def default value
     * @return value of key
     */
    public static long getLong(String key, long def) {
        long val;

        String stringValue = res.getProperty(key);
        if (stringValue == null) {
            val = def;
        } else {
            val = Long.parseLong(stringValue);
        }

        return val;
    }

    /**
     * Returns the value of a key parsed as int;
     * @param key property key
     * @param def default value
     * @return value of key
     */
    public static int getInt(String key, int def) {
        int val;

        String stringValue = res.getProperty(key);
        if (stringValue == null) {
            val = def;
        } else {
            val = Integer.parseInt(stringValue);
        }

        return val;
    }

    /**
     * Returns the value of a key parsed as boolean;
     * @param key property key
     * @param def default value
     * @return value of key
     */
    public static boolean getBoolean(String key, boolean def){
        boolean val;

        String stringValue = res.getProperty(key);
        if (stringValue == null) {
            val = def;
        } else {
            val = Boolean.parseBoolean(stringValue);
        }

        return val;
    }

    /**
     * Returns the value of a key parsed as double;
     * @param key property key
     * @param def default value
     * @return value of key
     */
    public static double getDouble(String key, double def) {
        double val;

        String stringValue = res.getProperty(key);
        if (stringValue == null) {
            val = def;
        } else {
            val = Double.parseDouble(stringValue);
        }

        return val;
    }
}
