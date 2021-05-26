package com.heima.wemedia.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @Description:
 * @author: yp
 */
@Configuration
@ComponentScan(value = {"com.heima.common.config","com.heima.common.exception"})
public class BaseConfig {

}
