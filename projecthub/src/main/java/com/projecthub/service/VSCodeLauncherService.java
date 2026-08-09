package com.projecthub.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Opens a project directory in Visual Studio Code via the {@code code} CLI,
 * with a macOS-native {@code open -a "Visual Studio Code"} fallback.
 */
@Service
public class VSCodeLauncherService {

    private static final Logger log = LoggerFactory.getLogger(VSCodeLauncherService.class);
    private static final int LAUNCH_TIMEOUT_SECONDS = 15;

    /**
     * Opens the given project path in VS Code.
     *
     * @param projectPath absolute path to the project directory or file
     * @return {@code true} if a launch command was started successfully
     */
    public boolean openInVSCode(Path projectPath) {
        if (projectPath == null || !Files.exists(projectPath)) {
            log.warn("Cannot open VS Code — path does not exist: {}", projectPath);
            return false;
        }

        String absolutePath = projectPath.toAbsolutePath().toString();

        if (tryLaunch(List.of("code", absolutePath))) {
            log.info("Opened project in VS Code via 'code' CLI: {}", absolutePath);
            return true;
        }

        log.info("'code' CLI not available — falling back to macOS open -a Visual Studio Code");
        if (tryLaunch(List.of("open", "-a", "Visual Studio Code", absolutePath))) {
            log.info("Opened project via macOS fallback: {}", absolutePath);
            return true;
        }

        log.error("Failed to open project in VS Code (CLI and macOS fallback both failed): {}", absolutePath);
        return false;
    }

    private boolean tryLaunch(List<String> command) {
        try {
            ProcessBuilder builder = new ProcessBuilder(command);
            builder.redirectErrorStream(true);
            Process process = builder.start();
            boolean finished = process.waitFor(LAUNCH_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (!finished) {
                // code CLI may detach; treat a still-running process as success after start
                return true;
            }
            return process.exitValue() == 0;
        } catch (Exception ex) {
            log.debug("Launch failed for {}: {}", command, ex.toString());
            return false;
        }
    }
}
