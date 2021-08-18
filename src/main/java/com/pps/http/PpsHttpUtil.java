package com.pps.http;


import com.pps.http.executor.PpsHttpExcutor;
import com.pps.http.myrequest.chrome.ChromeRequestFactory;
import com.pps.http.myrequest.netty.MyNetty4ClientHttpRequestFactory;
import com.pps.http.myrequest.phantom.PhantomRequestFactory;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLException;

/**
 * @author pps
 * @discription;
 * @time 2021/1/21 14:04
 */

public class PpsHttpUtil implements PpsHttp {

    //http协议1.0
    private static volatile RestTemplate restTemplateForNetty1_0;
    //http 协议1.1
    private static volatile RestTemplate restTemplateForNetty1_1;
    //无界浏览器
    private static volatile RestTemplate restTemplateForPhantomjs;
    //无界浏览器谷歌
    private static volatile RestTemplate restTmplateForChrome;

    //最大响应大小
    private static int maxResponseSize=10*1024*1024;

    private static String PHAMTOM_JS_PATH;
    private static String CHROME_PATH;
    private static Object lock=new Object();
    private static PhantomRequestFactory phantomRequestFactory;
    private static ChromeRequestFactory chromeRequestFactory;
    private static SslContext sslContext;
    private static int workThread=1;
    public static void setPhamtomJsPath(String path){
      PHAMTOM_JS_PATH=path;
    }
    public static void setChromePath(String chromePath) {
        CHROME_PATH = chromePath;
    }
    public static void setMaxResponseSize(int maxResponseSize) {
        PpsHttpUtil.maxResponseSize = maxResponseSize;
    }

    static {
        try {
            sslContext= SslContextBuilder.forClient().build();
        } catch (SSLException e) {
            e.printStackTrace();
        }
    }


    public static void setWorkThread(int workThread) {
        PpsHttpUtil.workThread = workThread;
    }

    /**
     * 创建同步的请求器
     * @return
     */
    public  static PpsHttpExcutor createSyncClient(){

      // restTemplate.setRequestFactory();
        if(restTemplateForNetty1_1==null){
            synchronized (lock){
                if(restTemplateForNetty1_1==null){
                    MyNetty4ClientHttpRequestFactory myNetty4ClientHttpRequestFactory1 =
                            new MyNetty4ClientHttpRequestFactory(HttpVersion.HTTP_1_1,-1,-1,workThread);
                    myNetty4ClientHttpRequestFactory1.setSslContext(sslContext);
                    myNetty4ClientHttpRequestFactory1.setMaxResponseSize(maxResponseSize);
                    restTemplateForNetty1_1=new RestTemplate();
                    restTemplateForNetty1_1.setRequestFactory(myNetty4ClientHttpRequestFactory1);
                }
            }
        }
       return  new PpsHttpExcutor(false,restTemplateForNetty1_1,null);

    }
    /**
     * 创建同步的请求器
     * @return
     */
    public  static  PpsHttpExcutor   createSyncClient(HttpVersion httpVersion){

        // restTemplate.setRequestFactory();
        if(httpVersion==HttpVersion.HTTP_1_1) {
            if(restTemplateForNetty1_1==null){
                synchronized (lock){
                    if(restTemplateForNetty1_1==null){
                        MyNetty4ClientHttpRequestFactory myNetty4ClientHttpRequestFactory1 =
                                new MyNetty4ClientHttpRequestFactory(HttpVersion.HTTP_1_1,-1,-1,workThread);
                        myNetty4ClientHttpRequestFactory1.setSslContext(sslContext);
                        myNetty4ClientHttpRequestFactory1.setMaxResponseSize(maxResponseSize);
                        restTemplateForNetty1_1=new RestTemplate();
                        restTemplateForNetty1_1.setRequestFactory(myNetty4ClientHttpRequestFactory1);
                    }
                }
            }
            return new PpsHttpExcutor(false, restTemplateForNetty1_1,null);
        }else {
            if(restTemplateForNetty1_0==null){
                synchronized (lock){
                    if(restTemplateForNetty1_0==null){
                        MyNetty4ClientHttpRequestFactory myNetty4ClientHttpRequestFactory1 =
                                new MyNetty4ClientHttpRequestFactory(HttpVersion.HTTP_1_0,-1,-1,workThread);
                        myNetty4ClientHttpRequestFactory1.setSslContext(sslContext);
                        myNetty4ClientHttpRequestFactory1.setMaxResponseSize(maxResponseSize);
                        restTemplateForNetty1_0=new RestTemplate();
                        restTemplateForNetty1_0.setRequestFactory(myNetty4ClientHttpRequestFactory1);
                    }
                }
            }
            return new PpsHttpExcutor(false, restTemplateForNetty1_0,null);
        }

    }


    /**
     * 创建异步的请求器
     * @return
     */
    public  static PpsHttpExcutor   createASyncClient(){
        if(restTemplateForNetty1_1==null){
            synchronized (lock){
                if(restTemplateForNetty1_1==null){
                    MyNetty4ClientHttpRequestFactory myNetty4ClientHttpRequestFactory1 =
                            new MyNetty4ClientHttpRequestFactory(HttpVersion.HTTP_1_1,-1,-1,workThread);
                    myNetty4ClientHttpRequestFactory1.setSslContext(sslContext);
                    myNetty4ClientHttpRequestFactory1.setMaxResponseSize(maxResponseSize);
                    restTemplateForNetty1_1=new RestTemplate();
                    restTemplateForNetty1_1.setRequestFactory(myNetty4ClientHttpRequestFactory1);
                }
            }
        }
        return  new PpsHttpExcutor(true,restTemplateForNetty1_1,null);

    }


