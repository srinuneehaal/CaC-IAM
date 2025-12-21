package com.cac.iam.util;

import java.util.Arrays;
import java.util.Objects;

public final class CommandLineFlags {

    private CommandLineFlags() {
    }

    /**
     * Checks whether the provided flag is present in the supplied arguments, ignoring case and trimming whitespace.
     *
     * @param args full argument array (may be null or empty)
     * @param flag flag value to search for (e.g. {@code --plan})
     * @return true if the flag appears in the array; otherwise false
     */
    public static boolean hasFlag(String[] args, String flag) {
        if (args == null || args.length == 0 || flag == null) {
            return false;
        }
        return Arrays.stream(args)
                .filter(Objects::nonNull)
                .map(String::trim)
                .anyMatch(flag::equalsIgnoreCase);
    }
}
