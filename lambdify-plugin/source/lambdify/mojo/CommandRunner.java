package lambdify.mojo;

import lombok.*;
import lombok.experimental.*;
import lombok.var;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.*;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;

@UtilityClass
class CommandRunner {

    ExecutorService executor = Executors.newFixedThreadPool(2);

    Command command( String command, String...args ) {
        command = ( command.contains( File.separator ) ) ? command : discoveryExecutable( command );
        return new Command().command(command).args(args);
    }

    private void run( File workDir, String command, String...args ){
        val parsedArgs = String.join(" ", args);
        val commandLine = format("%s %s", command, parsedArgs);

        try {
            val p = Runtime.getRuntime().exec( commandLine, new String[0], workDir );
            executor.submit( () -> printTo( p.getInputStream(), System.out ) );
            executor.submit( () -> printTo( p.getErrorStream(), System.err ) );
            p.waitFor();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void printTo( InputStream input, PrintStream output ) {
        try (val stream = new BufferedReader(new InputStreamReader(input))) {
            var s = "";
            while ((s = stream.readLine()) != null) {
                output.println(s);
            }
        } catch ( IOException cause ) {
            throw new RuntimeException(cause);
        }
    }

    private String discoveryExecutable( String executable ) {
        val path = System.getenv("PATH");
        val pathEntries = path.split( File.pathSeparator );

        for ( val entry : pathEntries ) {
            val file = new File( entry, executable );
            if ( file.exists() )
                return file.getAbsolutePath();
        }

        return executable;
    }

    @Accessors(fluent = true)
    @Data class Command {

        String[] args;
        String command;
        String workingDirectory;

        void run(){
            CommandRunner.run(
                new File(workingDirectory),
                command, args
            );
        }
    }
}
