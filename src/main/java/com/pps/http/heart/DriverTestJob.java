/*
 * Copyright (c) ACCA Corp.
 * All Rights Reserved.
 */
package com.pps.http.heart;

import com.pps.http.myrequest.chrome.ChromeRequestFactory;
import com.pps.http.myrequest.phantom.PhantomRequestFactory;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;

/**
 * @author Pu PanSheng, 2021/7/23
 * @version OPRA v1.0
 */
@Slf4j
public class DriverTestJob {



    public DriverTestJob(PhantomRequestFactory phantomRequestFactory, ChromeRequestFactory chromeRequestFactory,int internal){

        log.info("已开启驱动定时检测任务-----------");
        ScheduledThreadPoolExecutor scheduledThreadPoolExecutor=new ScheduledThreadPoolExecutor(1);

        scheduledThreadPoolExecutor.scheduleAtFixedRate(()->{

            log.info("驱动心跳测试定时任务开始---------");
            if (phantomRequestFactory != null) {
                log.info("phantonjs 驱动心跳测试----------");
                try {
                    phantomRequestFactory.check();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (chromeRequestFactory != null) {
                log.info("chrome 驱动心跳测试----------");
                try {
                    chromeRequestFactory.check();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }

            log.info("驱动心跳测试任务定时任务结束------");

        },3,internal,TimeUnit.MINUTES);







    }


}
