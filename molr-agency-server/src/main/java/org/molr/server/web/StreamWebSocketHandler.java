package org.molr.server.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import static java.util.Objects.requireNonNull;

public class StreamWebSocketHandler extends TextWebSocketHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(StreamWebSocketHandler.class);
    private static final String SESSION_STREAM_SUBSCRIPTION = "SESSION_STREAM_SUBSCRIPTION";

    private final Flux<String> inputStream;

    public StreamWebSocketHandler(Flux<String> inputStream) {
        this.inputStream = requireNonNull(inputStream, "inputStream must not be null.");
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        LOGGER.info("Websocket connection {} initialized", session.getId());
        Disposable subscription = inputStream //
                .publishOn(Schedulers.elastic()) //
                .subscribe(chroma -> sendMessage(session, chroma), e -> LOGGER.error("ERROR", e),
                        () -> LOGGER.info("COMPLETE !!"));

        session.getAttributes().put(SESSION_STREAM_SUBSCRIPTION, subscription);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        LOGGER.info("Websocket connection {} closed with status {}", session.getId(), status);
        ((Disposable) session.getAttributes().get(SESSION_STREAM_SUBSCRIPTION)).dispose();
    }

    private void sendMessage(WebSocketSession session, String chroma) {
        try {
            session.sendMessage(new TextMessage(chroma));
        } catch (Exception e) {
            LOGGER.warn("Websocket connection {}, cannot send message. Attempting to close...", session.getId(), e);
            closeConnection(session);
        }
    }

    private void closeConnection(WebSocketSession session) {
        try {
            session.close();
            LOGGER.info("Websocket connection {} closed", session.getId());
        } catch (Exception e1) {
            LOGGER.warn("Could not close websocket connection {}", session.getId(), e1);
        }
    }
}
