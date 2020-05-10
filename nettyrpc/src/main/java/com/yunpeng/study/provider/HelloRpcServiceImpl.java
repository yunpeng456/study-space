package com.yunpeng.study.provider;

import com.yunpeng.study.api.HelloRpcService;

/**
 * *@ClassName HelloRpcServiceImpl
 * *@Description TODO
 * *@Author yunpeng.zhao
 * *@Date 2020/5/5 9:36 下午
 **/
public class HelloRpcServiceImpl implements HelloRpcService {
    @Override
    public String hello(String context) {
        return "Hello" + context +"!";
    }
}
