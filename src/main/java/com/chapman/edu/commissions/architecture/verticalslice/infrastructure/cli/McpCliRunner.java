package com.chapman.edu.commissions.architecture.verticalslice.infrastructure.cli;

import com.chapman.edu.commissions.architecture.verticalslice.infrastructure.mcp.McpPrompts;
import com.chapman.edu.commissions.architecture.verticalslice.infrastructure.mcp.McpResources;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;

import java.util.*;

/**
 * MCP CLI Runner — Interactive command-line interface for the MCP server.
 *
 * Activated with the "cli" Spring profile:
 *   java -Dspring.profiles.active=cli -jar target/commission-calculator-0.0.1-SNAPSHOT.jar
 *
 * Provides a REPL for exploring and invoking MCP tools, prompts, and
 * resources without needing an external MCP client (Claude Desktop, Inspector).
 *
 * Commands:
 *   help                          — Show available commands
 *   tools                         — List all MCP tools
 *   tool <name> [json-args]       — Invoke a tool
 *   prompts                       — List all MCP prompts
 *   prompt <name>                 — Show prompt details
 *   resources                     — List all MCP resources
 *   resource <uri>                — Read a resource
 *   templates                     — List resource templates
 *   search <keyword>              — Search tools by name/description
 *   exit                          — Exit the CLI
 */
@Configuration
@Profile("cli")
public class McpCliRunner {

    private static final Logger log = LoggerFactory.getLogger(McpCliRunner.class);

    @Bean
    @Order(100)
    public CommandLineRunner mcpCli(List<ToolCallback> tools,
                                     McpPrompts mcpPrompts,
                                     McpResources mcpResources) {
        return args -> {
            var cli = new McpCli(tools, mcpPrompts, mcpResources);
            cli.run();
        };
    }

    /**
     * Interactive MCP CLI implementation.
     */
    static class McpCli {

        private static final String BANNER = """

            ╔══════════════════════════════════════════════════════╗
            ║          MCP Commission Calculator CLI              ║
            ║   Interactive tool for exploring MCP capabilities   ║
            ╚══════════════════════════════════════════════════════╝
            """;
        private static final String PROMPT = "mcp> ";
        private static final String DIVIDER = "─".repeat(50);

        private final Map<String, ToolCallback> toolMap;
        private final List<ToolCallback> toolList;
        private final McpPrompts mcpPrompts;
        private final McpResources mcpResources;
        private final ObjectMapper objectMapper;

        McpCli(List<ToolCallback> tools, McpPrompts mcpPrompts, McpResources mcpResources) {
            this.toolList = tools;
            this.toolMap = new LinkedHashMap<>();
            for (ToolCallback tool : tools) {
                toolMap.put(tool.getToolDefinition().name(), tool);
            }
            this.mcpPrompts = mcpPrompts;
            this.mcpResources = mcpResources;
            this.objectMapper = new ObjectMapper()
                    .enable(SerializationFeature.INDENT_OUTPUT);
        }

        void run() {
            System.out.println(BANNER);
            System.out.println("  " + toolMap.size() + " tools | " +
                    mcpPrompts.getAllPrompts().size() + " prompts | " +
                    mcpResources.getAllResources().size() + " resources");
            System.out.println("  Type 'help' for commands, 'exit' to quit\n");

            try (Scanner scanner = new Scanner(System.in)) {
                while (true) {
                    System.out.print(PROMPT);
                    if (!scanner.hasNextLine()) break;

                    String line = scanner.nextLine().trim();
                    if (line.isEmpty()) continue;

                    String[] parts = line.split("\\s+", 2);
                    String command = parts[0].toLowerCase();
                    String argument = parts.length > 1 ? parts[1].trim() : "";

                    try {
                        switch (command) {
                            case "help", "h", "?" -> printHelp();
                            case "tools", "t" -> listTools();
                            case "tool" -> invokeTool(argument);
                            case "prompts", "p" -> listPrompts();
                            case "prompt" -> showPrompt(argument);
                            case "resources", "r" -> listResources();
                            case "resource" -> readResource(argument);
                            case "templates" -> listTemplates();
                            case "search", "s" -> searchTools(argument);
                            case "exit", "quit", "q" -> {
                                System.out.println("Goodbye!");
                                return;
                            }
                            default -> System.out.println("Unknown command: " + command + ". Type 'help' for commands.");
                        }
                    } catch (Exception e) {
                        System.out.println("Error: " + e.getMessage());
                    }
                }
            }
        }

