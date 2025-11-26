package com.chapman.edu.commissions.verticalslice.mcp.protocol;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class McpRequest {
    @Builder.Default
    private String jsonrpc = "2.0";
    private String id;
    private String method;
    private Map<String, Object> params;
}
