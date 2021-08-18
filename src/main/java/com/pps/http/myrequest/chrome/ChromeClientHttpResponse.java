/*
 * Copyright (c) ACCA Corp.
 * All Rights Reserved.
 */
package com.pps.http.myrequest.chrome;

import com.pps.http.myrequest.phantom.PhantomRequestFactory;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Pu PanSheng, 2021/4/4
 * @version OPRA v1.0
 */
@Slf4j
public class ChromeClientHttpResponse implements ClientHttpResponse {

    private ChromeDriver chromeDriver;
    private ChromeRequestFactory chromeRequestFactory;
    public ChromeClientHttpResponse(ChromeDriver driver, ChromeRequestFactory chromeRequestFactory) {
        this.chromeDriver=driver;
        this.chromeRequestFactory=chromeRequestFactory;
    }

    @Override
    public HttpStatus getStatusCode() throws IOException {
        return  HttpStatus.OK;
    }

    @Override
    public int getRawStatusCode() throws IOException {
        return 200;
    }

    @Override
    public String getStatusText() throws IOException {
        return "ok";
    }

    @Override
    public void close() {

    }

    /**
     * diver放回缓存池
     */
    public void returnDiver(){
        chromeRequestFactory.returnDriver(chromeDriver);
    }


    @Override
    public InputStream getBody() throws IOException {
        String pageSource = chromeDriver.getPageSource();
        ByteArrayInputStream byteArrayInputStream=new ByteArrayInputStream(pageSource.getBytes());
        return byteArrayInputStream;
    }

    public String getHtml(){
        return  chromeDriver.getPageSource();
    }

    public  ChromeDriver getDriver(){
        return  chromeDriver;
    }

    @Override
    public HttpHeaders getHeaders() {
        log.warn("谷歌驱动 无法直接拿到headder!");
        return null;
    }
}
