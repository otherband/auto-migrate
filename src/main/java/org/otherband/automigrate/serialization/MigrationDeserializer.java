package org.otherband.automigrate.serialization;

import com.google.gson.*;
import org.otherband.automigrate.MigrationType;
import org.otherband.automigrate.serialization.MigrationStep.MethodUseRename;
import org.otherband.automigrate.serialization.MigrationStep.TypeRename;

import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

public class MigrationDeserializer {
    private static final Gson GSON = buildCustomGson();
    private static final Set<String> MIGRATION_TYPES = Arrays.stream(MigrationType.values())
            .map(migrationType -> migrationType.name().toLowerCase(Locale.ROOT))
            .collect(Collectors.toSet());

    public static MigrationDescription fromJson(String jsonString) {
        return GSON.fromJson(jsonString, MigrationDescription.class);
    }

    private static Gson buildCustomGson() {
        return new GsonBuilder().registerTypeAdapter(MigrationStep.class, new MigrationStepDeserializer()).create();
    }

    private static class MigrationStepDeserializer implements JsonDeserializer<MigrationStep> {

        private static final String MUST_CONTAIN_TYPE = "Could not deserialize migration: object [%s] does not contain a 'type' attribute";
        private static final String UNRECOGNIZED_TYPE = "Unrecognized migration step type '%s'";

        @Override
        public MigrationStep deserialize(JsonElement jsonElement,
                                         Type type,
                                         JsonDeserializationContext context) throws JsonParseException {
            return switch (getType(jsonElement)) {
                case RENAME_METHOD -> context.deserialize(jsonElement, MethodUseRename.class);
                case RENAME_TYPE -> context.deserialize(jsonElement, TypeRename.class);
            };
        }

        private static MigrationType getType(JsonElement jsonElement) {
            JsonElement typeField = jsonElement.getAsJsonObject().get("type");
            String typeValue = getValidatedType(jsonElement, typeField);
            return MigrationType.valueOf(typeValue.toUpperCase(Locale.ROOT));
        }

        private static String getValidatedType(JsonElement jsonElement, JsonElement typeField) {
            if (Objects.isNull(typeField)) {
                throw new SerializationException(MUST_CONTAIN_TYPE.formatted(jsonElement));
            }
            String typeValue = typeField.getAsString();
            if (!isValidEnum(typeValue)) {
                throw new SerializationException(UNRECOGNIZED_TYPE.formatted(typeValue));
            }
            return typeValue;
        }

        private static boolean isValidEnum(String typeValue) {
            String lowerCaseTypeValue = Optional.of(typeValue).map(s -> s.toLowerCase(Locale.ROOT)).orElse("");
            return MIGRATION_TYPES.contains(lowerCaseTypeValue);
        }
    }


}
