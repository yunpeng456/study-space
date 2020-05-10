package com.yunpeng.study.consumer;

import com.yunpeng.study.api.HelloRpcService;
import com.yunpeng.study.api.RpcService;
import com.yunpeng.study.consumer.proxy.RpcProxy;
import com.yunpeng.study.provider.HelloRpcServiceImpl;
import com.yunpeng.study.provider.RpcServiceImpl;

/**
 * *@ClassName RpcConsumer
 * *@Description TODO
 * *@Author yunpeng.zhao
 * *@Date 2020/5/7 9:57 下午
 **/
public class RpcConsumer {

    public static void main(String[] args) {
        //本地调用
//        HelloRpcService helloRpcService = new HelloRpcServiceImpl();
//        System.out.println("嘿嘿" + helloRpcService.hello("yunpeng"));
//        RpcService rpcService = new RpcServiceImpl();
//        System.out.println("加法" + rpcService.add(3,4));
//        System.out.println("减法" + rpcService.sub(10,9));

        //远程调用
        HelloRpcService helloRpcService = RpcProxy.create(HelloRpcService.class);
        System.out.println("远程嘿嘿" + helloRpcService.hello("yunpeng"));
        RpcService rpcService = RpcProxy.create(RpcService.class);
        System.out.println("远程加法" + rpcService.add(3,4));
        System.out.println("远程减法" + rpcService.sub(10,9));


    }
}
