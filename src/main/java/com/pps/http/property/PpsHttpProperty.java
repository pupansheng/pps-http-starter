/*
 * Copyright (c) ACCA Corp.
 * All Rights Reserved.
 */
package com.pps.http.property;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author Pu PanSheng, 2021/5/8
 * @version OPRA v1.0
 */
@Configuration
@ConfigurationProperties("pps.http")
@Getter
@Setter
public class PpsHttpProperty {


    /**
     * 是否开启自动配置
     */
    private boolean enable;

    /**
     * 是否开启http1_1自动配置
     */
    private boolean enableforHttp1=true;
    /**
     * 是否开启http1_0自动配置
     */
    private boolean enableforHttp0=false;
    /**
     * 是否开启phatomjs自动配置
     */
    private boolean enableforPhantom=false;
    /**
     * phantomJs 驱动位置
     */
    private String path;

    /**
     * phantomjs 等待时间设置 单位秒
     */
    private int implicitlyWait=10;

    /**
     * paantomJs 驱动缓存大小
     */
    private  int buffSize=2;
    /**
     *
     * http默认连接时间 默认无限 仅对普通请求管用  phatomjs无效
     */
    private int connectTime=-1;
    /**
     * http read时间
     */
    private int readTimeout=-1;

    /**
     * http1_1 线程池大小
     */
    private int theadForHttp1=1;

    /**
     * http1_0 线程池大小
     */
    private int theadForHttp0=1;





}
