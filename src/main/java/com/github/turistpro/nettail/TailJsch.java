package com.github.turistpro.nettail;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.Hashtable;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class TailJsch implements Runnable {

    private Thread worker;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private URI uri;
    private InputStream m_in;
    private FluxSink<String> stringFluxSink;
    private Flux<String> flux;

    private boolean stopFlag = false;

    private JSch jsch;

    public TailJsch(JSch jsch, URI uri) {
        this.jsch = jsch;
        this.uri = uri;
        this.flux = Flux.create(stringFluxSink -> {
            this.stringFluxSink = stringFluxSink;
        });
        this.flux = flux
                .doFinally(signalType -> {
                    this.stop();
                    log.info("stop({})", uri.toString());
                })
                .doOnSubscribe(subscription -> {
                    log.info("start({})", uri.toString());
                    this.start();
                });
    }

    public void start() {
        worker = new Thread(this);
        running.set(true);
        worker.start();
    }

    public void stop() {
        running.set(false);
    }

    public Flux<String> getFlux() {
        return flux;
    }


    @Override
    public void run() {
        try {
            String userInfo = uri.getUserInfo();
            if(userInfo==null) {
                stringFluxSink.next("User is not set");
                stringFluxSink.complete();
                return;
            }
            String[] userInfoArray = userInfo.split(":");
            String username = userInfoArray[0];
            Session session = jsch.getSession(username, uri.getHost());
            if(userInfoArray.length>1) {
                // set password
                session.setPassword(userInfoArray[1]);
            }
            Hashtable<String, String> config = new Hashtable<String, String>();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            session.connect(5000);
            session.setServerAliveInterval(5000);


            ChannelExec m_channelExec = (ChannelExec) session.openChannel("exec");
            m_channelExec.setPty(true);
            String cmd = "tail -f " + uri.getPath();
            m_channelExec.setCommand(cmd);
            m_in = m_channelExec.getInputStream();
            m_channelExec.connect();
            BufferedReader m_bufferedReader = new BufferedReader(new InputStreamReader(m_in));
            int i = 0;
            while (running.get()) {
                if (m_bufferedReader.ready()) {
                    String line = m_bufferedReader.readLine();
                    stringFluxSink.next(line);
                }
            }
            m_bufferedReader.close();
            m_channelExec.sendSignal("SIGINT");
            m_channelExec.disconnect();
            session.disconnect();
            stringFluxSink.complete();
            log.info("completeStop({})", uri.toString());
        } catch (Exception e) {
            if(stringFluxSink!=null) {
                stringFluxSink.error(e);
                stringFluxSink.complete();
            }
            log.error(e.getMessage(), e.getCause());
        }
    }
}
