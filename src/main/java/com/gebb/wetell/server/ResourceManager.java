package com.gebb.wetell.server;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Collectors;

/**
 * Gets specific info from an .env file such as resource path, passwords, etc.
 */
public class ResourceManager {
    private static final String[] options = {"sslCertPath", "sslPassword"};

    private final HashMap<String, String> optionsValues = new HashMap<>(options.length);

    public ResourceManager() {
        try {
            File file = new File(".env");
            if (!file.exists()) {
                System.out.println("Created .env, writing options...");
                BufferedWriter writer = new BufferedWriter(new FileWriter(file));
                for (String option : options) {
                    writer.write(option + ":\n");
                }
                writer.close();
                return;
            }
            BufferedReader reader = new BufferedReader(new FileReader(file));
            for (String line : reader.lines().collect(Collectors.toList())) {
                for (String option: options) {
                    int index = line.indexOf(option);
                    if (index == 0) {
                        if (line.charAt(index + option.length()) != ':') {
                            System.err.println("Found option at the start of the line, but `:` is missing");
                            return;
                        }
                        String value = line.substring(index + option.length() + 1);
                        if (value.isBlank()) {
                            optionsValues.put(option, null);
                        } else {
                            optionsValues.put(option, value);
                        }
                    }
                }
            }
            BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));
            for (String option : options) {
                if (!optionsValues.containsKey(option)) {
                    writer.write(option + ":\n");
                }
            }
            writer.close();
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * @param option
     * @return The value of the option, may be null
     */
    public String getInfo(String option) {
        return optionsValues.get(option);
    }
}
