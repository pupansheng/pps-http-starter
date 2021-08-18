/*
 * Copyright (c) ACCA Corp.
 * All Rights Reserved.
 */
package com.pps.http.configure;

import com.pps.http.PpsHttpUtilForSpring;
import com.pps.http.driverhander.ChromeDriverHander;
import com.pps.http.driverhander.PhantomDriverHander;
import com.pps.http.heart.DriverTestJob;
import com.pps.http.myrequest.chrome.ChromeRequestFactory;
import com.pps.http.myrequest.netty.MyNetty4ClientHttpRequestFactory;
import com.pps.http.myrequest.phantom.PhantomRequestFactory;
import com.pps.http.property.PpsHttpProperty;
import io.netty.handler.codec.http.HttpVersion;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;


import java.util.function.Consumer;

/**
 * @author Pu PanSheng, 2021/5/8
 * @version OPRA v1.0
 */
@EnableConfigurationProperties({PpsHttpProperty.class})//注入该类进入容器
@ConditionalOnProperty(prefix = "pps.http",value = "enable",havingValue = "true")
@Slf4j
public class PpsHttpAutoConfigure {


    @Autowired
    private PpsHttpProperty ppsHttpProperty;
    @Autowired(required = false)
    private  PhantomDriverHander driverHander;
    @Autowired(required = false)
    private ChromeDriverHander chromeDriverHander;

    private Consumer<DesiredCapabilities>  defaultConfig=(dcaps)->{
        //ssl证书支持
        dcaps.setCapability("acceptSslCerts", true);
        //截屏支持
        dcaps.setCapability("takesScreenshot", true);
        //css搜索支持
        dcaps.setCapability("cssSelectorsEnabled", true);
        //js支持
        dcaps.setJavascriptEnabled(true);
    };
    @Bean("myNetty4ClientHttpRequestFactory1_1")
    @ConditionalOnProperty(prefix = "pps.http",value = "enableforHttp1",havingValue = "true")
    public MyNetty4ClientHttpRequestFactory myNetty4ClientHttpRequestFactory(){
        log.info("开启Http1_1 协议网络请求Factory  线程数：{}-----------------------------------",ppsHttpProperty.getTheadForHttp1());
        MyNetty4ClientHttpRequestFactory myNetty4ClientHttpRequestFactory = new MyNetty4ClientHttpRequestFactory(HttpVersion.HTTP_1_1,
                ppsHttpProperty.getConnectTime(), ppsHttpProperty.getReadTimeout(),ppsHttpProperty.getTheadForHttp1());
        myNetty4ClientHttpRequestFactory.afterPropertiesSet();
        myNetty4ClientHttpRequestFactory.setMaxResponseSize(ppsHttpProperty.getMaxResponseSize());
        return myNetty4ClientHttpRequestFactory;
    }

    @Bean("myNetty4ClientHttpRequestFactory1_0")
    @ConditionalOnProperty(prefix = "pps.http",value = "enableforHttp0",havingValue = "true")
    public MyNetty4ClientHttpRequestFactory myNetty4ClientHttpRequestFactory2(){
        log.info("开启Http1_0 协议网络请求Factory  线程数：{}-----------------------------------",ppsHttpProperty.getTheadForHttp0());
        MyNetty4ClientHttpRequestFactory myNetty4ClientHttpRequestFactory = new MyNetty4ClientHttpRequestFactory(HttpVersion.HTTP_1_0,
                ppsHttpProperty.getConnectTime(), ppsHttpProperty.getReadTimeout(),ppsHttpProperty.getTheadForHttp0());
        myNetty4ClientHttpRequestFactory.afterPropertiesSet();
        myNetty4ClientHttpRequestFactory.setMaxResponseSize(ppsHttpProperty.getMaxResponseSize());
        return myNetty4ClientHttpRequestFactory;
    }

