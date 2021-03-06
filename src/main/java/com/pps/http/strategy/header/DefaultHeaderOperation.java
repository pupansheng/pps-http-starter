package com.pps.http.strategy.header;

import com.pps.http.strategy.HttpRequstOperation;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequest;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author
 * @discription;
 * @time 2021/1/11 15:00
 */
public class DefaultHeaderOperation implements HttpRequstOperation {
    @Override
    public void defaultHeaderOperation(HttpHeaders headers) {

        if(!headers.containsKey("Accept")){
            headers.add("Accept", MediaType.ALL.toString());
        }
        if(!headers.containsKey("Accept-Charset")){
            List<Charset> charsets =new ArrayList<>();
            charsets.add(StandardCharsets.UTF_8);
            headers.setAcceptCharset(charsets);
        }
        if(!headers.containsKey("Content-Type")){
            headers.setContentType(MediaType.APPLICATION_JSON);
        }

    }

}