    public  static PpsHttpExcutor   createASyncClient(HttpVersion httpVersion){

        if(httpVersion==HttpVersion.HTTP_1_1) {
            if(restTemplateForNetty1_1==null){
                synchronized (lock){
                    if(restTemplateForNetty1_1==null){
                        MyNetty4ClientHttpRequestFactory myNetty4ClientHttpRequestFactory1 =
                                new MyNetty4ClientHttpRequestFactory(HttpVersion.HTTP_1_1,-1,-1,workThread);
                        myNetty4ClientHttpRequestFactory1.setSslContext(sslContext);
                        myNetty4ClientHttpRequestFactory1.setMaxResponseSize(maxResponseSize);
                        restTemplateForNetty1_1=new RestTemplate();
                        restTemplateForNetty1_1.setRequestFactory(myNetty4ClientHttpRequestFactory1);
                    }
                }
            }
            return new PpsHttpExcutor(false, restTemplateForNetty1_1,null);
        }else {
            if(restTemplateForNetty1_0==null){
                synchronized (lock){
                    if(restTemplateForNetty1_0==null){
                        MyNetty4ClientHttpRequestFactory myNetty4ClientHttpRequestFactory1 =
                                new MyNetty4ClientHttpRequestFactory(HttpVersion.HTTP_1_0,-1,-1,workThread);
                        myNetty4ClientHttpRequestFactory1.setSslContext(sslContext);
                        myNetty4ClientHttpRequestFactory1.setMaxResponseSize(maxResponseSize);
                        restTemplateForNetty1_0=new RestTemplate();
                        restTemplateForNetty1_0.setRequestFactory(myNetty4ClientHttpRequestFactory1);
                    }
                }
            }
            return new PpsHttpExcutor(false, restTemplateForNetty1_0,null);
        }

    }


    /**
     * 特别注意 需要quit驱动 否则驱动程序会一直在
     * @param async
     * @return
     */
    public static PpsHttpExcutor createPhantojsClient(boolean async){
        if(phantomRequestFactory==null){
            synchronized (lock){
                if(phantomRequestFactory==null){
                    restTemplateForPhantomjs=new RestTemplate();
                    phantomRequestFactory=new PhantomRequestFactory(PHAMTOM_JS_PATH,1);
                }
            }
        }
        restTemplateForPhantomjs.setRequestFactory(phantomRequestFactory);
        return new PpsHttpExcutor(async, restTemplateForPhantomjs,null);
    }

    /**
     * 特别注意 需要quit驱动 否则驱动程序会一直在
     * @param async
     * @return
     */
    public static PpsHttpExcutor createPhantojsClient(boolean async,int buffsize){
        if(phantomRequestFactory==null){
            synchronized (lock){
                if(phantomRequestFactory==null){
                    phantomRequestFactory=new PhantomRequestFactory(PHAMTOM_JS_PATH,buffsize);
                    restTemplateForPhantomjs=new RestTemplate();
                }
            }
        }
        restTemplateForPhantomjs.setRequestFactory(phantomRequestFactory);
        return new PpsHttpExcutor(async, restTemplateForPhantomjs,null);
    }

    /**
     * 特别注意 需要quit驱动 否则驱动程序会一直在
     * @param async
     * @return
     */
    public static PpsHttpExcutor createChromeClient(boolean async){
        if(chromeRequestFactory==null){
            synchronized (lock){
                if(chromeRequestFactory==null){
                    chromeRequestFactory=new ChromeRequestFactory(CHROME_PATH,1);
                    restTmplateForChrome=new RestTemplate();
                }
            }
        }
        restTmplateForChrome.setRequestFactory(chromeRequestFactory);
        return new PpsHttpExcutor(async, restTmplateForChrome,null);
    }

    /**
     * 特别注意 需要quit驱动 否则驱动程序会一直在
     * @param async
     * @return
     */
    public static PpsHttpExcutor creatChromeClient(boolean async,int buffsize){
        if(chromeRequestFactory==null){
            synchronized (lock){
                if(chromeRequestFactory==null){
                    chromeRequestFactory=new ChromeRequestFactory(CHROME_PATH,buffsize);
                    restTmplateForChrome=new RestTemplate();
                }
            }
        }
        restTmplateForChrome.setRequestFactory(chromeRequestFactory);
        return new PpsHttpExcutor(async, restTmplateForChrome,null);
    }

    /**
     * 特别注意 需要quit驱动 否则驱动程序会一直在
     * @param async
     * @return
     */
    public static PpsHttpExcutor creatChromeClient(ChromeOptions options,boolean async, int buffsize){
        if(chromeRequestFactory==null){
            synchronized (lock){
                if(chromeRequestFactory==null){
                    chromeRequestFactory=new ChromeRequestFactory(options,CHROME_PATH,buffsize);
                    restTmplateForChrome=new RestTemplate();
                }
            }
        }
        restTmplateForChrome.setRequestFactory(chromeRequestFactory);
        return new PpsHttpExcutor(async, restTmplateForChrome,null);
    }

}
