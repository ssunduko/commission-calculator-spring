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
public class McpResponse {
    @Builder.Default
    private String jsonrpc = "2.0";
    private String id;
    private Object result;
    private McpError error;
}
