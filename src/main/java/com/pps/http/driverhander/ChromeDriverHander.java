package com.pps.http.driverhander;/*
 * Copyright (c) ACCA Corp.
 * All Rights Reserved.
 */


import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.remote.DesiredCapabilities;

/**
 * @author Pu PanSheng, 2021/5/8
 * @version OPRA v1.0
 */
public interface ChromeDriverHander {


    /**
     * 浏览器自定义
     * @param dcaps
     */
    void  customPhantomJsDriver(ChromeOptions dcaps);

    /**
     * 驱动自定义
     */
    void  driverCustom(ChromeDriver[] chromeDrivers);
}
