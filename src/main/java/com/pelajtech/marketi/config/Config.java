package com.pelajtech.marketi.config;

import com.pelajtech.marketi.log.Logging;

import java.util.Optional;

public class Config {

    public static String env(String key, String defaultValue) {
        return Optional.ofNullable(System.getenv(key)).orElse(defaultValue);
    }

    public static int env(String key, int defaultValue) {
        try {
            return Integer.parseInt(env(key, "" + defaultValue));
        } catch (NumberFormatException e) {
            Logging.LOG.error("Invalid value for {}. Needs to be an int. Using default value {}", key, defaultValue, e);
            return defaultValue;
        }
    }

    public static boolean env(String key, boolean defaultValue) {
        return Boolean.parseBoolean(env(key, "" + defaultValue));
    }

    public static double env(String key, double defaultValue) {
        try {
            return Double.parseDouble(env(key, "" + defaultValue));
        } catch (NumberFormatException e) {
            Logging.LOG.error("Invalid value for {}. Needs to be a double. Using default value {}", key, defaultValue, e);
            return defaultValue;
        }
    }

}
