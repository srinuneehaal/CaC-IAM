package com.cac.iam.service.plan.stratagy;


import com.cac.iam.model.FileCategory;
import com.cac.iam.model.LoadedFile;

import java.io.IOException;
import java.nio.file.Path;

public interface FileParsingStrategy {

    /**
     * Returns the category of files supported by this strategy.
     *
     * @return supported file category
     */
    FileCategory getCategory();

    /**
     * Indicates whether the given path is supported by this strategy.
     *
     * @param path candidate path
     * @return true if supported
     */
    boolean supports(Path path);

    /**
     * Parses the provided path into a {@link LoadedFile}.
     *
     * @param path path to parse
     * @return loaded file
     * @throws IOException when parsing fails
     */
    LoadedFile parse(Path path) throws IOException;
}
