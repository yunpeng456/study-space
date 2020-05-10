package com.yunpeng.study.registry;

import com.yunpeng.study.protocol.InvokerProtocol;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * *@ClassName RpcRegistryHander
 * *@Description TODO
 * *@Author yunpeng.zhao
 *     //1.根据一个包名将所有符合条件的Class全部扫描出来，放到一个容器之中
 *     //如果是分布式式就是读取配置文件
 *     //2.给每一个Class起一个唯一的名字作为服务的名称，放到容器中
 *     //3.当有客户端连接过来以后，获取协议内容（我们定义好的InvokerProtocol）
 *     //4.从注册好的容器中找到符合条件的服务
 *     //5.使用远程调用provider得到返回结果并返回给客户端
 * *@Date 2020/5/6 8:23 上午
 **/
public class RpcRegistryHandler extends ChannelInboundHandlerAdapter {



    private List<String> classNames = new ArrayList<>();

    private Map<String,Object> registryMap = new ConcurrentHashMap<>();

    public RpcRegistryHandler() {

        //1.根据一个包名将所有符合条件的Class全部扫描出来，放到一个容器之中
        doScanner("com.yunpeng.study.provider");
        //服务进行注册
        doRegistry();
    }

    private void doRegistry() {

        if (classNames.isEmpty()) {
            return;
        }
        try {

            for (String className : classNames) {
                Class<?> clazz = Class.forName(className);
                //接口的名称作为服务的名称
                Class<?> i = clazz.getInterfaces()[0];
                String serviceName = i.getName();
                //这里本来应该存放的是网络路径，从配置文件读取，
                //在调用的时候再去解析 ,这里简单直接使用反射
                registryMap.put(serviceName,clazz.newInstance());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void doScanner(String packagePath) {
        URL url = this.getClass().getClassLoader().getResource(packagePath.replaceAll("\\.", "/"));
        File files = new File(url.getFile());

        for (File file : files.listFiles()) {
            if (file.isDirectory()) {
                doScanner(packagePath + "." + file.getName());
            } else {
                classNames.add(packagePath +"."+ file.getName().replace(".class", ""));
            }
        }


    }

    /**
     * 3.当有客户端连接过来时，获取协议内容，通过反射调用对应方法
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Object result = new Object();
        InvokerProtocol request = (InvokerProtocol) msg;
        //4.从注册好的容器中找到符合条件的服务
        if (registryMap.containsKey(request.getClassName())){
            Object service = registryMap.get(request.getClassName());
            Method method = service.getClass().getMethod(request.getMethodName(), request.getParams());

            result = method.invoke(service, request.getValues());
        }
        //5.使用远程调用provider得到返回结果并返回给客户端
        ctx.write(result);
        ctx.flush();
        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {

    }
}
