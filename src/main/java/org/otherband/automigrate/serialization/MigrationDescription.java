package org.otherband.automigrate.serialization;

import java.util.List;

public record MigrationDescription(String migrationVersion, List<MigrationStep> migrationSteps) {
}
