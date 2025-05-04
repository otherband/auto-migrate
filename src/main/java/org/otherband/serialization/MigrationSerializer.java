package org.otherband.serialization;

import com.google.gson.Gson;

public class MigrationSerializer {

    private static final Gson GSON = new Gson();

    public static String serialize(MigrationDescription migrationDescription) {
        return GSON.toJson(migrationDescription);
    }
}
