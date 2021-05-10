package com.pps.http.strategy.request;

import com.alibaba.fastjson.JSON;
import com.pps.http.strategy.HttpRequstOperation;
import org.springframework.http.client.ClientHttpRequest;

import java.io.IOException;
import java.util.Map;

/**
 * @author pps
 * @discription;默认的json请求处理方式
 * @time 2021/1/11 15:00
 */
public class DefaulJsonRequestOperation implements HttpRequstOperation {

    @Override
    public void doWithRequest(ClientHttpRequest request, Map requestParamJsonBody)  {

        try {
            String s = JSON.toJSONString(requestParamJsonBody);
            request.getBody().write(s.getBytes("utf-8"));
        } catch (IOException e) {
           throw new RuntimeException(e);
        }

    }
}
