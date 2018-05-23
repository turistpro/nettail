package com.github.turistpro.nettail;

import com.jcraft.jsch.JSch;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class LogTailFluxFactory {

    @Autowired
    private JSch jSch;
    private final Map<String, Flux<String>> fluxMap = Collections.synchronizedMap(new HashMap());

    public Flux<String> getInstance(URI uri) {
        synchronized (fluxMap) {
            Flux<String> connectableFlux;
            connectableFlux = fluxMap.get(uri.toString());
            if (connectableFlux == null) {
                Flux<String> flux = new TailJsch(jSch, uri).getFlux();
                connectableFlux = flux
                        .doFinally(signalType -> {
                            synchronized (fluxMap) {
                                fluxMap.remove(uri);
                            }
                            log.info("remove({})", uri.toString());
                        })
                        .publish()
                        .refCount(1)
                        .doFinally(signalType -> {
                            log.info("final({})", signalType);
                        })
                        .doOnSubscribe(subscription -> log.info("downstream={}({})", 1, uri.toString()))
                ;
                fluxMap.put(uri.toString(), connectableFlux);
                log.info("add({})", uri.toString());
            }
            return connectableFlux;
        }
    }
}