    @Bean("phantomRequestFactory")
    @ConditionalOnProperty(prefix = "pps.http",value = "enableforPhantom",havingValue = "true")
    public PhantomRequestFactory phantomRequestFactory(){
        log.info("开启Phantomjs 虚拟浏览器-----------------------------------");
        DesiredCapabilities dcaps = new DesiredCapabilities();
        defaultConfig.accept(dcaps);
        if(driverHander!=null){
            log.info("检测到自定义PhantomJs浏览器设置 开始自定义设置------");
          driverHander.customPhantomJsDriver(dcaps);
        }
        PhantomRequestFactory phantomRequestFactory=new PhantomRequestFactory(dcaps,
                driverHander,ppsHttpProperty.getPath(),ppsHttpProperty.getBuffSize());
        log.info("phantomJs 驱动位置：{}， 缓存大小：{}", ppsHttpProperty.getPath(), ppsHttpProperty.getBuffSize());

        log.info("开启Phantomjs 虚拟浏览器结束---------------------------------");
        return phantomRequestFactory;
    }

    @Bean("chromeRequestFactory")
    @ConditionalOnProperty(prefix = "pps.http",value = "enableforChrome",havingValue = "true")
    public ChromeRequestFactory chromeRequestFactory (){
        log.info("开启chrome 虚拟浏览器-----------------------------------");
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--disable-dev-shm-usage ");
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");
        if(chromeDriverHander!=null){
            log.info("检测到自定义PhantomJs浏览器设置 开始自定义设置------");
            chromeDriverHander.customPhantomJsDriver(options);
        }
        ChromeRequestFactory chromeRequestFactory=new ChromeRequestFactory(options,
                chromeDriverHander,ppsHttpProperty.getChromePath(),ppsHttpProperty.getChromeBuffSize());
        log.info("chrome驱动位置：{}， 缓存大小：{}", ppsHttpProperty.getChromePath(), ppsHttpProperty.getChromeBuffSize());

        log.info("开启Chrome 虚拟浏览器结束---------------------------------");
        return chromeRequestFactory;
    }
    @Bean
    public  PpsHttpUtilForSpring ppsHttpUtilForSpring(
            @Autowired(required = false) @Qualifier("myNetty4ClientHttpRequestFactory1_1") MyNetty4ClientHttpRequestFactory myNetty4ClientHttpRequestFactory1_1,
            @Autowired(required = false) @Qualifier("myNetty4ClientHttpRequestFactory1_0") MyNetty4ClientHttpRequestFactory myNetty4ClientHttpRequestFactory1_0,
            @Autowired(required = false) PhantomRequestFactory phantomRequestFactory,
            @Autowired(required = false) ChromeRequestFactory chromeRequestFactory){

        PpsHttpUtilForSpring ppsHttpUtilForSpring=new PpsHttpUtilForSpring();
        ppsHttpUtilForSpring.setMyNetty4ClientHttpRequestFactory1_1(myNetty4ClientHttpRequestFactory1_1);
        ppsHttpUtilForSpring.setMyNetty4ClientHttpRequestFactory1_0(myNetty4ClientHttpRequestFactory1_0);
        ppsHttpUtilForSpring.setChromeRequestFactory(chromeRequestFactory);
        ppsHttpUtilForSpring.setPhantomRequestFactory(phantomRequestFactory);
        boolean enableHeat = ppsHttpProperty.isEnableHeat();
        if(enableHeat) {
            new DriverTestJob(phantomRequestFactory, chromeRequestFactory,ppsHttpProperty.getInterval());
        }
        log.info("已启动ppsHttpUtilForSpring-----------------------");
        return ppsHttpUtilForSpring;

    }

    public static void main(String args[]){


        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless"); // 禁用阻止弹出窗口
        options.addArguments("--disable-dev-shm-usage "); // 启动无沙盒模式运行
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");
        System.setProperty("webdriver.chrome.driver","d:\\chromedriver.exe");
        ChromeDriver chromeDriver=new ChromeDriver(options);
        chromeDriver.get("https://www.baidu.com");
        System.out.println(chromeDriver.getPageSource());


    }

}
