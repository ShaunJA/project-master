package utils;

import java.io.*;
import java.nio.charset.Charset;

/**
 * Created by ShaunAJ on 2017/7/11.
 */
public class StreamUtils {
    public static final int BUFFER_SIZE = 4096;

    public static String copyToString(InputStream in, Charset charset) throws IOException {
        if(in == null) {
            throw new IllegalArgumentException("No InputStream specified");
        }
        StringBuilder out = new StringBuilder();
        InputStreamReader reader = new InputStreamReader(in, charset);
        char[] buffer = new char[BUFFER_SIZE];
        int bytesRead = -1;
        while ((bytesRead = reader.read(buffer)) != -1) {
            out.append(buffer, 0, bytesRead);
        }
        return out.toString();
    }

    public static byte[] copyToByteArray(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream(BUFFER_SIZE);
        copy(in, out);
        return out.toByteArray();
    }

    public static int copy(InputStream in, OutputStream out) throws IOException {
        if(null == in) {
            throw new IllegalArgumentException("No InputStream specified");
        }

        if(null == out) {
            throw new IllegalArgumentException("No OutputStream specified");
        }

        int byteCount = 0;
        byte[] buffer = new byte[BUFFER_SIZE];
        int bytesRead = -1;
        while ((bytesRead = in.read(buffer)) != -1) {
            out.write(buffer, 0, bytesRead);
            byteCount += bytesRead;
        }
        out.flush();
        return byteCount;
    }
}
