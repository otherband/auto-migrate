package org.otherband.serialization;

import org.otherband.MigrationType;

public interface MigrationStep {
    MigrationType type();

    record MethodUseRename(MigrationType type, String fromName, String toName) implements MigrationStep {
    }

}
