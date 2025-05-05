package org.otherband.automigrate.serialization;

import com.google.gson.*;

import java.lang.reflect.Type;

public class MigrationSerializer {

    private static final Gson GSON = buildCustomGson();

    public static String serialize(MigrationDescription migrationDescription) {
        return GSON.toJson(migrationDescription);
    }

    private static Gson buildCustomGson() {
        return new GsonBuilder().registerTypeAdapter(MigrationStep.class, new MigrationStepSerializer()).create();
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
