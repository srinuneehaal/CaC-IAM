package com.cac.iam.model;

import java.nio.file.Path;
import java.util.Objects;

public class LoadedFile {

    private final FileCategory category;
    private final String key;
    private final Path path;
    private final Object payload;

    /**
     * Creates a new loaded file wrapper.
     *
     * @param category file category
     * @param key      logical key for the file
     * @param path     source path on disk
     * @param payload  deserialized payload content
     */
    public LoadedFile(FileCategory category, String key, Path path, Object payload) {
        this.category = category;
        this.key = key;
        this.path = path;
        this.payload = payload;
    }

    /**
     * Returns the file category.
     *
     * @return category
     */
    public FileCategory getCategory() {
        return category;
    }

    /**
     * Returns the logical key for the file.
     *
     * @return key
     */
    public String getKey() {
        return key;
    }

    /**
     * Returns the underlying path.
     *
     * @return file path
     */
    public Path getPath() {
        return path;
    }

    /**
     * Returns the deserialized payload.
     *
     * @return payload object
     */
    public Object getPayload() {
        return payload;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LoadedFile that = (LoadedFile) o;
        return category == that.category && Objects.equals(key, that.key)
                && Objects.equals(payload, that.payload);
    }

    @Override
    public int hashCode() {
        return Objects.hash(category, key, payload);
    }

    @Override
    public String toString() {
        return "LoadedFile{" +
                "category=" + category +
                ", key='" + key + '\'' +
                ", path=" + path +
                ", payload=" + payload +
                '}';
    }
}
