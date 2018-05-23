package com.github.turistpro.nettail;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.*;

import java.net.URI;
import java.net.URISyntaxException;


@Component
@Slf4j
public class LogTailWebSocketHanlder implements WebSocketHandler{

    @Autowired private LogTailFluxFactory logTailFluxFactory;

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        try {
            URI uri = new URI(session.getHandshakeInfo().getUri().getPath().substring(10));
            Flux<String> flux = logTailFluxFactory.getInstance(uri);
            session.receive().map(WebSocketMessage::getPayloadAsText).subscribe();
            return session.send(flux.map(session::textMessage));
        } catch (URISyntaxException e) {
            session.send(Mono.just(e.toString()).map(session::textMessage));
        }
        return session.close();
    }
}
