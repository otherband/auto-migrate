package org.otherband.automigrate.serialization;

import org.junit.jupiter.api.Test;
import org.otherband.automigrate.MigrationType;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.otherband.automigrate.Commons.readResource;

class MigrationSerializationTest {


    @Test
    void deserialize() throws IOException {
        String migrationString = readResource("migrations/sample-migration.json");
        MigrationDescription migrationDescription = MigrationDeserializer.fromJson(migrationString);
        List<MigrationStep> steps = migrationDescription.migrationSteps();
        assertEquals(2, steps.size());

        MigrationStep.MethodUseRename firstStep = (MigrationStep.MethodUseRename) steps.get(0);
        assertEquals("of", firstStep.fromName());
        assertEquals("ofElements", firstStep.toName());
        assertEquals(MigrationType.RENAME_METHOD, firstStep.type());

        MigrationStep.TypeRename secondStep = (MigrationStep.TypeRename) steps.get(1);
        assertEquals("List", secondStep.fromName());
        assertEquals("ListOfElements", secondStep.toName());
        assertEquals(MigrationType.RENAME_TYPE, secondStep.type());

    }

    @Test
    void serialize() throws IOException {
        String expected = readResource("migrations/serialization-result.json");
        List<MigrationStep> migrationSteps = List.of(new MigrationStep.MethodUseRename("of", "ofElements"),
                new MigrationStep.TypeRename("ofStuff", "ofThings"));
        MigrationDescription migrationDescription = new MigrationDescription("1.0", migrationSteps);
        String jsonString = MigrationSerializer.serialize(migrationDescription);
        assertEquals(expected, jsonString);
    }

    @Test
    void doesNotContainTypeAttribute() throws IOException {
        String invalidMigration = readResource("migrations/migration-with-no-type.json");
        SerializationException exception = assertThrows(SerializationException.class, () -> MigrationDeserializer.fromJson(invalidMigration));
        assertEquals("""
                        Could not deserialize migration: object [{"fromName":"of","toName":"ofElements"}] does not contain a 'type' attribute""",
                exception.getMessage());
    }

    @Test
    void invalidTypeAttribute() throws IOException {
        String invalidMigration = readResource("migrations/migration-with-invalid-type.json");
        SerializationException exception = assertThrows(SerializationException.class, () -> MigrationDeserializer.fromJson(invalidMigration));
        assertEquals("Unrecognized migration step type 'DOES_NOT_EXIST'",
                exception.getMessage());
    }


}