        // ════════════════════════════════════════════════════
        // Commands
        // ════════════════════════════════════════════════════

        private void printHelp() {
            System.out.println("""

                Available Commands:
                ───────────────────────────────────────────────
                  help                        Show this help
                  tools                       List all MCP tools
                  tool <name> [json-args]     Invoke a tool
                  prompts                     List all MCP prompts
                  prompt <name>               Show prompt details
                  resources                   List all MCP resources
                  resource <uri>              Read a resource by URI
                  templates                   List resource templates
                  search <keyword>            Search tools by name/description
                  exit                        Exit the CLI

                Examples:
                ───────────────────────────────────────────────
                  tool getAllDeals
                  tool getDeal {"id":"deal-1"}
                  tool createDeal {"title":"New Deal","value":50000,"salesRepId":"REP001"}
                  tool convertCurrency {"from":"USD","to":"EUR","amount":1000}
                  resource deals://all
                  resource currency://supported
                  prompt analyze-sales-performance
                  search commission
                """);
        }

        private void listTools() {
            System.out.println("\n  MCP Tools (" + toolMap.size() + " total)");
            System.out.println("  " + DIVIDER);

            // Group by category
            Map<String, List<String>> categories = new LinkedHashMap<>();
            for (var tool : toolList) {
                var def = tool.getToolDefinition();
                String name = def.name();
                String category = categorize(name);
                categories.computeIfAbsent(category, k -> new ArrayList<>())
                        .add(String.format("    %-35s %s", name, truncate(def.description(), 50)));
            }

            for (var entry : categories.entrySet()) {
                System.out.println("\n  [" + entry.getKey() + "]");
                entry.getValue().forEach(System.out::println);
            }
            System.out.println();
        }

        private void invokeTool(String argument) {
            if (argument.isEmpty()) {
                System.out.println("Usage: tool <name> [json-args]");
                System.out.println("  Example: tool getAllDeals");
                System.out.println("  Example: tool createDeal {\"title\":\"Test\",\"value\":50000,\"salesRepId\":\"REP001\"}");
                return;
            }

            String[] parts = argument.split("\\s+", 2);
            String toolName = parts[0];
            String jsonArgs = parts.length > 1 ? parts[1] : "{}";

            ToolCallback tool = toolMap.get(toolName);
            if (tool == null) {
                System.out.println("Tool not found: " + toolName);
                System.out.println("Use 'tools' to list available tools or 'search " + toolName + "' to find matches.");
                return;
            }

            System.out.println("\n  Invoking: " + toolName);
            System.out.println("  Args: " + jsonArgs);
            System.out.println("  " + DIVIDER);

            try {
                String result = tool.call(jsonArgs);
                System.out.println("  Result:");
                System.out.println(prettyPrint(result));
            } catch (Exception e) {
                System.out.println("  Error: " + e.getMessage());
            }
            System.out.println();
        }

        private void listPrompts() {
            var prompts = mcpPrompts.getAllPrompts();
            System.out.println("\n  MCP Prompts (" + prompts.size() + " total)");
            System.out.println("  " + DIVIDER);

            for (var prompt : prompts) {
                String name = (String) prompt.get("name");
                String desc = (String) prompt.get("description");
                System.out.printf("    %-40s %s%n", name, truncate(desc, 45));
            }
            System.out.println("\n  Use 'prompt <name>' to see details.\n");
        }

        @SuppressWarnings("unchecked")
        private void showPrompt(String name) {
            if (name.isEmpty()) {
                System.out.println("Usage: prompt <name>");
                return;
            }

            var prompt = mcpPrompts.getPrompt(name);
            if (prompt == null) {
                System.out.println("Prompt not found: " + name);
                return;
            }

            System.out.println("\n  Prompt: " + name);
            System.out.println("  " + DIVIDER);
            System.out.println("  Description: " + prompt.get("description"));

            var arguments = (List<Map<String, Object>>) prompt.get("arguments");
            if (arguments != null && !arguments.isEmpty()) {
                System.out.println("\n  Arguments:");
                for (var arg : arguments) {
                    String required = Boolean.TRUE.equals(arg.get("required")) ? " (required)" : " (optional)";
                    System.out.printf("    %-25s %s%s%n", arg.get("name"), arg.get("description"), required);
                }
            }

            if (prompt.containsKey("template")) {
                System.out.println("\n  Template:");
                String template = (String) prompt.get("template");
                for (String line : template.split("\n")) {
                    System.out.println("    " + line);
                }
            }
            System.out.println();
        }

