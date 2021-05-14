/*
 * Copyright (c) ACCA Corp.
 * All Rights Reserved.
 */
package com.pps.http.myrequest.phantom;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;


/**
 * @author Pu PanSheng, 2021/4/4
 * @version OPRA v1.0
 */
public class PhantomClientHttpRequest implements ClientHttpRequest {

    private URI uri;
    private HttpMethod httpMethod;
    private PhantomJSDriver driver;
    private OutputStream outputStream;
    private HttpHeaders httpHeaders;
    private PhantomRequestFactory phantomRequestFactory;
    public PhantomClientHttpRequest(URI uri, HttpMethod httpMethod,  PhantomRequestFactory phantomRequestFactory) {
            this.uri=uri;
        this.httpMethod=httpMethod;
        this.phantomRequestFactory=phantomRequestFactory;
        this.driver=phantomRequestFactory.getDriver();
        this.outputStream=new ByteArrayOutputStream();
        this.httpHeaders=new HttpHeaders();
    }


    @Override
    public ClientHttpResponse execute() throws IOException {
        try {
            driver.get(uri.toString());
        }catch (Exception e){
            phantomRequestFactory.returnDriver(driver);
        }
        PhantomClientHttpResponse phantomClientHttpResponse=new PhantomClientHttpResponse(driver,phantomRequestFactory);
        return phantomClientHttpResponse;
    }

    @Override
    public OutputStream getBody() throws IOException {
        return outputStream;
    }

    @Override
    public String getMethodValue() {
        return httpMethod.name();
    }

    @Override
    public URI getURI() {
        return uri;
    }

    @Override
    public HttpHeaders getHeaders() {
        return  httpHeaders;
    }

    public PhantomJSDriver getDriver() {
        return driver;
    }
}
