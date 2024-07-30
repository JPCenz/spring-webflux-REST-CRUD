package com.jpcenz.projects.springwebfluxrestcrud.util;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class JsonUtil {

    private static final Gson gson = new Gson();

    public static String convertObjectToJsonWithPrefix(Object object, String prefix) {
        // Convert object to JsonElement
        JsonElement jsonElement = gson.toJsonTree(object);

        // Add prefix to keys
        JsonObject prefixedJsonObject = addPrefixToKeys(jsonElement.getAsJsonObject(), prefix);

        // Convert JsonObject back to JSON string
        return gson.toJson(prefixedJsonObject);
    }

    private static JsonObject addPrefixToKeys(JsonObject jsonObject, String prefix) {
        JsonObject prefixedJsonObject = new JsonObject();

        for (String key : jsonObject.keySet()) {
            JsonElement value = jsonObject.get(key);
            prefixedJsonObject.add(prefix + key, value);
        }

        return prefixedJsonObject;
    }
}