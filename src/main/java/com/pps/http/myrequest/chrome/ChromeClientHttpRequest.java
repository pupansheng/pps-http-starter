/*
 * Copyright (c) ACCA Corp.
 * All Rights Reserved.
 */
package com.pps.http.myrequest.chrome;

import com.pps.http.myrequest.phantom.PhantomClientHttpResponse;
import com.pps.http.myrequest.phantom.PhantomRequestFactory;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;


/**
 * @author Pu PanSheng, 2021/4/4
 * @version OPRA v1.0
 */
public class ChromeClientHttpRequest implements ClientHttpRequest {

    private URI uri;
    private HttpMethod httpMethod;
    private ChromeDriver driver;
    private OutputStream outputStream;
    private HttpHeaders httpHeaders;
    private ChromeRequestFactory chromeRequestFactory;
    public ChromeClientHttpRequest(URI uri, HttpMethod httpMethod, ChromeRequestFactory phantomRequestFactory) {
            this.uri=uri;
        this.httpMethod=httpMethod;
        this.chromeRequestFactory=phantomRequestFactory;
        this.driver=phantomRequestFactory.getDriver();
        this.outputStream=new ByteArrayOutputStream();
        this.httpHeaders=new HttpHeaders();
    }


    @Override
    public ClientHttpResponse execute() throws IOException {
        try {
            driver.get(uri.toString());
        }catch (Exception e){
            chromeRequestFactory.returnDriver(driver);
        }
        ChromeClientHttpResponse phantomClientHttpResponse=new ChromeClientHttpResponse(driver,chromeRequestFactory);
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

    public ChromeDriver getDriver() {
        return driver;
    }
}
