package org.otherband.serialization;

import com.google.gson.*;
import org.otherband.MigrationType;
import org.otherband.serialization.MigrationStep.MethodUseRename;

import java.lang.reflect.Type;

public class MigrationDeserialization {
    private static final Gson GSON = buildCustomGson();

    public static MigrationDescription fromJson(String jsonString) {
        return GSON.fromJson(jsonString, MigrationDescription.class);
    }

    private static Gson buildCustomGson() {
        return new GsonBuilder().registerTypeAdapter(MigrationStep.class, new MigrationStepDeserializer()).create();
    }

    private static class MigrationStepDeserializer implements JsonDeserializer<MigrationStep> {

        @Override
        public MigrationStep deserialize(JsonElement jsonElement,
                                         Type type,
                                         JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            return switch (getType(jsonElement)) {
                case METHOD_USE_RENAME -> jsonDeserializationContext.deserialize(jsonElement, MethodUseRename.class);
            };
        }

        private static MigrationType getType(JsonElement jsonElement) {
            return MigrationType.valueOf(jsonElement.getAsJsonObject().get("type").getAsString());
        }
    }

}
