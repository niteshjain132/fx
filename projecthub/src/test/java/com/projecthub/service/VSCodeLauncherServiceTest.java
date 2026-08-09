package com.projecthub.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;

class VSCodeLauncherServiceTest {

    private final VSCodeLauncherService service = new VSCodeLauncherService();

    @Test
    void openInVSCode_returnsFalseForMissingPath() {
        assertFalse(service.openInVSCode(Path.of("/nonexistent/projecthub/path-" + System.nanoTime())));
    }

    @Test
    void openInVSCode_handlesExistingDirectoryWithoutCrashing(@TempDir Path tempDir) throws Exception {
        Files.writeString(tempDir.resolve("README.md"), "# temp");
        // May succeed or fail depending on whether VS Code / code CLI is installed;
        // the important contract is that it does not throw.
        service.openInVSCode(tempDir);
    }
}
