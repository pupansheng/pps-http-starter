/*
 * Copyright (c) ACCA Corp.
 * All Rights Reserved.
 */
package com.pps.http.myrequest.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelConfig;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.SocketChannelConfig;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.timeout.ReadTimeoutHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.AsyncClientHttpRequest;
import org.springframework.http.client.AsyncClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import javax.net.ssl.SSLException;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.TimeUnit;

/**
 * @author pupansheng, 2021/3/29
 * @version OPRA v1.0
 */
@Slf4j
public class MyNetty4ClientHttpRequestFactory implements ClientHttpRequestFactory,
        AsyncClientHttpRequestFactory, InitializingBean, DisposableBean {
    /**
     * The default maximum response size.
     * @see #setMaxResponseSize(int)
     */
    public static final int DEFAULT_MAX_RESPONSE_SIZE = 1024 * 1024 * 10;


    private final EventLoopGroup eventLoopGroup;

    private final boolean defaultEventLoopGroup;

    private int maxResponseSize = DEFAULT_MAX_RESPONSE_SIZE;

    @Nullable
    private SslContext sslContext;

    private int connectTimeout = -1;

    private int readTimeout = -1;

    private HttpVersion httpVersion=HttpVersion.HTTP_1_1;
    @Nullable
    private volatile Bootstrap bootstrap;

    private int ioWorkerCount=1;


    /**
     * Create a new {@code Netty4ClientHttpRequestFactory} with a default
     * {@link NioEventLoopGroup}.
     */
    public MyNetty4ClientHttpRequestFactory() {
        this.eventLoopGroup = new NioEventLoopGroup(ioWorkerCount);
        this.defaultEventLoopGroup = true;
    }

    /**
     * Create a new {@code Netty4ClientHttpRequestFactory} with the given
     * {@link EventLoopGroup}.
     * <p><b>NOTE:</b> the given group will <strong>not</strong> be
     * {@linkplain EventLoopGroup#shutdownGracefully() shutdown} by this factory;
     * doing so becomes the responsibility of the caller.
     */
    public MyNetty4ClientHttpRequestFactory(EventLoopGroup eventLoopGroup) {
        Assert.notNull(eventLoopGroup, "EventLoopGroup must not be null");
        this.eventLoopGroup = eventLoopGroup;
        this.defaultEventLoopGroup = false;
    }

    public MyNetty4ClientHttpRequestFactory(HttpVersion httpVersion) {
        this();
        this.httpVersion=httpVersion;
    }

    public MyNetty4ClientHttpRequestFactory(HttpVersion httpVersion,int connectTimeout,int readTimeout,int theadNum) {
        this();
        this.readTimeout=readTimeout;
        this.connectTimeout=connectTimeout;
        this.httpVersion=httpVersion;
        this.ioWorkerCount=theadNum;
    }

    /**
     * Set the default maximum response size.
     * <p>By default this is set to {@link #DEFAULT_MAX_RESPONSE_SIZE}.
     * @see HttpObjectAggregator#HttpObjectAggregator(int)
     * @since 4.1.5
     */
    public void setMaxResponseSize(int maxResponseSize) {
        this.maxResponseSize = maxResponseSize;
    }

    /**
     * Set the SSL context. When configured it is used to create and insert an
     * {@link io.netty.handler.ssl.SslHandler} in the channel pipeline.
     * <p>A default client SslContext is configured if none has been provided.
     */
    public void setSslContext(SslContext sslContext) {
        this.sslContext = sslContext;
    }

    /**
     * Set the underlying connect timeout (in milliseconds).
     * A timeout value of 0 specifies an infinite timeout.
     * @see ChannelConfig#setConnectTimeoutMillis(int)
     */
    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    /**
     * Set the underlying URLConnection's read timeout (in milliseconds).
     * A timeout value of 0 specifies an infinite timeout.
     * @see ReadTimeoutHandler
     */
    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }


    @Override
    public void afterPropertiesSet() {
        if (this.sslContext == null) {
            this.sslContext = getDefaultClientSslContext();
        }
    }

    private SslContext getDefaultClientSslContext() {
        try {
            return SslContextBuilder.forClient().build();
        }
        catch (SSLException ex) {
            throw new IllegalStateException("Could not create default client SslContext", ex);
        }
    }


    @Override
    public ClientHttpRequest createRequest(URI uri, HttpMethod httpMethod) throws IOException {
        return createRequestInternal(uri, httpMethod);
    }

    @Override
    public AsyncClientHttpRequest createAsyncRequest(URI uri, HttpMethod httpMethod) throws IOException {
        return createRequestInternal(uri, httpMethod);
    }

    private MyNetty4ClientHttpRequest createRequestInternal(URI uri, HttpMethod httpMethod) {
        return new MyNetty4ClientHttpRequest(getBootstrap(uri), uri, httpMethod,httpVersion);
    }

    private Bootstrap getBootstrap(URI uri) {
        boolean isSecure = (uri.getPort() == 443 || "https".equalsIgnoreCase(uri.getScheme()));
        if (isSecure) {
            return buildBootstrap(uri, true);
        }
        else {
            Bootstrap bootstrap = this.bootstrap;
            if (bootstrap == null) {
                bootstrap = buildBootstrap(uri, false);
                this.bootstrap = bootstrap;
            }
            return bootstrap;
        }
    }

    private Bootstrap buildBootstrap(URI uri, boolean isSecure) {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(this.eventLoopGroup).channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel channel) throws Exception {
                        configureChannel(channel.config());
                        ChannelPipeline pipeline = channel.pipeline();
                        if (isSecure) {
                            Assert.notNull(sslContext, "sslContext should not be null");
                            pipeline.addLast(sslContext.newHandler(channel.alloc(), uri.getHost(), uri.getPort()));
                        }
                        pipeline.addLast(new HttpClientCodec());
                        pipeline.addLast(new HttpObjectAggregator(maxResponseSize));
                        if (readTimeout > 0) {
                            pipeline.addLast(new ReadTimeoutHandler(readTimeout,
                                    TimeUnit.MILLISECONDS));
                        }
                    }
                });
        return bootstrap;
    }

    /**
     * Template method for changing properties on the given {@link SocketChannelConfig}.
     * <p>The default implementation sets the connect timeout based on the set property.
     * @param config the channel configuration
     */
    protected void configureChannel(SocketChannelConfig config) {
        if (this.connectTimeout >= 0) {
            config.setConnectTimeoutMillis(this.connectTimeout);
        }
    }


    @Override
    public void destroy() throws InterruptedException {

        if (this.defaultEventLoopGroup) {
            log.info("??????http {} EventLoopGroup ??????-------------",httpVersion.toString());
            // Clean up the EventLoopGroup if we created it in the constructor
            this.eventLoopGroup.shutdownGracefully().sync();
            log.info("??????http {} EventLoopGroup ????????????-----------",httpVersion.toString());
        }
    }

}
