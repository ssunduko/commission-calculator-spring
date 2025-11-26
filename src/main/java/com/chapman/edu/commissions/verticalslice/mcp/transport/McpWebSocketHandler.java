package com.chapman.edu.commissions.verticalslice.mcp.transport;

import com.chapman.edu.commissions.verticalslice.mcp.protocol.McpRequest;
import com.chapman.edu.commissions.verticalslice.mcp.protocol.McpResponse;
import com.chapman.edu.commissions.verticalslice.mcp.protocol.McpError;
import com.chapman.edu.commissions.verticalslice.mcp.server.McpServerHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Slf4j
@Component
public class McpWebSocketHandler extends TextWebSocketHandler {

    private final McpServerHandler mcpServerHandler;
    private final ObjectMapper objectMapper;

    public McpWebSocketHandler(McpServerHandler mcpServerHandler, ObjectMapper objectMapper) {
        this.mcpServerHandler = mcpServerHandler;
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("WebSocket connection established: {}", session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        log.info("Received message: {}", payload);

        try {
            McpRequest request = objectMapper.readValue(payload, McpRequest.class);
            McpResponse response = mcpServerHandler.handleRequest(request);
            String responseJson = objectMapper.writeValueAsString(response);

            log.info("Sending response: {}", responseJson);
            session.sendMessage(new TextMessage(responseJson));

        } catch (Exception e) {
            log.error("Error processing message", e);
            McpResponse errorResponse = McpResponse.builder()
                .jsonrpc("2.0")
                .error(McpError.parseError(e.getMessage()))
                .build();
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(errorResponse)));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        log.info("WebSocket connection closed: {} with status: {}", session.getId(), status);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("WebSocket transport error for session: {}", session.getId(), exception);
    }
}
