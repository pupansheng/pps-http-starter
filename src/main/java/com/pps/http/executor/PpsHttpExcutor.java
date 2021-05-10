package com.pps.http.executor;


import com.pps.http.PpsHttp;
import com.pps.http.PpsHttpUtil;
import com.pps.http.PpsHttpUtilForSpring;
import com.pps.http.myrequest.phantom.PhantomClientHttpRequest;
import com.pps.http.myrequest.phantom.PhantomClientHttpResponse;
import com.pps.http.strategy.HttpRequstOperation;
import com.pps.http.strategy.HttpStrategyFactory;
import com.pps.http.util.PpsAsyncUtil;
import com.pps.http.util.UrlParamGenarete;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author
 * @discription;
 * @time 2021/1/21 14:52
 */
@Slf4j
public class PpsHttpExcutor {

    private RestTemplate restTemplate;
    private static final String POST="post";
    private static final String GET="get";


    private boolean async;

    private boolean strict;

    private boolean isDebug;

    private Object[] params;

    private String url;
    /*
      是否在解析url参数的时候添加？号
     */
    private boolean addW;
    /**
     * 是否对get请求中中文encod
     */
    private boolean getRequestEncode=true;

    /**
     * 是否自动把响应转换为字符串
     */
    private boolean autoTransforString=true;

    /*
     自定义请求头
     */
    private Consumer<HttpHeaders> httpHeadersConsumer;
    /*
     自定义请求参数
     */
    private Consumer<ClientHttpRequest> clientHttpRequestConsumer;
    /*
     自定义错误捕捉
     */
    private Consumer<Throwable> catchCallback;

    /**
     * 浏览模式 默认的为电脑浏览器
     */
    private  boolean isMobile=false;


    /**
     * 默认的请求类型
     */
    private MediaType mediaType=MediaType.APPLICATION_FORM_URLENCODED;

    /**
     * 父类转换接口
     */
    private PpsHttp ppsHttp;

