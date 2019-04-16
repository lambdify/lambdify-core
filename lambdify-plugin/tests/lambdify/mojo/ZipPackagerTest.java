package lambdify.mojo;

import lombok.*;
import org.junit.jupiter.api.*;

import java.io.*;
import java.nio.file.*;

class ZipPackagerTest {

    @SneakyThrows
    @Test void addExecutableFile()
    {
        val zipFile = Paths.get( "output/package.zip" ).toAbsolutePath().toString();
        val zipContentFolder = Paths.get( "output/zip content" ).toAbsolutePath().toString();
        try (
            @NonNull val bootstrap = getClass().getResourceAsStream("/bootstrap");
            @NonNull val zip = new ZipPackager( zipFile, zipContentFolder );
        ) {
            zip.addExecutableFile("bootstrap", bootstrap);
        }
    }
}
