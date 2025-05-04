package org.otherband.serialization;

import org.otherband.MigrationType;

public interface MigrationStep {
    MigrationType type();

    record MethodUseRename(MigrationType type, String fromName, String toName) implements MigrationStep {
        public MethodUseRename(String fromName, String toName) {
            this(MigrationType.METHOD_USE_RENAME, fromName, toName);
        }
    }

}
