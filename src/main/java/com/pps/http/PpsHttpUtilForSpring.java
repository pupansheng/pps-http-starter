package com.pps.http;


import com.pps.http.executor.PpsHttpExcutor;
import com.pps.http.myrequest.chrome.ChromeRequestFactory;
import com.pps.http.myrequest.netty.MyNetty4ClientHttpRequestFactory;
import com.pps.http.myrequest.phantom.PhantomRequestFactory;
import io.netty.handler.codec.http.HttpVersion;
import org.springframework.web.client.RestTemplate;

/**
 * @author pps
 * @discription;
 * @time 2021/1/21 14:04
 */


public class PpsHttpUtilForSpring implements PpsHttp {

    //http协议1.0
    private volatile   RestTemplate restTemplateForNetty1_0;
    //http 协议1.1
    private  volatile RestTemplate restTemplateForNetty1_1;
    //测试连接
    private volatile RestTemplate restTemplateForConnect;
    //无界浏览器
    private volatile RestTemplate restTemplateForPhantomjs;
    //无界浏览器谷歌
    private volatile static RestTemplate restTmplateForChrome;

    private Object lock=new Object();
    
    
   
    MyNetty4ClientHttpRequestFactory myNetty4ClientHttpRequestFactory1_1;

  
    MyNetty4ClientHttpRequestFactory myNetty4ClientHttpRequestFactory1_0;


    PhantomRequestFactory phantomRequestFactory;

    ChromeRequestFactory chromeRequestFactory;

    public void setMyNetty4ClientHttpRequestFactory1_1(MyNetty4ClientHttpRequestFactory myNetty4ClientHttpRequestFactory1_1) {
        this.myNetty4ClientHttpRequestFactory1_1 = myNetty4ClientHttpRequestFactory1_1;
    }

    public void setMyNetty4ClientHttpRequestFactory1_0(MyNetty4ClientHttpRequestFactory myNetty4ClientHttpRequestFactory1_0) {
        this.myNetty4ClientHttpRequestFactory1_0 = myNetty4ClientHttpRequestFactory1_0;
    }

    public void setPhantomRequestFactory(PhantomRequestFactory phantomRequestFactory) {
        this.phantomRequestFactory = phantomRequestFactory;
    }

    public void setChromeRequestFactory(ChromeRequestFactory chromeRequestFactory) {
        this.chromeRequestFactory = chromeRequestFactory;
    }

    public MyNetty4ClientHttpRequestFactory getMyNetty4ClientHttpRequestFactory1_1() {
        return myNetty4ClientHttpRequestFactory1_1;
    }

    public MyNetty4ClientHttpRequestFactory getMyNetty4ClientHttpRequestFactory1_0() {
        return myNetty4ClientHttpRequestFactory1_0;
    }

    public PhantomRequestFactory getPhantomRequestFactory() {
        return phantomRequestFactory;
    }

    public ChromeRequestFactory getChromeRequestFactory() {
        return chromeRequestFactory;
    }

    /**
     * 创建同步的请求器
     * @return
     */
    public PpsHttpExcutor createSyncClient(){
        if(restTemplateForNetty1_1==null){
            synchronized (lock){
                if(restTemplateForNetty1_1==null) {
                    if(myNetty4ClientHttpRequestFactory1_1==null){
                        throw new RuntimeException("未开启http1_1 配置!");
                    }
                    restTemplateForNetty1_1 = new RestTemplate();
                    restTemplateForNetty1_1.setRequestFactory(myNetty4ClientHttpRequestFactory1_1);
                }
            }
        }
       return  new PpsHttpExcutor(false,restTemplateForNetty1_1,this);

    }

