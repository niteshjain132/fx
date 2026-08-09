package com.projecthub.service;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GitServiceTest {

    @Test
    void parseBranchOutput_stripsMarkersAndDedupesRemotes() {
        String output = """
                * main
                  feature/platform-core
                  remotes/origin/main
                  remotes/origin/feature/platform-core
                  remotes/origin/HEAD -> origin/main
                  hotfix/auth-leak
                """;

        List<String> branches = GitService.parseBranchOutput(output);

        assertEquals(List.of("main", "feature/platform-core", "hotfix/auth-leak"), branches);
        assertFalse(branches.stream().anyMatch(b -> b.contains("->")));
        assertFalse(branches.stream().anyMatch(b -> b.startsWith("remotes/")));
    }

    @Test
    void normalizeBranchName_stripsRemotePrefix() {
        assertEquals("feature/x", GitService.normalizeBranchName("remotes/origin/feature/x"));
        assertEquals("main", GitService.normalizeBranchName("  main  "));
    }

    @Test
    void parseBranchOutput_handlesEmpty() {
        assertTrue(GitService.parseBranchOutput("").isEmpty());
        assertTrue(GitService.parseBranchOutput(null).isEmpty());
    }
}
