package managers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.*;

public abstract class BotConfig {
    private static Set<String> requiredKeys;
    private static Map<String, String> config;

    /**
     * Initializes the config map from config.txt. Creates new file config.txt if it does not exist.
     *
     * Don't call this more than once, mkay?
     *
     * @return true if successful, false if there is input needed in config.txt
     */
    public static boolean init(){
        File file = new File("config.txt");
        if(!file.exists()){
            create(file);
            System.out.println("Created file \"config.txt\". Be sure to put the token in the file!");
            return false;
        }
        Scanner scanner;
        try {
            scanner = new Scanner(file);
        } catch (FileNotFoundException e) {
            // This should be impossible.
            e.printStackTrace();
            return false;
        }

        // reads config.txt and puts entries into config
        config = new HashMap<>();
        Set<String> emptyConfigValues = new HashSet<>();
        scanner.useDelimiter("\n");
        scanner.forEachRemaining((configLine) -> {
            if(!configLine.contains(":")) return;
            // all this regex does is remove whitespace around the : separator so the user can format config.txt with more freedom.
            configLine = configLine.replaceFirst("\\s*:\\s*", ":");
            int splitIndex = configLine.indexOf(':');
            String key = configLine.substring(0, splitIndex);
            // this is only true if there is no value assigned to this config entry
            if(splitIndex >= configLine.length() - 1){
                emptyConfigValues.add(key);
            }
            String value = configLine.substring(splitIndex + 1); // +1 to avoid including the :
            // putIfAbsent used in case duplicates somehow end up in config.txt. It will always use the first one.
            config.putIfAbsent(key, value);
        });

        // only valid if every config entry has a value
        boolean valid = emptyConfigValues.isEmpty();
        for(String key : emptyConfigValues){
            System.out.println('"' + key + "\" has no value in config.txt.");
        }

        // doesn't construct fw if it doesn't have to
        FileWriter fw = null;

        // Checks to see if every entry in requiredKeys is represented in config.txt, otherwise config.txt is invalid.

        for(String key : requiredKeys){
            // the key.split()[0] here just allows requiredKeys to also be used to assign default values to config entries.
            if(!config.containsKey(key.split("\\s*:\\s*")[0])){
                valid = false;
                if(fw == null){
                    try {
                        fw = new FileWriter(file, true);
                    } catch (IOException e) {
                        e.printStackTrace();
                        return false;
                    }
                }
                try {
                    // if there is no default value in key, adds the : to the end
                    // NOTE: technically, if config.txt is empty when this is run, this will add an ugly newline
                    // to the start of config.txt. If that happens, it's your fault. I took many precautions against that.
                    fw.append("\n").append(key).append(key.contains(":") ? "" : ": ");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println("adding config value \"" + key + '"');
            }
        }

        if(fw != null) {
            try {
                fw.flush();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        if(!valid){
            System.out.println("One or more missing entries in config.txt.");
        }
        return valid;
    }

    private static void create(File file){
        FileWriter fw;
        try {
            if(!file.createNewFile()){
                // This should be impossible, since this is only ever called if file does not exist.
                System.out.println("Something is very weird. Attempting to proceed normally...");
            }
            fw = new FileWriter(file);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        // This just avoids a newline preceding the config entries in config.txt.
        boolean first = true;

        // populates config.txt with all keys and optional default values from requiredKeys
        for(String key : requiredKeys){
            try {
                fw.append(first ? "" : "\n").append(key).append(key.contains(":") ? "" : ": ");
            } catch (IOException e) {
                e.printStackTrace();
            }
            first = false;
        }
        try {
            fw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets a value from config.txt.
     *
     * @throws InvalidParameterException if key is not in config.txt
     * @param key the key to retrieve the config entry of
     * @return the corresponding config entry
     */
    public static String get(String key){
        if(!config.containsKey(key)){
            throw new InvalidParameterException("key \"" + key + "\" not found in config.txt.");
        }
        return config.get(key);
    }

    /**
     * Only useful before init() is called.
     * Sets the keys that must be in config.txt, including optional default values.
     *
     * @param keys An array of keys
     */
    public static void setKeys(String... keys) {
        requiredKeys = new HashSet<>(Arrays.asList(keys));
    }
}
