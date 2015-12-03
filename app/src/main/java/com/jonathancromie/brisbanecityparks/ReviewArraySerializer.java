package com.jonathancromie.brisbanecityparks;

import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * Created by Jonathan on 03-Dec-15.
 */
public class ReviewArraySerializer implements JsonSerializer<Review[]>, JsonDeserializer<Review[]> {


    @Override
    public Review[] deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        String serialized = json.getAsString();
        JsonArray array = (JsonArray) new JsonParser().parse(serialized);
        return new Gson().fromJson(array, Review[].class);
    }

    @Override
    public JsonElement serialize(Review[] src, Type typeOfSrc, JsonSerializationContext context) {
        String serialized = new Gson().toJson(src);
        return new JsonPrimitive(serialized);
    }
}
