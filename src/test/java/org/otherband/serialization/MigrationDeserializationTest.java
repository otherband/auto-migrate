package org.otherband.serialization;

import org.junit.jupiter.api.Test;
import org.otherband.MigrationType;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.otherband.Commons.readResource;

class MigrationDeserializationTest {

    private final MigrationDeserialization migrationDeserialization = new MigrationDeserialization();

    @Test
    void deserialize() throws IOException {
        String migrationString = readResource("migrations/sample-migration.json");
        MigrationDescription migrationSteps = migrationDeserialization.fromJson(migrationString);
        assertEquals(1, migrationSteps.migrationSteps().size());
        MigrationStep.MethodUseRename firstStep = (MigrationStep.MethodUseRename) migrationSteps.migrationSteps().get(0);
        assertEquals("of", firstStep.fromName());
        assertEquals("ofElements", firstStep.toName());
        assertEquals(MigrationType.METHOD_USE_RENAME, firstStep.type());
    }

    @Test
    void serialize() throws IOException {
        // TODO
    }

}