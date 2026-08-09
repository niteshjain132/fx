package com.projecthub.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Executes Git CLI commands via {@link ProcessBuilder} against a project working directory.
 */
@Service
public class GitService {

    private static final Logger log = LoggerFactory.getLogger(GitService.class);
    private static final int COMMAND_TIMEOUT_SECONDS = 30;

    /**
     * Lists local and remote branches for the given repository directory.
     * Parses {@code git branch -a} into a clean, de-duplicated list of branch names.
     */
    public List<String> listBranches(Path workingDirectory) {
        if (workingDirectory == null || !Files.isDirectory(workingDirectory)) {
            log.warn("Invalid working directory for git branch listing: {}", workingDirectory);
            return List.of();
        }

        try {
            ProcessResult result = run(workingDirectory, "git", "branch", "-a");
            if (result.exitCode() != 0) {
                log.warn("git branch -a failed ({}): {}", result.exitCode(), result.stderr());
                return List.of();
            }
            return parseBranchOutput(result.stdout());
        } catch (Exception ex) {
            log.error("Failed to list git branches in {}", workingDirectory, ex);
            return List.of();
        }
    }

    /**
     * Checks out the selected branch in the background.
     *
     * @return a future completing with {@code true} on success
     */
    public CompletableFuture<Boolean> checkoutBranch(Path workingDirectory, String branchName) {
        return CompletableFuture.supplyAsync(() -> {
            if (workingDirectory == null || branchName == null || branchName.isBlank()) {
                return false;
            }
            String normalized = normalizeBranchName(branchName);
            try {
                ProcessResult result = run(workingDirectory, "git", "checkout", normalized);
                if (result.exitCode() != 0) {
                    log.warn("git checkout {} failed: {}", normalized, result.stderr());
                    return false;
                }
                log.info("Checked out branch '{}' in {}", normalized, workingDirectory);
                return true;
            } catch (Exception ex) {
                log.error("Failed to checkout branch '{}' in {}", normalized, workingDirectory, ex);
                return false;
            }
        });
    }

    public String getCurrentBranch(Path workingDirectory) {
        if (workingDirectory == null || !Files.isDirectory(workingDirectory)) {
            return "";
        }
        try {
            ProcessResult result = run(workingDirectory, "git", "rev-parse", "--abbrev-ref", "HEAD");
            if (result.exitCode() == 0) {
                return result.stdout().trim();
            }
        } catch (Exception ex) {
            log.debug("Could not resolve current branch in {}", workingDirectory, ex);
        }
        return "";
    }

    static List<String> parseBranchOutput(String output) {
        List<String> branches = new ArrayList<>();
        if (output == null || output.isBlank()) {
            return branches;
        }
        for (String rawLine : output.split("\\R")) {
            String line = rawLine.trim();
            if (line.isEmpty() || line.contains("->")) {
                continue;
            }
            if (line.startsWith("*")) {
                line = line.substring(1).trim();
            }
            if (line.startsWith("remotes/")) {
                line = line.substring("remotes/".length());
                int slash = line.indexOf('/');
                if (slash >= 0) {
                    line = line.substring(slash + 1);
                }
            }
            if (!line.isBlank() && !branches.contains(line)) {
                branches.add(line);
            }
        }
        return branches;
    }

    static String normalizeBranchName(String branchName) {
        String name = branchName.trim();
        if (name.startsWith("remotes/")) {
            name = name.substring("remotes/".length());
            int slash = name.indexOf('/');
            if (slash >= 0) {
                name = name.substring(slash + 1);
            }
        }
        return name;
    }

    private ProcessResult run(Path workingDirectory, String... command) throws Exception {
        ProcessBuilder builder = new ProcessBuilder(command);
        builder.directory(workingDirectory.toFile());
        builder.redirectErrorStream(false);
        Process process = builder.start();

        String stdout;
        String stderr;
        try (BufferedReader outReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
             BufferedReader errReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
            stdout = outReader.lines().collect(Collectors.joining("\n"));
            stderr = errReader.lines().collect(Collectors.joining("\n"));
        }

        boolean finished = process.waitFor(COMMAND_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        if (!finished) {
            process.destroyForcibly();
            throw new IllegalStateException("Command timed out: " + String.join(" ", command));
        }
        return new ProcessResult(process.exitValue(), stdout, stderr);
    }

    private record ProcessResult(int exitCode, String stdout, String stderr) {
    }
}
