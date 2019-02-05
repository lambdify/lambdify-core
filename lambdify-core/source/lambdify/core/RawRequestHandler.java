package lambdify.core;

import java.io.*;

public interface RawRequestHandler {

    byte[] handle( byte[] request ) throws IOException;
}
