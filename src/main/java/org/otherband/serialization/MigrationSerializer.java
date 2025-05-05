package org.otherband.serialization;

import com.google.gson.*;

import java.lang.reflect.Type;

public class MigrationSerializer {

    private static final Gson GSON = new GsonBuilder().registerTypeAdapter(MigrationStep.class, new MigrationStepSerializer()).create();

    public static String serialize(MigrationDescription migrationDescription) {
        return GSON.toJson(migrationDescription);
    }

    private static class MigrationStepSerializer implements JsonSerializer<MigrationStep> {

        @Override
        public JsonElement serialize(MigrationStep migrationStep, Type type, JsonSerializationContext jsonSerializationContext) {
            JsonElement result = jsonSerializationContext.serialize(migrationStep);
            result.getAsJsonObject().addProperty("type", migrationStep.type().name());
            return result;
        }
    }
}
