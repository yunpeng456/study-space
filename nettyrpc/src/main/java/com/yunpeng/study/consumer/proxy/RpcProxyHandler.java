package com.yunpeng.study.consumer.proxy;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * *@ClassName RpcConsumerHandler
 * *@Description TODO
 * *@Author yunpeng.zhao
 * *@Date 2020/5/7 10:59 下午
 **/
public class RpcProxyHandler extends ChannelInboundHandlerAdapter {

    private Object response;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        response =msg;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        System.out.println("client is exception");
    }

    public Object getResponse() {
        return response;
    }
}
