package com.pps.http;


import com.pps.http.executor.PpsHttpExcutor;
import com.pps.http.myrequest.netty.MyNetty4ClientHttpRequestFactory;
import com.pps.http.myrequest.phantom.PhantomRequestFactory;
import io.netty.handler.codec.http.HttpVersion;
import org.springframework.web.client.RestTemplate;

/**
 * @author pps
 * @discription;
 * @time 2021/1/21 14:04
 */

public class PpsHttpUtil implements PpsHttp {

    //http协议1.0
    private static RestTemplate restTemplateForNetty1_0=new RestTemplate();
    //http 协议1.1
    private static RestTemplate restTemplateForNetty1_1=new RestTemplate();
    //无界浏览器
    private static RestTemplate restTemplateForPhantomjs=new RestTemplate();

    private static final ThreadLocal<String> PHAMTOM_JS_PATH=ThreadLocal.withInitial(()->{
        String winLocation="D:\\dev_softwares\\phantomjs-2.1.1-windows\\bin\\phantomjs.exe";;
        String linLocation="/usr/bin/phantomjs";
        String os = System.getProperty("os.name");
        if(os.toLowerCase().startsWith("win")) {
           return  winLocation;
        }else {
          return  linLocation;
        }
    });

    static {
        restTemplateForNetty1_0.setRequestFactory(new MyNetty4ClientHttpRequestFactory(HttpVersion.HTTP_1_0));
        restTemplateForNetty1_1.setRequestFactory(new MyNetty4ClientHttpRequestFactory(HttpVersion.HTTP_1_1));
        restTemplateForPhantomjs.setRequestFactory(new PhantomRequestFactory(PHAMTOM_JS_PATH.get(),2));
    }

    /**
     * 创建同步的请求器
     * @return
     */
    public  static PpsHttpExcutor createSyncClient(){

      // restTemplate.setRequestFactory();
       return  new PpsHttpExcutor(false,restTemplateForNetty1_1,null);

    }
    /**
     * 创建同步的请求器
     * @return
     */
    public  static  PpsHttpExcutor   createSyncClient(HttpVersion httpVersion){

        // restTemplate.setRequestFactory();
        if(httpVersion==HttpVersion.HTTP_1_1) {
            return new PpsHttpExcutor(false, restTemplateForNetty1_1,null);
        }else {
            return new PpsHttpExcutor(false, restTemplateForNetty1_0,null);
        }

    }


    /**
     * 创建异步的请求器
     * @return
     */
    public  static PpsHttpExcutor   createASyncClient(){

        return  new PpsHttpExcutor(true,restTemplateForNetty1_1,null);

    }


    public  static PpsHttpExcutor   createASyncClient(HttpVersion httpVersion){

        if(httpVersion==HttpVersion.HTTP_1_1) {
            return new PpsHttpExcutor(false, restTemplateForNetty1_1,null);
        }else {
            return new PpsHttpExcutor(false, restTemplateForNetty1_0,null);
        }

    }


    /**
     * 特别注意 需要quit驱动 否则驱动程序会一直在
     * @param async
     * @return
     */
    public static PpsHttpExcutor createPhantojsClient(boolean async){
            return new PpsHttpExcutor(async, restTemplateForPhantomjs,null);
    }




}
