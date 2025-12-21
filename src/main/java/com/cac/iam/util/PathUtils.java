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
        String name = path.getFileName().toString();
        int lastDot = name.lastIndexOf('.');
        return lastDot > 0 ? name.substring(0, lastDot) : name;
    }


}
