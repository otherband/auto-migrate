package org.otherband.serialization;

import org.junit.jupiter.api.Test;
import org.otherband.MigrationType;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.otherband.Commons.readResource;

class MigrationDeserializationTest {


    @Test
    void deserialize() throws IOException {
        String migrationString = readResource("migrations/sample-migration.json");
        MigrationDescription migrationSteps = MigrationDeserialization.fromJson(migrationString);
        assertEquals(1, migrationSteps.migrationSteps().size());
        MigrationStep.MethodUseRename firstStep = (MigrationStep.MethodUseRename) migrationSteps.migrationSteps().get(0);
        assertEquals("of", firstStep.fromName());
        assertEquals("ofElements", firstStep.toName());
        assertEquals(MigrationType.METHOD_USE_RENAME, firstStep.type());
    }

    @Test
    void serialize() throws IOException {
        String expected = readResource("migrations/serilization-result.json");
        List<MigrationStep> migrationSteps = List.of(new MigrationStep.MethodUseRename("of", "ofElements"),
                new MigrationStep.MethodUseRename("ofStuff", "ofThings"));
        MigrationDescription migrationDescription = new MigrationDescription("1.0", migrationSteps);
        String jsonString = MigrationSerializer.serialize(migrationDescription);
        assertEquals(expected, jsonString);
    }

    @Test
    void doesNotContainTypeAttribute() throws IOException {
        String invalidMigration = readResource("migrations/invalid-migration.json");
        SerializationException exception = assertThrows(SerializationException.class, () -> {
            MigrationDeserialization.fromJson(invalidMigration);
        });
        assertEquals("""
                        Could not deserialize migration: object [{"fromName":"of","toName":"ofElements"}] does not contain a 'type' attribute""",
                exception.getMessage());
    }


}