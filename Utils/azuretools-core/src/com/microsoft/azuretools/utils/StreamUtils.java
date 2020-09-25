package com.microsoft.azuretools.utils;

import org.apache.commons.lang3.StringUtils;

import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class StreamUtils {

    public static String toString(OutputStream outputStream, Charset charset) throws UnsupportedEncodingException {
        if (outputStream == null) {
            return StringUtils.EMPTY;
        }
        return new String(outputStream.toString().getBytes(), charset.name());
    }

    public static String toString(OutputStream outputStream) throws UnsupportedEncodingException {
       return StreamUtils.toString(outputStream, StandardCharsets.UTF_8);
    }
}
