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
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.URI;


@Slf4j
public class SshTailFlux {

    private URI uri;
    private int readLineCount;

    private static final StringDecoder decoder = StringDecoder.textPlainOnly();

    private SshClient client;
    private ClientSession session;
    private ChannelExec channelExec;

    public SshTailFlux(SshClient client, URI uri, int readLineCount) {
        this.uri = uri;
        this.readLineCount = readLineCount;
        this.client = client;
    }

    public Flux<String> getFlux() {



            Flux<DataBuffer> sourceBuffer = DataBufferUtils.readInputStream(
                    () -> {
                        String userInfo = uri.getUserInfo();
                        String[] userInfoArray = userInfo.split(":");
                        String username = userInfoArray[0];
                        String password = userInfoArray.length>1 ? userInfoArray[1] : null;
                        int port = uri.getPort() > 0 ? uri.getPort() : 22;
                        PipedOutputStream out = new PipedOutputStream();
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
                                    channelExec = session.createExecChannel("tail -F -n " + readLineCount + " " + uri.getPath());
                                    channelExec.setOut(out);
                                    channelExec.open();
                                } catch (IOException e) {
                                    log.error(e.getMessage(), e.fillInStackTrace());
                                }
                            });
                        } catch (IOException e) {
                            log.error(e.getMessage(), e.fillInStackTrace());
                        }
                        return new NoCloseInputStream(new PipedInputStream(out));
                    },
                    new DefaultDataBufferFactory(),
                    8192);


            Flux<String> source = decoder.decode(
                    sourceBuffer,
                    ResolvableType.forClass(String.class),
                    null,
                    null);

            Flux<String> flux = Flux.create(fluxSink -> {
                new Thread(() -> source.subscribe(fluxSink::next, fluxSink::error, () -> fluxSink.complete())).start();
            });
            flux = flux.doFinally(signalType -> this.stop());
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
