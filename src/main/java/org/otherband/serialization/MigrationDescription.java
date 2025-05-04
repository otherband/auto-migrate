package org.otherband.serialization;

import java.util.List;

public record MigrationDescription(String migrationVersion, List<MigrationStep> migrationSteps) {
}
