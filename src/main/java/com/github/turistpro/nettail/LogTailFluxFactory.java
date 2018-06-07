package com.github.turistpro.nettail;

import com.jcraft.jsch.JSch;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.net.URI;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class LogTailFluxFactory {

    @Value("${nettail.bufferSize:100}")
    private int bufferSize;

    @Value("${nettail.connectionTimeout:0}")
    private long connectionTimeout;

    @Autowired
    private JSch jSch;
    private final Map<String, Flux<String>> fluxMap = Collections.synchronizedMap(new HashMap());

    public Flux<String> getInstance(URI uri) {
        synchronized (fluxMap) {
            Flux<String> connectableFlux;
            connectableFlux = fluxMap.get(uri.toString());
            if (connectableFlux == null) {
                Flux<String> flux = new TailJsch(jSch, uri, bufferSize).getFlux();
                connectableFlux = flux
                        .doFinally(signalType -> {
                            synchronized (fluxMap) {
                                fluxMap.remove(uri);
                            }
                            log.info("remove({})", uri.toString());
                        })
                        .replay(bufferSize)
                        .refCount(1, Duration.ofSeconds(connectionTimeout))
                        .doFinally(signalType -> {
                            log.info("final({})", signalType);
                        })
                        .doOnSubscribe(subscription -> log.info("subcribe=({})", uri.toString()))
                ;
                fluxMap.put(uri.toString(), connectableFlux);
                log.info("add({})", uri.toString());
            }
            return connectableFlux;
        }
    }
}
