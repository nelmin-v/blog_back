package com.nelmin.balancer.config;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.handler.timeout.ReadTimeoutException;
import io.netty.handler.timeout.WriteTimeoutException;
import org.springframework.boot.web.embedded.netty.NettyReactiveWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.stereotype.Component;

import static io.netty.handler.timeout.IdleStateEvent.WRITER_IDLE_STATE_EVENT;

@Component
public class NettyWebServerFactoryPortCustomizer  implements WebServerFactoryCustomizer<NettyReactiveWebServerFactory> {
    int allIdleTimeSeconds = 61; // сек
    @Override
    public void customize(NettyReactiveWebServerFactory factory) {
        // https://projectreactor.io/docs/netty/release/reference/index.html#_http_access_log
        factory.addServerCustomizers(server ->
                // свой регистратор метрик

                server.doOnConnection(connection ->
                        connection.addHandler(
                                new IdleStateHandler(
                                        0, 0, allIdleTimeSeconds
                                ) {
                                    @Override
                                    protected void channelIdle(
                                            ChannelHandlerContext ctx,
                                            IdleStateEvent evt
                                    ) {
                                        ctx.fireExceptionCaught(
                                                evt.state() == WRITER_IDLE_STATE_EVENT.state()
                                                        ? WriteTimeoutException.INSTANCE
                                                        : ReadTimeoutException.INSTANCE
                                        );
                                        ctx.write(new CloseWebSocketFrame());
                                        ctx.close();
                                    }
                                }
                        )
                )
        );


        // https://www.baeldung.com/spring-boot-reactor-netty
        // https://piotrminkowski.com/2021/05/24/ssl-with-spring-webflux-and-vault-pki/
//        Ssl ssl = new Ssl();
//        ssl.setEnabled(true);
//        ssl.setKeyStore("classpath:sample.jks");
//        ssl.setKeyAlias("alias");
//        ssl.setKeyPassword("password");
//        ssl.setKeyStorePassword("secret");
//
//        serverFactory.setSsl(ssl);
//        Http2 http2 = new Http2();
//        http2.setEnabled(false);
//        serverFactory.addServerCustomizers(new SslServerCustomizer(ssl, http2, null));
//        serverFactory.setPort(8443);
//
//        serverFactory.setPort(8088);
    }
}
