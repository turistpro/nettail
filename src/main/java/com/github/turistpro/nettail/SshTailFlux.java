package com.github.turistpro.nettail;

import lombok.extern.slf4j.Slf4j;
import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.channel.ChannelExec;
import org.apache.sshd.client.future.ConnectFuture;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.common.util.io.NoCloseInputStream;
import org.springframework.core.ResolvableType;
import org.springframework.core.codec.StringDecoder;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.URI;


@Slf4j
public class SshTailFlux {

    private final URI uri;
    private final String command;

    private static final StringDecoder decoder = StringDecoder.textPlainOnly();
    private static final DataBufferFactory dataBufferFactory =  new DefaultDataBufferFactory();
    private static final int BUFFER_SIZE = 8192;

    private SshClient client;
    private ClientSession session;
    private ChannelExec channelExec;

    public SshTailFlux(SshClient client, URI uri, int readLineCount) {
        this.uri = uri;
        this.client = client;
        this.command = "tail -F -n " + readLineCount + " " + uri.getPath();
    }

    public Flux<String> getFlux() {

        String userInfo = uri.getUserInfo();
        if (userInfo == null) {
            return Flux.just("UserInfo required.");
        }
        String[] userInfoArray = userInfo.split(":");
        String username = userInfoArray[0];
        String password = userInfoArray.length > 1 ? userInfoArray[1] : null;
        int port = uri.getPort() > 0 ? uri.getPort() : 22;


        Flux<DataBuffer> source = DataBufferUtils.readInputStream(
                () -> {

                    PipedOutputStream out = new PipedOutputStream();
                    PipedInputStream in = new PipedInputStream(out);
                    try {
                        ConnectFuture connectFuture = client.connect(username, uri.getHost(), port)
                                .verify(5000);

                        connectFuture.addListener(future -> {
                            session = future.getSession();
                            if (password != null) {
                                session.addPasswordIdentity(password);
                            }
                            try {
                                session.auth().verify(5000);
                                channelExec = session.createExecChannel(command);
                                channelExec.setOut(out);
                                channelExec.open();
                            } catch (IOException e) {
                                log.error(e.getMessage(), e.fillInStackTrace());
                                try {
                                    out.write(e.getMessage().getBytes());
                                    out.flush();
                                    out.close();
                                    in.close();
                                    this.stop();
                                } catch (IOException error) {
                                    log.error(error.getMessage(), error.fillInStackTrace());
                                }
                            }
                        });
                    } catch (IOException e) {
                        log.error(e.getMessage(), e.fillInStackTrace());
                        out.write(e.getMessage().getBytes());
                        out.flush();
                        out.close();
                        in.close();
                        this.stop();
                    }
                    return new NoCloseInputStream(in);
                },
                dataBufferFactory,
                BUFFER_SIZE).doFinally(signalType -> this.stop());


        Flux<String> flux = decoder.decode(
                source,
                ResolvableType.forClass(String.class),
                null,
                null)
                .subscribeOn(Schedulers.elastic());

        return flux;


    }

    private void stop() {
        try {
            if (channelExec != null)
                channelExec.close();
            if (session != null)
                session.disconnect(0, "complete");
        } catch (IOException e) {
            log.error(e.getMessage(), e.fillInStackTrace());
        }

    }
}
