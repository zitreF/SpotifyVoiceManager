package me.fertiz.spotifyvoice.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.Map;
import java.util.Objects;

public class ResourceUtil {

    public static Path extractResourceDirectory(String resourceDir, Class<?> clazz) throws IOException {
        try {
            URI uri = Objects.requireNonNull(
                    clazz.getResource("/" + resourceDir),
                    "Resource not found: " + resourceDir
            ).toURI();

            Path tempDir = Files.createTempDirectory(resourceDir);
            tempDir.toFile().deleteOnExit();

            if ("jar".equals(uri.getScheme())) {
                String jarPath = uri.toString().split("!")[0];
                try (FileSystem fs = FileSystems.newFileSystem(URI.create(jarPath), Map.of())) {
                    Path resourcePath = fs.getPath("/" + resourceDir);
                    copyRecursive(resourcePath, tempDir);
                }
            } else {
                Path resourcePath = Paths.get(uri);
                copyRecursive(resourcePath, tempDir);
            }

            return tempDir;
        } catch (URISyntaxException e) {
            throw new IOException("Invalid resource URI", e);
        }
    }

    private static void copyRecursive(Path source, Path target) throws IOException {
        try (var stream = Files.walk(source)) {
            stream.forEach(path -> {
                try {
                    Path targetPath = target.resolve(source.relativize(path).toString());
                    if (Files.isDirectory(path)) {
                        Files.createDirectories(targetPath);
                    } else {
                        try (InputStream in = Files.newInputStream(path)) {
                            Files.createDirectories(targetPath.getParent());
                            Files.copy(in, targetPath, StandardCopyOption.REPLACE_EXISTING);
                        }
                    }
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        }
    }
}
