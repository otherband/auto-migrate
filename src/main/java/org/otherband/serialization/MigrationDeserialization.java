package org.otherband.serialization;

import com.google.gson.*;
import org.otherband.MigrationType;
import org.otherband.serialization.MigrationStep.MethodUseRename;

import java.lang.reflect.Type;
import java.util.Objects;

public class MigrationDeserialization {
    private static final Gson GSON = buildCustomGson();

    public static MigrationDescription fromJson(String jsonString) {
        return GSON.fromJson(jsonString, MigrationDescription.class);
    }

    private static Gson buildCustomGson() {
        return new GsonBuilder().registerTypeAdapter(MigrationStep.class, new MigrationStepDeserializer()).create();
    }

    private static class MigrationStepDeserializer implements JsonDeserializer<MigrationStep> {

        private static final String MUST_CONTAIN_TYPE = "Could not deserialize migration: object [%s] does not contain a 'type' attribute";

        @Override
        public MigrationStep deserialize(JsonElement jsonElement,
                                         Type type,
                                         JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            return switch (getType(jsonElement)) {
                case METHOD_USE_RENAME -> jsonDeserializationContext.deserialize(jsonElement, MethodUseRename.class);
            };
        }

        private static MigrationType getType(JsonElement jsonElement) {
            JsonElement typeField = jsonElement.getAsJsonObject().get("type");
            if (Objects.isNull(typeField)) {
                throw new SerializationException(MUST_CONTAIN_TYPE.formatted(jsonElement));
            }
            return MigrationType.valueOf(typeField.getAsString());
        }
    }


}