    /**
     * 自动将http请求的响应转换为bytes[] 策略
     */
    private Function<ClientHttpResponse,byte []> responseTobytesStrategy=response -> {

        try {
            InputStream body = response.getBody();
            int available = body.available();
            byte[] bytes = new byte[available];
            body.read(bytes);
            return  bytes;
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    };



    public PpsHttpExcutor setResponseTobytesStrategy(Function<ClientHttpResponse, byte[]> responseTobytesStrategy) {
        this.responseTobytesStrategy = responseTobytesStrategy;
        return  this;
    }

    /**
     * post请求遇到重定向时的策略 若为空则自己处理 不为空则应该返回一个新的主url
     */
    private BiFunction<ClientHttpResponse,String,String> _3xxStrategy=(r,l)->{

        URI location = r.getHeaders().getLocation();
        return location.toString();
        /*String host = location.getHost();
        if(host==null||"".equals(host)){
            throw new RuntimeException("host为空：系统自定义重定向策略失败！请手动设置");
        }
        String path = location.getPath();

        String query = location.getQuery();
        //解析条件
        String s=query;

        if(path!=null||!"".equals(path)) {
            String pa = "";
            String qa = "";
            boolean paT = true;
            List<String> list = new ArrayList<>();

            if(s!=null) {
                for (int i = 0; i < s.length(); i++) {
                    String sc = s.substring(i, i + 1);
                    if (paT) {
                        if ("=".equals(sc)) {
                            list.add(pa);
                            paT = false;
                            pa = "";
                            qa = "";
                        } else {
                            pa += sc;
                        }
                    } else {
                        if ("&".equals(sc)) {
                            list.add(qa);
                            paT = true;
                            pa = "";
                            qa = "";
                        } else {
                            qa += sc;
                        }
                    }
                }
            }
            list.add(qa);
            Map<Object, Object> param = new HashMap<>();
            for (int i = 0; i < list.size(); i++) {
                param.put(list.get(i), list.get(++i));
            }
            param.forEach((k, v) -> {
                String env = URLDecoder.decode((String) v);
                param.put(k, env);
            });
            String query2 = UrlParamGenarete.urlParamByMap(param, false,false);
            String scheme = location.getScheme();
            l=scheme+"://"+host+path+"?"+query2;
        }


        return l;*/
    };

    /**
     * @param asyn
     */
    public PpsHttpExcutor(boolean asyn,RestTemplate restTemplate,PpsHttp ppsHttp) {
        this.async = async;
        this.restTemplate=restTemplate;
        this.ppsHttp=ppsHttp;

    }

    /**
     *
     * @param autoTransforString 是否自动转换为string
     * @return
     */
    public PpsHttpExcutor setAutoTransforString(boolean autoTransforString) {
        this.autoTransforString=autoTransforString;
        return this;
    }
    /**
     *
     * @param params 第一个参数为附加在url上的参数  后面的为在方法体中 类型只能为map或者实体类
     * @return
     */
    public PpsHttpExcutor setParam(Object... params) {
        this.params = params;
        return this;
    }

    /**
     * 设置url
     * @param url
     * @return
     */
    public PpsHttpExcutor setUrl(String url) {
        this.url = url;
        return this;
    }

    /**
     * 当把第一个参数转化为url参数携带后 是否需要在其前面添加？号
     * @param w
     * @return
     */
    public PpsHttpExcutor setAddW(boolean w) {
        this.addW = w;
        return this;
    }

    /**
     * 是否开启debug模式
     * @param debug
     * @return
     */
    public PpsHttpExcutor setDebug(boolean debug) {
        this.isDebug = debug;
        return this;
    }
    /**
     * 是否严格模式  严格模式：请求不是200响应就会抛出错误
     * @param strict
     * @return
     */
    public PpsHttpExcutor setStrict(boolean strict) {
        this.strict = strict;
        return this;
    }

    /**
     * 设置浏览模式 默认为电脑浏览器
     * @param isMobile
     * @return
     */
    public PpsHttpExcutor setEquimentMode(boolean isMobile){

        this.isMobile=isMobile;
        return  this;
    }

    /**
     * 对于get请求 url上面的参数 是否需要转码
     * @param requestEncoding
     * @return
     */
    public PpsHttpExcutor setGetRequestEncoding(boolean requestEncoding) {
        this.getRequestEncode=requestEncoding;
        return this;
    }

    /**
     * 设置自定义请求头
     * @param httpHeadersConsumer
     * @return
     */
    public PpsHttpExcutor setHttpHeadersConsumer(Consumer<HttpHeaders> httpHeadersConsumer) {
        this.httpHeadersConsumer = httpHeadersConsumer;
        return this;
    }

    /**
     * 设置自定义请求构造
     * @param clientHttpRequestConsumer
     * @return
     */
    public PpsHttpExcutor setClientHttpRequestConsumer(Consumer<ClientHttpRequest> clientHttpRequestConsumer) {
        this.clientHttpRequestConsumer = clientHttpRequestConsumer;
        return this;
    }

    /**
     * 设置自定义错误捕捉
     * @param catchCallback
     * @return
     */
    public PpsHttpExcutor setCatchCallback(Consumer<Throwable> catchCallback) {
        this.catchCallback = catchCallback;
        return this;
    }


    /**
     * 默认重定向策略处理
     */
    private boolean auto3xxStrategy=true;


    /**
     * 是否自动重定向
     * @param auto3xxStrategy
     * @return
     */
    public PpsHttpExcutor setAuto3xxStrategy(boolean auto3xxStrategy) {
        this.auto3xxStrategy = auto3xxStrategy;
        return this;
    }

    /**
     * 自定义重定向时的策略
     * @param _3xxStrategy
     */
    public PpsHttpExcutor set_3xxStrategy(BiFunction<ClientHttpResponse, String, String> _3xxStrategy) {
        this._3xxStrategy = _3xxStrategy;
        return this;
    }

    /**
     * 必须检查
     */
    private void check() {
        if (url == null) {
            throw new RuntimeException("url---> 不能为空！");
        }
    }

    /**
     * 发起post json请求
     * @param callback
     */
    public void postJson(BiConsumer<ClientHttpResponse,String> callback) {
        mediaType=MediaType.APPLICATION_JSON_UTF8;
        submit(POST, callback);
    }

    /**
     * 发起post xform
     * @param callback
     */
    public void postXWFrom(BiConsumer<ClientHttpResponse,String> callback) {
        mediaType=MediaType.APPLICATION_FORM_URLENCODED;
        submit(POST, callback);
    }

    /**
     * get请求
     * @param callback
     */
    public void get(BiConsumer<ClientHttpResponse, String> callback) {
        mediaType=MediaType.APPLICATION_FORM_URLENCODED;
        submit(GET, callback);
    }

    /**
     * @param method
     * @param callback
     */
    private void submit(String method, BiConsumer<ClientHttpResponse,String> callback) {

        check();


        HttpRequstOperation defaultOperation = HttpStrategyFactory.getDefaultOperation();

        if (catchCallback == null) {
            catchCallback = (e) -> {
                e.printStackTrace();
                throw new RuntimeException(e);
            };
        }
        //解析携带参数
        Map requestParamBody = new HashMap();
        Map requestUrlPram = new HashMap();
        //参数解析
        int[] count = new int[1];
        if(params!=null)
        for (Object ab : params) {
            if (ab instanceof Number || ab instanceof String) {
                throw new RuntimeException("警告：" + ab + "请求参数为基本类型和字符串类型 不符合规范 必须为自定义类和map类型");
            }
            if (ab instanceof Map) {
                if (count[0] == 0) {
                    requestUrlPram.putAll((Map) ab);
                } else {
                    requestParamBody.putAll((Map) ab);
                }

            } else {
                if (ab == null) {
                    count[0]++;
                    continue;
                }
                Class<?> aClass = ab.getClass();
                Field[] declaredFields = aClass.getDeclaredFields();
                Arrays.stream(declaredFields).forEach(f -> {
                    f.setAccessible(true);
                    String name = f.getName();
                    try {
                        Object o = f.get(ab);
                        if (o != null) {
                            if (count[0] == 0) {
                                requestUrlPram.put(name, o);
                            } else {
                                requestParamBody.put(name, o);
                            }
                        }
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                        throw new RuntimeException("解析请求参数错误：" + e.getMessage());
                    }
                });
                count[0]++;
            }
        }


        //根据请求类型 构造request条件
        if (clientHttpRequestConsumer == null) {

            clientHttpRequestConsumer = (r) -> {
                MediaType contentType = r.getHeaders().getContentType();
                String type = contentType.toString();
                HttpRequstOperation operation = HttpStrategyFactory.getOperation(type);
                if (operation != null) {
                    operation.doWithRequest(r, requestParamBody);
                }else {
                    throw new RuntimeException("对于MediaType:"+type+"  没有默认的请求构造策略 请自定义");
                }
            };

        }

        //请求头设置
        HttpHeaders headers = new HttpHeaders();
        if(httpHeadersConsumer==null){
            httpHeadersConsumer=(h)->{
                defaultOperation.defaultHeaderOperation(h);
            };
        }else {
            defaultOperation.defaultHeaderOperation(headers);
        }
        headers.setContentType(mediaType);

        if(isMobile){
            headers.set("user-agent","Mozilla/5.0 (iPhone; CPU iPhone OS 13_2_3 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.0.3 Mobile/15E148 Safari/604.1 Edg/88.0.4324.96");
        }else {
            headers.set("user-agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.93 Safari/537.36 Edg/90.0.818.56");
        }
        httpHeadersConsumer.accept(headers);

        // 准备网络请求
        String httpUrl = url + UrlParamGenarete.urlParamByMap(requestUrlPram, addW,getRequestEncode);

        HttpMethod httpMethod = "post".equals(method) ? HttpMethod.POST : HttpMethod.GET;

        Consumer<ClientHttpRequest> finalClientHttpRequestConsumer = clientHttpRequestConsumer;

        Consumer<Throwable> finalCatchCallback = catchCallback;

        if (async) {

            PpsAsyncUtil.submit(() -> {
                return restTemplate.execute(httpUrl, httpMethod, new RequestCallback() {
                    @Override
                    public void doWithRequest(ClientHttpRequest request) throws IOException {

                        if(!(request instanceof PhantomClientHttpRequest)) {
                            request.getHeaders().addAll(headers);
                            finalClientHttpRequestConsumer.accept(request);
                        }

                    }
                }, new ResponseExtractor<Object>() {
                    @Override
                    public Object extractData(ClientHttpResponse response) throws IOException {
                        // 结果解析

                        try {


                            if (strict && response.getStatusCode() != HttpStatus.OK) {
                                throw new RuntimeException(httpUrl + ":请求失败！");
                            }
                            if (response.getStatusCode().is3xxRedirection() && auto3xxStrategy) {
                                URI location = response.getHeaders().getLocation();
                                if (_3xxStrategy == null) {
                                    log.warn("请注意：网址被定向！请自定义处理》》》》》》》》》》》》》》》》》》》》》》》》》:" + location.toString());
                                } else {
                                    AtomicReference<ClientHttpResponse> t = new AtomicReference<>();
                                    String newUrl = _3xxStrategy.apply(response, location.toString());
                                    log.warn("请注意：网址被定向！新网址为：" + newUrl);
                                    if (ppsHttp == null) {
                                        PpsHttpUtil.createSyncClient().setAutoTransforString(false).setUrl(newUrl).get((r, s) -> {
                                            t.set(r);
                                        });
                                    } else {
                                        PpsHttpUtilForSpring ppsHttp = (PpsHttpUtilForSpring) PpsHttpExcutor.this.ppsHttp;
                                        ppsHttp.createSyncClient().setAutoTransforString(false).setUrl(newUrl).get((r, s) -> {
                                            t.set(r);
                                        });
                                    }
                                    return t.get();
                                }
                            }
                            return response;

                        }catch (Exception e){

                            if(response instanceof PhantomClientHttpResponse){
                                PhantomClientHttpResponse phantomJs = (PhantomClientHttpResponse) response;
                                phantomJs.returnDiver();
                            }
                            throw new RuntimeException(e);
                        }

                    }


                });
            }, (t) -> {
                ClientHttpResponse response = (ClientHttpResponse) t;
                //尝试把相应转换
                try{
                    if(autoTransforString) {
                        callback.accept(response,responseToString(response));
                    }else {
                        callback.accept(response,null);
                    }
                }finally {
                    if(response instanceof PhantomClientHttpResponse){
                        PhantomClientHttpResponse phantomJs = (PhantomClientHttpResponse) response;
                        phantomJs.returnDiver();
                    }
                }


            }, (e) -> {

                finalCatchCallback.accept(e);
            });
        } else {

            try (ClientHttpResponse response = (ClientHttpResponse) restTemplate.execute(httpUrl, httpMethod, new RequestCallback() {
                @Override
                public void doWithRequest(ClientHttpRequest request) throws IOException {
                    if (!(request instanceof PhantomClientHttpRequest)) {
                        request.getHeaders().addAll(headers);
                        finalClientHttpRequestConsumer.accept(request);
                    }
                }
            }, new ResponseExtractor<Object>() {
                @Override
                public Object extractData(ClientHttpResponse response) throws IOException {
                    // 结果解析
                    try {

                        if (strict && response.getStatusCode() != HttpStatus.OK) {
                            throw new RuntimeException(httpUrl + ":请求失败！");
                        }

                        if (response.getStatusCode().is3xxRedirection() && auto3xxStrategy) {
                            URI location = response.getHeaders().getLocation();
                            if (_3xxStrategy == null) {
                                log.warn("请注意：网址被定向！请自定义处理》》》》》》》》》》》》》》》》》》》》》》》》》:" + location.toString());
                            } else {
                                AtomicReference<ClientHttpResponse> t = new AtomicReference<>();
                                String newUrl = _3xxStrategy.apply(response, location.toString());
                                log.warn("请注意：网址被定向！新网址为：" + newUrl);
                                if (ppsHttp == null) {
                                    PpsHttpUtil.createSyncClient().setAutoTransforString(false).setUrl(newUrl).get((r, s) -> {
                                        t.set(r);
                                    });
                                } else {
                                    PpsHttpUtilForSpring ppsHttp = (PpsHttpUtilForSpring) PpsHttpExcutor.this.ppsHttp;
                                    ppsHttp.createSyncClient().setAutoTransforString(false).setUrl(newUrl).get((r, s) -> {
                                        t.set(r);
                                    });
                                }
                                return t.get();
                            }
                        }
                        return response;
                    }catch (Exception e){

                        if (response instanceof PhantomClientHttpResponse) {
                            PhantomClientHttpResponse phantomJs = (PhantomClientHttpResponse) response;
                            phantomJs.returnDiver();
                        }

                        throw new RuntimeException(e);
                    }

                }


            })) {

                try {
                    if (autoTransforString) {
                        callback.accept(response, responseToString(response));
                    } else {
                        callback.accept(response, null);
                    }
                } catch (Exception e) {
                    finalCatchCallback.accept(e);
                } finally {

                    if (response instanceof PhantomClientHttpResponse) {
                        PhantomClientHttpResponse phantomJs = (PhantomClientHttpResponse) response;
                        phantomJs.returnDiver();
                    }

                }
            }



        }
    }

    private String responseToString(ClientHttpResponse response){

        if(response instanceof PhantomClientHttpResponse){

            PhantomClientHttpResponse phantomJs = (PhantomClientHttpResponse) response;
            PhantomJSDriver driver = phantomJs.getDriver();
            String pageSource = driver.getPageSource();
            return  pageSource;
        }

        MediaType contentType = response.getHeaders().getContentType();
        byte [] bytes= null;
        try {
            bytes =responseTobytesStrategy.apply(response);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("response 转换为String失败：");
        }
        Charset charset = contentType.getCharset();
        if(charset==null){
            charset=Charset.forName("utf-8");
        }
        String s = new String(bytes, charset);
        return s;
    }

}