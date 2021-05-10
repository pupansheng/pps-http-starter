/*
 * Copyright (c) ACCA Corp.
 * All Rights Reserved.
 */
package com.pps.http.myrequest.phantom;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;

/**
 * @author Pu PanSheng, 2021/4/4
 * @version OPRA v1.0
 */
@Slf4j
public class PhantomClientHttpResponse implements ClientHttpResponse {

    private PhantomJSDriver phantomJSDriver;
    private PhantomRequestFactory phantomRequestFactory;
    public PhantomClientHttpResponse(PhantomJSDriver driver, PhantomRequestFactory phantomRequestFactory) {
        this.phantomJSDriver=driver;
        this.phantomRequestFactory=phantomRequestFactory;
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
        phantomRequestFactory.returnDriver(phantomJSDriver);
    }


    @Override
    public InputStream getBody() throws IOException {
        String pageSource = phantomJSDriver.getPageSource();
        ByteArrayInputStream byteArrayInputStream=new ByteArrayInputStream(pageSource.getBytes());
        return byteArrayInputStream;
    }

    public String getHtml(){
        return  phantomJSDriver.getPageSource();
    }

    public  PhantomJSDriver getDriver(){
        return  phantomJSDriver;
    }

    @Override
    public HttpHeaders getHeaders() {
        return null;
    }
}
