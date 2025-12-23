package com.cac.iam.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.nio.file.Path;
import java.util.Objects;

/**
 * Centralised holder for filesystem locations so directory and file names
 * can be tweaked via application.properties without code changes.
 */
@Component
@ConfigurationProperties(prefix = "cacex.paths")
@Validated
public class FileLocationProperties {

    @NotBlank
    private String changedFilesDir ;
    @NotBlank
    private String planDir;
    @NotBlank
    private String masterPlanFile;
    private String policiesDirName = "policies";
    private String rolesDirName = "roles";
    private String usersDirName = "users";

    private EnvironmentLookup envLookup = System::getenv;

    /**
     * Returns the configured changed files directory.
     *
     * @return changed files directory
     */
    public String getChangedFilesDir() {
        return changedFilesDir;
    }

    /**
     * Sets the root directory containing changed files.
     *
     * @param changedFilesDir directory name or path
     */
    public void setChangedFilesDir(String changedFilesDir) {
        this.changedFilesDir = changedFilesDir;
    }

    /**
     * Returns the configured plan directory.
     *
     * @return plan directory
     */
    public String getPlanDir() {
        return planDir;
    }

    /**
     * Sets the directory where plan files are written.
     *
     * @param planDir directory name or path
     */
    public void setPlanDir(String planDir) {
        this.planDir = planDir;
    }

    /**
     * Returns the master plan filename.
     *
     * @return master plan filename
     */
    public String getMasterPlanFile() {
        return masterPlanFile;
    }

    /**
     * Sets the master plan filename.
     *
     * @param masterPlanFile file name for the master plan
     */
    public void setMasterPlanFile(String masterPlanFile) {
        this.masterPlanFile = masterPlanFile;
    }

    public String getPoliciesDirName() {
        return policiesDirName;
    }

    public void setPoliciesDirName(String policiesDirName) {
        this.policiesDirName = policiesDirName;
    }

    public String getRolesDirName() {
        return rolesDirName;
    }

    public void setRolesDirName(String rolesDirName) {
        this.rolesDirName = rolesDirName;
    }

    public String getUsersDirName() {
        return usersDirName;
    }

    public void setUsersDirName(String usersDirName) {
        this.usersDirName = usersDirName;
    }

    /**
     * Resolves the changed files root as a {@link Path}.
     *
     * @return path to changed files
     */
    public Path changedFilesRoot() {
        return Path.of(changedFilesDir);
    }

    /**
     * Resolves the plan directory path, applying environment overrides when present.
     *
     * @return plan directory path
     */
    public Path planDirPath() {
        return Path.of(resolveFromEnv("PLAN_DIR", planDir));
    }

    /**
     * Resolves the full master plan file path, applying environment overrides when present.
     *
     * @return master plan path
     */
    public Path masterPlanPath() {
        String fileName = resolveFromEnv("MASTER_PLAN_FILE", masterPlanFile);
        return planDirPath().resolve(fileName);
    }

    private String resolveFromEnv(String envKey, String fallback) {
        String value = envLookup.lookup(envKey);
        return value != null && !value.isBlank() ? value : fallback;
    }

    /**
     * Test hook to override environment lookup without touching real env vars.
     */
    public void setEnvLookup(EnvironmentLookup envLookup) {
        this.envLookup = Objects.requireNonNull(envLookup);
    }
}
