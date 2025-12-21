package com.cac.iam.service.plan.stratagy;

import com.cac.iam.exception.UnsupportedFileCategoryException;
import com.cac.iam.exception.UnsupportedFilePathException;
import com.cac.iam.model.FileCategory;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class FileParsingStrategyFactory {

    private final Map<FileCategory, FileParsingStrategy> strategies = new EnumMap<>(FileCategory.class);

    /**
     * Builds a factory with discovered parsing strategies.
     *
     * @param discoveredStrategies strategies to register
     */
    public FileParsingStrategyFactory(List<FileParsingStrategy> discoveredStrategies) {

        for (FileParsingStrategy strategy : discoveredStrategies) {
            strategies.put(strategy.getCategory(), strategy);
        }
    }

    /**
     * Resolves the parsing strategy that supports the given path.
     *
     * @param path file path to parse
     * @return matching parsing strategy
     * @throws com.cac.iam.exception.UnsupportedFilePathException if none match
     */
    public FileParsingStrategy resolve(Path path) {

        for (FileParsingStrategy strategy : strategies.values()) {
            if (strategy.supports(path)) {
                return strategy;
            }
        }
        throw new UnsupportedFilePathException("Unsupported file path: " + path);
    }

    /**
     * Resolves a parsing strategy by category.
     *
     * @param category file category to resolve
     * @return parsing strategy for the category
     * @throws UnsupportedFileCategoryException if no strategy is registered
     */
    public FileParsingStrategy resolve(FileCategory category) {
        FileParsingStrategy strategy = strategies.get(category);
        if (strategy == null) {
            throw new UnsupportedFileCategoryException("Unsupported file category: " + category);
        }
        return strategy;
    }
}
