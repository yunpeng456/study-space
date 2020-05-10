package com.yunpeng.study.protocol;

import lombok.Data;

import java.io.Serializable;

/**
 * *@ClassName InvokerProtocol
 * *@Description TODO
 * *@Author yunpeng.zhao
 * *@Date 2020/5/5 9:28 下午
 **/
@Data
public class InvokerProtocol implements Serializable {
    /**
     * 服务名
     */
    private String className;
    /**
     * 方法名称，具体的逻辑
     */
    private String methodName;
    /**
     * 形参列表
     */
    private Class<?>[] params;

    /**
     * 实参列表
     */
    private Object[] values;
}
