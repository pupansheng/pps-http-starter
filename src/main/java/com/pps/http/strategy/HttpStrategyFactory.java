package com.pps.http.strategy;



import com.pps.http.strategy.header.DefaultHeaderOperation;
import com.pps.http.strategy.request.DefaulJsonRequestOperation;
import com.pps.http.strategy.request.DefaulXfromRequestOperation;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author
 * @discription;
 * @time 2021/1/11 14:06
 */
public class HttpStrategyFactory {

 private static ConcurrentHashMap<Object,HttpRequstOperation> strates=new ConcurrentHashMap<>();
 private static final String DEFAULT_OPERATION="default_operation";

 static {
     strates.put("application/json",new DefaulJsonRequestOperation());
     strates.put("application/x-www-form-urlencoded",new DefaulXfromRequestOperation());
 }

 public static void register(Object k,HttpRequstOperation httpResponseOperation){
     strates.put(k,httpResponseOperation);
 }

 public static HttpRequstOperation getOperation(Object k){
     return strates.get(k);
 }

 public static void registerDefaultOperation(HttpRequstOperation httpResponseOperation){
     strates.put(DEFAULT_OPERATION,httpResponseOperation);
 }
 public static HttpRequstOperation getDefaultOperation(){
     HttpRequstOperation httpRequstOperation = strates.get(DEFAULT_OPERATION);
     if(httpRequstOperation==null){
         DefaultHeaderOperation defaultHeaderOperation = new DefaultHeaderOperation();
         strates.put(DEFAULT_OPERATION,defaultHeaderOperation);
         return defaultHeaderOperation;
     }
    return httpRequstOperation;
 }
}
