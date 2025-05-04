package org.otherband.serialization;

import com.google.gson.*;
import org.otherband.MigrationType;
import org.otherband.serialization.MigrationStep.MethodUseRename;

import java.lang.reflect.Type;

public class MigrationDeserialization {
    private static final Gson GSON = new GsonBuilder().registerTypeAdapter(MigrationStep.class, new MigrationStepDeserializer()).create();

    public MigrationDescription fromJson(String jsonString) {
        return GSON.fromJson(jsonString, MigrationDescription.class);
    }


    private static class MigrationStepDeserializer implements JsonDeserializer<MigrationStep> {

        @Override
        public MigrationStep deserialize(JsonElement jsonElement,
                                         Type type,
                                         JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            MigrationType migrationType = MigrationType.valueOf(jsonElement.getAsJsonObject().get("type").getAsString());
            return switch (migrationType) {
                case METHOD_USE_RENAME -> jsonDeserializationContext.deserialize(jsonElement, MethodUseRename.class);
            };
        }
    }

}
