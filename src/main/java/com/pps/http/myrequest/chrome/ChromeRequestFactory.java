/*
 * Copyright (c) ACCA Corp.
 * All Rights Reserved.
 */
package com.pps.http.myrequest.chrome;

import com.pps.http.driverhander.ChromeDriverHander;
import com.pps.http.driverhander.PhantomDriverHander;
import com.pps.http.myrequest.phantom.PhantomClientHttpRequest;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestFactory;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Pu PanSheng, 2021/4/4
 * @version OPRA v1.0
 */

@Slf4j
public class ChromeRequestFactory implements ClientHttpRequestFactory, DisposableBean {
    private String path;
    private  int bufferSize;
    private ChromeDriver [] chromeDrivers;
    private AtomicBoolean [] driverStatus;
    private Semaphore semaphore;
    private ChromeOptions options;
    private ChromeDriverHander chromeDriverHander;
    private static final ThreadLocal<ChromeDriver> CHROME_DRIVER_THREAD_LOCAL=new ThreadLocal<>();
    private static final ThreadLocal<Integer> CHROME_DRIVER_COUNT=ThreadLocal.withInitial(()->{
        return new Integer(0);
    });
    public ChromeRequestFactory( ChromeOptions options, ChromeDriverHander phantomDriverHander, String path, int bufferSize) {
        if(path==null||"".equals(path)){
            throw new RuntimeException("chrome path 不合法！："+path);
        }
        this.options=options;
        this.chromeDriverHander=phantomDriverHander;
        this.path=path;
        this.bufferSize=bufferSize;
        this.chromeDrivers=new ChromeDriver[bufferSize];
        this.semaphore=new Semaphore(bufferSize);
        this.driverStatus=new AtomicBoolean[bufferSize];
        initDriver();
    }
    public ChromeRequestFactory( ChromeOptions options,String path, int bufferSize) {
        if(path==null||"".equals(path)){
            throw new RuntimeException("chrome path 不合法！："+path);
        }
        this.options=options;
        this.path=path;
        this.bufferSize=bufferSize;
        this.chromeDrivers=new ChromeDriver[bufferSize];
        this.semaphore=new Semaphore(bufferSize);
        this.driverStatus=new AtomicBoolean[bufferSize];
        initDriver();
    }
    public ChromeRequestFactory(String path, int bufferSize) {
        if(path==null||"".equals(path)){
            throw new RuntimeException("phantomjs path 不合法！："+path);
        }
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless"); // 禁用阻止弹出窗口
        options.addArguments("--disable-dev-shm-usage "); // 启动无沙盒模式运行
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");
        this.options=options;
        this.path=path;
        this.bufferSize=bufferSize;
        this.chromeDrivers=new ChromeDriver[bufferSize];
        this.semaphore=new Semaphore(bufferSize);
        this.driverStatus=new AtomicBoolean[bufferSize];
        initDriver();
    }
    private void initDriver(){
        System.setProperty("webdriver.chrome.driver",path);
        for (int i = 0; i <bufferSize ; i++) {
            ChromeDriver driver= new ChromeDriver(options);
            driver.manage().window().maximize();
            driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
            chromeDrivers[i]=driver;
            driverStatus[i]=new AtomicBoolean(false);
        }
        if(chromeDriverHander!=null){
            chromeDriverHander.driverCustom(chromeDrivers);
        }
    }


    public void check() throws InterruptedException {

        for (int i = 0; i < chromeDrivers.length; i++) {

            if(!driverStatus[i].get()){
                semaphore.acquire();
                driverStatus[i].getAndSet(true);
                ChromeDriver chromeDriver = chromeDrivers[i];
                boolean isTrue=true;
                try {
                    chromeDriver.get("https://www.baidu.com");
                } catch (Exception e) {
                    isTrue=false;
                }
                if(!isTrue){
                    try {
                        chromeDrivers[i].close();
                        chromeDrivers[i].quit();
                    } catch (Exception e) {

                    }
                    log.info("driver :{} 失效 开始重新生成驱动---------------",chromeDrivers[i]);
                    chromeDrivers[i]=new ChromeDriver(options);
                }
                driverStatus[i].getAndSet(false);
                semaphore.release();
            }
        }

    }


    public ChromeDriver getDriver(){
        log.info("线程：{}  准备取走驱动---------：",Thread.currentThread().getId());
        ChromeDriver phantomJSDriver = CHROME_DRIVER_THREAD_LOCAL.get();
        if(phantomJSDriver!=null){
            Integer count = CHROME_DRIVER_COUNT.get();
            CHROME_DRIVER_COUNT.set(count+1);
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
                driverStatus[i].getAndSet(true);
                CHROME_DRIVER_THREAD_LOCAL.set(chromeDrivers[i]);
                log.info("线程：{}  以从缓存池中 取走驱动：",Thread.currentThread().getId(), chromeDrivers[i]);
                return chromeDrivers[i];
            }

        }

        throw new RuntimeException("异常状况！有驱动资源但是资源全都不可用！");
    }


    public void returnDriver(RemoteWebDriver driver){

        for (int i = 0; i <bufferSize ; i++) {
            if(chromeDrivers[i]==driver && driverStatus[i].get()){
                Integer count = CHROME_DRIVER_COUNT.get();
                if(count==0) {
                    semaphore.release();
                    driverStatus[i].getAndSet(false);
                    CHROME_DRIVER_THREAD_LOCAL.remove();
                    log.info("线程：{}  归还驱动：", Thread.currentThread().getId(), driver);
                }else {
                    CHROME_DRIVER_COUNT.set(count-1);
                }
            }
        }




    }

    @Override
    public ClientHttpRequest createRequest(URI uri, HttpMethod httpMethod) throws IOException {
        ChromeClientHttpRequest phantomClientHttpRequest = new ChromeClientHttpRequest(uri, httpMethod,this);
        return phantomClientHttpRequest;
    }

    @Override
    public void destroy() throws Exception {
        log.info("释放Chrome Driver 资源-------------");
        for (int i = 0; i < bufferSize; i++) {
            try {
                chromeDrivers[i].close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                chromeDrivers[i].quit();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        log.info("释放Chrome Driver 资源结束-----------");
    }
}
