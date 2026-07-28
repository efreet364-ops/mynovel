package io.github.novel.mynovel.core.ai.tools;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TerminalOperationToolTest {

    @Test
    void executeTerminalCommand() {
        TerminalOperationTool tool = new TerminalOperationTool();
        String result = tool.executeTerminalCommand("pwd");
        Assertions.assertNotNull(result);
    }
}