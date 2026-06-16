package net.nyana.reflection.util;

import java.io.*;
import java.nio.charset.StandardCharsets;

final class StreamUtils {
    private StreamUtils() {}

    public static String firstLine(final InputStream is) {
        try {
            is.mark(1024);
            final BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            final String line = reader.readLine();
            is.reset();
            return line;
        } catch (final IOException e) {
            throw new UncheckedIOException("Failed to read first line of input stream", e);
        }
    }
}
