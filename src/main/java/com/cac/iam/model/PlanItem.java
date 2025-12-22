package com.cac.iam.model;

public class PlanItem {

    private Action action;
    private FileCategory fileCategory;
    private String key;
    private String sourcePath;
    private Object payload;
    private Object beforePayload;

    /**
     * Creates an empty plan item placeholder.
     */
    public PlanItem() {
    }

    /**
     * Creates a plan item with all required details.
     *
     * @param action      plan action to take
     * @param fileCategory file category for the payload
     * @param key         logical key for the item
     * @param sourcePath  originating source path
     * @param payload     payload to apply
     */
    public PlanItem(Action action, FileCategory fileCategory, String key, String sourcePath, Object payload) {
        this.action = action;
        this.fileCategory = fileCategory;
        this.key = key;
        this.sourcePath = sourcePath;
        this.payload = payload;
    }

    /**
     * Returns the action to perform.
     *
     * @return plan action
     */
    public Action getAction() {
        return action;
    }

    /**
     * Sets the action to perform.
     *
     * @param action plan action
     */
    public void setAction(Action action) {
        this.action = action;
    }

    /**
     * Returns the file category of the plan item.
     *
     * @return file category
     */
    public FileCategory getFileCategory() {
        return fileCategory;
    }

    /**
     * Sets the file category of the plan item.
     *
     * @param fileCategory file category
     */
    public void setFileCategory(FileCategory fileCategory) {
        this.fileCategory = fileCategory;
    }

    /**
     * Returns the logical key of the plan item.
     *
     * @return key
     */
    public String getKey() {
        return key;
    }

    /**
     * Sets the logical key of the plan item.
     *
     * @param key key value
     */
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * Returns the source path of the plan item.
     *
     * @return source path
     */
    public String getSourcePath() {
        return sourcePath;
    }

    /**
     * Sets the source path of the plan item.
     *
     * @param sourcePath source path
     */
    public void setSourcePath(String sourcePath) {
        this.sourcePath = sourcePath;
    }

    /**
     * Returns the payload for the plan item.
     *
     * @return payload
     */
    public Object getPayload() {
        return payload;
    }

    /**
     * Sets the payload for the plan item.
     *
     * @param payload payload object
     */
    public void setPayload(Object payload) {
        this.payload = payload;
    }

    /**
     * Returns the payload that existed before the action (if available).
     *
     * @return before payload
     */
    public Object getBeforePayload() {
        return beforePayload;
    }

    /**
     * Sets the payload that existed before the action.
     *
     * @param beforePayload before payload object
     */
    public void setBeforePayload(Object beforePayload) {
        this.beforePayload = beforePayload;
    }

}
