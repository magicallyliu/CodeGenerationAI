package com.liuh.codegenerationbackend.tatelimter.enums;

/**
 * @Description  限流类型
 */

@SuppressWarnings("all")

public enum RateLimitType {

    /**
     * 接口限流
     */
    API,

    /**
     *  用户限流
     */
    USER,

    /**
     * IP限流
     */
    IP
}
