package utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Utils {
    public static String mapToJson(Object data) {
        // Create a Gson object and convert the Map object to JSON
        Gson gson = new GsonBuilder().create();
        return gson.toJson(data);
    }
}