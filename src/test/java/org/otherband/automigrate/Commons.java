package org.otherband.automigrate;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public class Commons {
    public static String readResource(String resource) throws IOException {
        return Files.readString(Path.of(getResource(resource).getPath()));
    }

    private static URL getResource(String resource) {
        return Objects.requireNonNull(ClassLoader.getSystemClassLoader().getResource(resource));
    }

}
