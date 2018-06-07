package com.github.turistpro.nettail;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;

import java.util.HashMap;
import java.util.Map;


@Configuration
@Slf4j
public class NetTailConfig {

    @Autowired
    private WebSocketHandler logWebSocketHandler;

    @Value("${nettail.ssh.key.location}")
    private String sshKeyLocation;

    @Value("${nettail.ssh.key.passphrase}")
    private String sshKeyPassphrase;

    @Bean
    public JSch setupJSch() {
        JSch jSch = new JSch();
        if( sshKeyLocation != null ) {
            try {
                jSch.addIdentity(sshKeyLocation, sshKeyPassphrase);
            } catch (JSchException e) {
                log.error(e.getCause().getMessage());
            }
        }

        return jSch;
    }

    @Bean
    public HandlerMapping webSocketHandlerMapping() {
        Map<String, WebSocketHandler> map = new HashMap<>();
        map.put("/api/tail/**", logWebSocketHandler);
        SimpleUrlHandlerMapping handlerMapping = new SimpleUrlHandlerMapping();
        handlerMapping.setOrder(-1);
        handlerMapping.setUrlMap(map);
        return handlerMapping;
    }

    @Bean
    public WebSocketHandlerAdapter handlerAdapter() {
        return new WebSocketHandlerAdapter();
    }

}
