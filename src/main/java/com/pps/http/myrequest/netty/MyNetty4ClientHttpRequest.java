/*
 * Copyright (c) ACCA Corp.
 * All Rights Reserved.
 */
package com.pps.http.myrequest.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpVersion;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.SettableListenableFuture;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.concurrent.ExecutionException;

/**
 * @author pupansheng, 2021/3/29
 * @version OPRA v1.0
 */
public class MyNetty4ClientHttpRequest extends AbstractAsyncClientHttpRequest implements ClientHttpRequest {

    private final Bootstrap bootstrap;

    private final URI uri;

    private final HttpMethod method;

    private final ByteBufOutputStream body;

    private HttpVersion httpVersion;

    public MyNetty4ClientHttpRequest(Bootstrap bootstrap, URI uri, HttpMethod method, HttpVersion httpVersion) {
        this.bootstrap = bootstrap;
        this.uri = uri;
        this.method = method;
        this.httpVersion=httpVersion;
        this.body = new ByteBufOutputStream(Unpooled.buffer(1024));
    }


    @Override
    public HttpMethod getMethod() {
        return this.method;
    }

    @Override
    public String getMethodValue() {
        return this.method.name();
    }

    @Override
    public URI getURI() {
        return this.uri;
    }

    @Override
    public ClientHttpResponse execute() throws IOException {
        try {
            return executeAsync().get();
        }
        catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted during request execution", ex);
        }
        catch (ExecutionException ex) {
            if (ex.getCause() instanceof IOException) {
                throw (IOException) ex.getCause();
            }
            else {
                throw new IOException(ex.getMessage(), ex.getCause());
            }
        }
    }

    @Override
    protected OutputStream getBodyInternal(HttpHeaders headers) throws IOException {
        return this.body;
    }

    @Override
    protected ListenableFuture<ClientHttpResponse> executeInternal(final HttpHeaders headers) throws IOException {
        final SettableListenableFuture<ClientHttpResponse> responseFuture = new SettableListenableFuture<>();

        ChannelFutureListener connectionListener = future -> {
            if (future.isSuccess()) {
                Channel channel = future.channel();
                channel.pipeline().addLast(new RequestExecuteHandler(responseFuture));
                FullHttpRequest nettyRequest = createFullHttpRequest(headers);
                channel.writeAndFlush(nettyRequest);
            }
            else {
                responseFuture.setException(future.cause());
            }
        };

        this.bootstrap.connect(this.uri.getHost(), getPort(this.uri)).addListener(connectionListener);
        return responseFuture;
    }

    private FullHttpRequest createFullHttpRequest(HttpHeaders headers) {
        io.netty.handler.codec.http.HttpMethod nettyMethod =
                io.netty.handler.codec.http.HttpMethod.valueOf(this.method.name());

        String authority = this.uri.getRawAuthority();
        String path = this.uri.toString().substring(this.uri.toString().indexOf(authority) + authority.length());
        ByteBuf buffer = this.body.buffer();
        FullHttpRequest nettyRequest = new DefaultFullHttpRequest(
                httpVersion, nettyMethod, path, buffer);
        nettyRequest.headers().set(HttpHeaders.HOST, this.uri.getHost() + ":" + getPort(uri));
        nettyRequest.headers().set(HttpHeaders.CONNECTION, "close");
        headers.forEach((headerName, headerValues) -> nettyRequest.headers().add(headerName, headerValues));
        if (!nettyRequest.headers().contains(HttpHeaders.CONTENT_LENGTH) && buffer.readableBytes() > 0) {
            nettyRequest.headers().set(HttpHeaders.CONTENT_LENGTH, buffer.readableBytes());
        }

        return nettyRequest;
    }

    private static int getPort(URI uri) {
        int port = uri.getPort();
        if (port == -1) {
            if ("http".equalsIgnoreCase(uri.getScheme())) {
                port = 80;
            }
            else if ("https".equalsIgnoreCase(uri.getScheme())) {
                port = 443;
            }
        }
        return port;
    }


    /**
     * A SimpleChannelInboundHandler to update the given SettableListenableFuture.
     */
    private static class RequestExecuteHandler extends SimpleChannelInboundHandler<FullHttpResponse> {

        private final SettableListenableFuture<ClientHttpResponse> responseFuture;

        public RequestExecuteHandler(SettableListenableFuture<ClientHttpResponse> responseFuture) {
            this.responseFuture = responseFuture;
        }

        @Override
        protected void channelRead0(ChannelHandlerContext context, FullHttpResponse response) throws Exception {
            this.responseFuture.set(new MyNetty4ClientHttpResponse(context, response));
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext context, Throwable cause) throws Exception {
            this.responseFuture.setException(cause);
        }
    }

}
