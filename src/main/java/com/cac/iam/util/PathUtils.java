package com.cac.iam.util;

import java.nio.file.Path;

public final class PathUtils {

    private PathUtils() {
    }

    /**
     * Returns the filename without its final extension component.
     *
     * @param path path whose basename should be derived
     * @return filename without extension, or the full filename if no dot is present
     */
    public static String baseName(Path path) {
        if (path == null || path.getFileName() == null) {
            return "";
        }
        String name = path.getFileName().toString();
        int lastDot = name.lastIndexOf('.');
        return lastDot > 0 ? name.substring(0, lastDot) : name;
    }

    /**
     * Returns the filename including extension, or empty string when the path is null.
     *
     * @param path path whose filename should be returned
     * @return filename or empty string when null
     */
    public static String fileName(Path path) {
        if (path == null || path.getFileName() == null) {
            return "";
        }
        return path.getFileName().toString();
    }


}
