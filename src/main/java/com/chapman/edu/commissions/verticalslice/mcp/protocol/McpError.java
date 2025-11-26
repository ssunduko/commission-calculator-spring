package com.chapman.edu.commissions.verticalslice.mcp.protocol;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class McpError {
    private Integer code;
    private String message;
    private Object data;

    public static McpError parseError(String message) {
        return McpError.builder()
            .code(-32700)
            .message("Parse error: " + message)
            .build();
    }

    public static McpError invalidRequest(String message) {
        return McpError.builder()
            .code(-32600)
            .message("Invalid request: " + message)
            .build();
    }

    public static McpError methodNotFound(String method) {
        return McpError.builder()
            .code(-32601)
            .message("Method not found: " + method)
            .build();
    }

    public static McpError invalidParams(String message) {
        return McpError.builder()
            .code(-32602)
            .message("Invalid params: " + message)
            .build();
    }

    public static McpError internalError(String message) {
        return McpError.builder()
            .code(-32603)
            .message("Internal error: " + message)
            .build();
    }
}
