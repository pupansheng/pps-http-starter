/*
 * Copyright (c) ACCA Corp.
 * All Rights Reserved.
 */
package com.pps.http.myrequest.phantom;

import com.pps.http.driverhander.PhantomDriverHander;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * @author Pu PanSheng, 2021/4/4
 * @version OPRA v1.0
 */

@Slf4j
public class PhantomRequestFactory implements ClientHttpRequestFactory, DisposableBean {
    private String path;
    private  int bufferSize;
    private PhantomJSDriver [] phantomJSDrivers;
    private AtomicBoolean [] driverStatus;
    private Semaphore semaphore;
    private DesiredCapabilities dcaps;
    private PhantomDriverHander phantomDriverHander;
    private static final ThreadLocal<PhantomJSDriver> PHANTOM_JS_DRIVER_THREAD_LOCAL=new ThreadLocal<>();
    public PhantomRequestFactory(DesiredCapabilities dcaps,PhantomDriverHander phantomDriverHander,String path, int bufferSize) {
        if(path==null||"".equals(path)){
            throw new RuntimeException("phantomjs path 不合法！："+path);
        }
        this.dcaps=dcaps;
        this.phantomDriverHander=phantomDriverHander;
        this.path=path;
        this.bufferSize=bufferSize;
        this.phantomJSDrivers=new PhantomJSDriver[bufferSize];
        this.semaphore=new Semaphore(bufferSize);
        this.driverStatus=new AtomicBoolean[bufferSize];
        initDriver();
    }
    public PhantomRequestFactory(String path, int bufferSize) {
        if(path==null||"".equals(path)){
            throw new RuntimeException("phantomjs path 不合法！："+path);
        }
        DesiredCapabilities dcaps = new DesiredCapabilities();

        dcaps.setCapability("acceptSslCerts", true);
        //截屏支持
        dcaps.setCapability("takesScreenshot", true);
        //css搜索支持
        dcaps.setCapability("cssSelectorsEnabled", true);
        //js支持
        dcaps.setJavascriptEnabled(true);

        this.dcaps=dcaps;
        this.path=path;
        this.bufferSize=bufferSize;
        this.phantomJSDrivers=new PhantomJSDriver[bufferSize];
        this.semaphore=new Semaphore(bufferSize);
        this.driverStatus=new AtomicBoolean[bufferSize];
        initDriver();
    }
    private void initDriver(){
        for (int i = 0; i <bufferSize ; i++) {
            //驱动支持
            dcaps.setCapability(PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY,path);
            //创建无界面浏览器对象
            PhantomJSDriver driver= new PhantomJSDriver(dcaps);
            driver.manage().window().maximize();
            driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
            phantomJSDrivers[i]=driver;
            driverStatus[i]=new AtomicBoolean(false);
        }
        if(phantomDriverHander!=null){
            phantomDriverHander.driverCustom(phantomJSDrivers);
        }
    }


    public PhantomJSDriver getDriver(){
        log.info("线程：{}  准备取走驱动---------：",Thread.currentThread().getId());
        PhantomJSDriver phantomJSDriver = PHANTOM_JS_DRIVER_THREAD_LOCAL.get();
        if(phantomJSDriver!=null){
            log.info("线程：{}  已从THREAD_LOCAL 取走驱动：",Thread.currentThread().getId(), phantomJSDriver);
            return phantomJSDriver;
        }
        try {

            semaphore.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        for (int i = 0; i <bufferSize ; i++) {

            if(!driverStatus[i].get()){
                driverStatus[i].set(true);
                PHANTOM_JS_DRIVER_THREAD_LOCAL.set(phantomJSDrivers[i]);
                log.info("线程：{}  以从缓存池中 取走驱动：",Thread.currentThread().getId(), phantomJSDrivers[i]);
                return phantomJSDrivers[i];
            }

        }

        throw new RuntimeException("异常状况！有驱动资源但是资源全都不可用！");
    }


    public void returnDriver(PhantomJSDriver driver){

        for (int i = 0; i <bufferSize ; i++) {
            if(phantomJSDrivers[i]==driver && driverStatus[i].get()){
                semaphore.release();
                driverStatus[i].set(false);
                PHANTOM_JS_DRIVER_THREAD_LOCAL.remove();
                log.info("线程：{}  归还驱动：",Thread.currentThread().getId(), driver);
            }
        }




    }

    @Override
    public ClientHttpRequest createRequest(URI uri, HttpMethod httpMethod) throws IOException {
        PhantomClientHttpRequest phantomClientHttpRequest = new PhantomClientHttpRequest(uri, httpMethod,this);
        return phantomClientHttpRequest;
    }

    @Override
    public void destroy() throws Exception {
        log.info("释放Phantomjs Driver 资源-------------");
        for (int i = 0; i < bufferSize; i++) {
            try {
                phantomJSDrivers[i].close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                phantomJSDrivers[i].quit();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        log.info("释放Phantomjs Driver 资源结束-----------");
    }
}
