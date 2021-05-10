package com.pps.http.strategy.request;


import com.pps.http.strategy.HttpRequstOperation;
import com.pps.http.util.UrlParamGenarete;
import org.springframework.http.client.ClientHttpRequest;

import java.io.IOException;
import java.util.Map;

/**
 * @author pps
 * @discription;默认的x-www-from请求处理方式
 * @time 2021/1/11 15:00
 */
public class DefaulXfromRequestOperation implements HttpRequstOperation {

    @Override
    public void doWithRequest(ClientHttpRequest request, Map requestParamBody)  {
        String s = UrlParamGenarete.urlParamByMap(requestParamBody, false);
        try {
            byte[] bytes = s.getBytes("utf-8");
            request.getBody().write(bytes);
        } catch (IOException e) {
           throw new RuntimeException(e);
        }

    }
}
