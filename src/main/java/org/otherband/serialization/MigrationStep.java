package org.otherband.serialization;

import org.otherband.MigrationType;

public sealed interface MigrationStep {
    MigrationType type();

    record MethodUseRename(String fromName, String toName) implements MigrationStep {
        @Override
        public MigrationType type() {
            return MigrationType.RENAME_METHOD;
        }
    }

    record TypeRename(String fromName, String toName) implements MigrationStep {
        @Override
        public MigrationType type() {
            return MigrationType.RENAME_TYPE;
        }
    }

}