    /**
     * 创建异步的请求器
     * @return
     */
    public   PpsHttpExcutor   createASyncClient(){

        if(restTemplateForNetty1_1==null){
            synchronized (lock){
                if(restTemplateForNetty1_1==null) {
                    if(myNetty4ClientHttpRequestFactory1_1==null){
                        throw new RuntimeException("未开启http1_1 配置!");
                    }
                    restTemplateForNetty1_1 = new RestTemplate();
                    restTemplateForNetty1_1.setRequestFactory(myNetty4ClientHttpRequestFactory1_1);
                }
            }
        }
        return  new PpsHttpExcutor(true,restTemplateForNetty1_1,this);

    }
    /**
     * 创建同步的请求器 协议指定
     * @return
     */
    public   PpsHttpExcutor   createSyncClient(HttpVersion httpVersion){

        if(httpVersion==HttpVersion.HTTP_1_1) {
            if(restTemplateForNetty1_1==null){
                synchronized (lock){
                    if(restTemplateForNetty1_1==null) {
                        if(myNetty4ClientHttpRequestFactory1_1==null){
                            throw new RuntimeException("未开启http1_1 配置!");
                        }
                        restTemplateForNetty1_1 = new RestTemplate();
                        restTemplateForNetty1_1.setRequestFactory(myNetty4ClientHttpRequestFactory1_1);
                    }
                }
            }
            return new PpsHttpExcutor(false, restTemplateForNetty1_1,this);
        }else {
            if(restTemplateForNetty1_0==null){
                synchronized (lock){
                    if(restTemplateForNetty1_0==null) {
                        if(myNetty4ClientHttpRequestFactory1_0==null){
                            throw new RuntimeException("未开启http1_0 配置!");
                        }
                        restTemplateForNetty1_0 = new RestTemplate();
                        restTemplateForNetty1_0.setRequestFactory(myNetty4ClientHttpRequestFactory1_0);
                    }
                }
            }
            return new PpsHttpExcutor(false, restTemplateForNetty1_0,this);
        }

    }

    
    public   PpsHttpExcutor   createASyncClient(HttpVersion httpVersion){

        if(httpVersion==HttpVersion.HTTP_1_1) {
            if(restTemplateForNetty1_1==null){
                synchronized (lock){
                    if(restTemplateForNetty1_1==null) {
                        if(myNetty4ClientHttpRequestFactory1_1==null){
                            throw new RuntimeException("未开启http1_1 配置!");
                        }

                        restTemplateForNetty1_1 = new RestTemplate();
                        restTemplateForNetty1_1.setRequestFactory(myNetty4ClientHttpRequestFactory1_1);
                    }
                }
            }
            return new PpsHttpExcutor(true, restTemplateForNetty1_1,this);
        }else {
            if(restTemplateForNetty1_0==null){
                synchronized (lock){
                    if(restTemplateForNetty1_0==null) {
                        if(myNetty4ClientHttpRequestFactory1_0==null){
                            throw new RuntimeException("未开启http1_0 配置!");
                        }
                        restTemplateForNetty1_0 = new RestTemplate();
                        restTemplateForNetty1_0.setRequestFactory(myNetty4ClientHttpRequestFactory1_0);
                    }
                }
            }
            return new PpsHttpExcutor(true, restTemplateForNetty1_0,this);
        }

    }


    /**
     * 创建模拟浏览器
     * @param async
     * @return
     */
    public  PpsHttpExcutor createPhantojsClient(boolean async){
        if(restTemplateForPhantomjs==null){
            synchronized (lock){
                if(restTemplateForPhantomjs==null) {
                    restTemplateForPhantomjs = new RestTemplate();
                    if(phantomRequestFactory==null){
                        throw new RuntimeException("未开启模拟浏览器phantomjs 配置");
                    }
                    restTemplateForPhantomjs.setRequestFactory(phantomRequestFactory);
                }
            }
        }
            return new PpsHttpExcutor(async, restTemplateForPhantomjs,this);
    }

    /**
     * 创建模拟浏览器
     * @param async
     * @return
     */
    public  PpsHttpExcutor createChromeClient(boolean async){
        if(restTmplateForChrome==null){
            synchronized (lock){
                if(restTmplateForChrome==null) {
                    restTmplateForChrome = new RestTemplate();
                    if(chromeRequestFactory==null){
                        throw new RuntimeException("未开启模拟chrome浏览器 配置");
                    }
                    restTmplateForChrome.setRequestFactory(chromeRequestFactory);
                }
            }
        }
        return new PpsHttpExcutor(async, restTmplateForChrome,this);
    }
    /**
     * 创建测试连接 可以指定网络连接的时间
     * @return
     */
    public PpsHttpExcutor createTestConnectClient(int connectTimeout,int readTimeout){
        if(restTemplateForConnect==null){
            synchronized (lock){
                if(restTemplateForConnect==null) {
                    MyNetty4ClientHttpRequestFactory myNetty4ClientHttpRequestFactory=
                            new MyNetty4ClientHttpRequestFactory(HttpVersion.HTTP_1_1,connectTimeout,readTimeout,1);
                    restTemplateForConnect = new RestTemplate();
                    restTemplateForConnect.setRequestFactory(myNetty4ClientHttpRequestFactory);
                }
            }
        }
        return  new PpsHttpExcutor(false,restTemplateForConnect,this);
    }

}