        private void listResources() {
            var resources = mcpResources.getAllResources();
            System.out.println("\n  MCP Resources (" + resources.size() + " total)");
            System.out.println("  " + DIVIDER);

            for (var res : resources) {
                System.out.printf("    %-30s %s%n", res.get("uri"), res.get("description"));
            }
            System.out.println("\n  Use 'resource <uri>' to read content.\n");
        }

        private void readResource(String uri) {
            if (uri.isEmpty()) {
                System.out.println("Usage: resource <uri>");
                System.out.println("  Example: resource deals://all");
                return;
            }

            System.out.println("\n  Reading: " + uri);
            System.out.println("  " + DIVIDER);

            var content = mcpResources.getResourceContent(uri);
            if (content.containsKey("error")) {
                System.out.println("  Error: " + content.get("error"));
            } else {
                String data = (String) content.get("content");
                System.out.println("  " + content.get("mimeType"));
                System.out.println(prettyPrint(data));
            }
            System.out.println();
        }

        private void listTemplates() {
            var templates = mcpResources.getAllResourceTemplates();
            System.out.println("\n  Resource Templates (" + templates.size() + " total)");
            System.out.println("  " + DIVIDER);

            for (var tmpl : templates) {
                System.out.printf("    %-40s %s%n", tmpl.get("uriTemplate"), tmpl.get("description"));
            }
            System.out.println("\n  Fill in template params to use: resource deal://my-deal-id\n");
        }

        private void searchTools(String keyword) {
            if (keyword.isEmpty()) {
                System.out.println("Usage: search <keyword>");
                return;
            }

            String lower = keyword.toLowerCase();
            var matches = toolList.stream()
                    .filter(t -> {
                        var def = t.getToolDefinition();
                        return def.name().toLowerCase().contains(lower) ||
                               def.description().toLowerCase().contains(lower);
                    })
                    .toList();

            System.out.println("\n  Search results for '" + keyword + "' (" + matches.size() + " matches)");
            System.out.println("  " + DIVIDER);

            if (matches.isEmpty()) {
                System.out.println("    No matches found.");
            } else {
                for (var tool : matches) {
                    var def = tool.getToolDefinition();
                    System.out.printf("    %-35s %s%n", def.name(), truncate(def.description(), 50));
                }
            }
            System.out.println();
        }

        // ════════════════════════════════════════════════════
        // Helpers
        // ════════════════════════════════════════════════════

        private String categorize(String toolName) {
            String lower = toolName.toLowerCase();
            if (lower.contains("currency") || lower.contains("rate") || lower.contains("supported")) return "Currency";
            if (lower.contains("deal")) return "Deals";
            if (lower.contains("plan") || lower.contains("rule") || lower.contains("activate")) return "Plans";
            if (lower.contains("dispute") || lower.contains("escalate") || lower.contains("resolve")) return "Disputes";
            if (lower.contains("calc") || lower.contains("commission")) return "Calculations";
            if (lower.contains("explain") || lower.contains("analyze") || lower.contains("summarize")) return "Sampling";
            return "Other";
        }

        private String truncate(String text, int maxLen) {
            if (text == null) return "";
            return text.length() <= maxLen ? text : text.substring(0, maxLen - 3) + "...";
        }

        private String prettyPrint(String json) {
            try {
                Object parsed = objectMapper.readValue(json, Object.class);
                return objectMapper.writeValueAsString(parsed)
                        .lines()
                        .map(l -> "    " + l)
                        .reduce("", (a, b) -> a + "\n" + b);
            } catch (JsonProcessingException e) {
                // Not JSON, return as-is
                return "    " + json;
            }
        }
    }
}
