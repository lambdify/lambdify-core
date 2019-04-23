package lambdify.core;

import lombok.*;
import org.junit.jupiter.api.*;

import java.io.*;
import java.nio.charset.*;
import java.nio.file.*;

import static org.junit.jupiter.api.Assertions.*;

class SimplifiedHttpClientTest {

    final String bigCSVFile = readFileContent("tests-resources/dataset.csv");

    @DisplayName("Can read all bytes from an input stream")
    @Test void readBytesFromStream() throws IOException {
        val stream = new FileInputStream( "tests-resources/dataset.csv" );
        val bytes = SimplifiedHttpClient.readBytesFromStream(stream);
        assertEquals( bigCSVFile, new String(bytes) );
    }

    @DisplayName("Can read N bytes from an input stream")
    @Test void readBytesFromStream1() throws IOException {
        val stream = new FileInputStream( "tests-resources/dataset.csv" );
        val bytes = SimplifiedHttpClient.readBytesFromStream(stream, 408732);
        assertEquals( bigCSVFile, new String(bytes, StandardCharsets.US_ASCII) );
    }

    String readFileContent( String fileName ) {
        try {
            val bytes = Files.readAllBytes(Paths.get(fileName));
            return new String(bytes);
        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }
}