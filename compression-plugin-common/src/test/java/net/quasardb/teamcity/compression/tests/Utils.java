package net.quasardb.teamcity.compression.tests;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class Utils {
    public static File getFileFromResources(String filePath){
        return new File("src/test/resources/"+filePath);
    }


    public static Map<String,String> mapOf(KeyValuePair ... entries){
        Map<String,String> map = new HashMap<>();
        for(KeyValuePair entry: entries){
            map.put(entry.getKey(),entry.getValue());
        }
        return map;
    }

    static class KeyValuePair {

        private final String value;
        private final String key;

        public KeyValuePair(String key, String value) {
            this.key = key;
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public String getKey() {
            return key;
        }
    }

}
