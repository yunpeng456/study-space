package com.yunpeng.study.consumer.proxy;

import com.yunpeng.study.protocol.InvokerProtocol;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * *@ClassName RpcProxy
 * *@Description TODO
 * *@Author yunpeng.zhao
 * *@Date 2020/5/7 10:02 下午
 **/
public class RpcProxy {

    public static<T> T create(Class<?> clazz){
        MethodProxy methodProxy = new MethodProxy(clazz);
        T result = (T)Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, methodProxy);
        return result;
    }

    /**
     * 通过代理对象实现远程网络调用
     */
    private static class MethodProxy implements InvocationHandler{

        private Class<?> clazz;

        public MethodProxy(Class clazz){
            this.clazz = clazz;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            //如果被代理得类不是一个接口，而是一个类
            if(Object.class.equals(method.getDeclaringClass())){
                return method.invoke(this,args);
            }else {
                return rpcInvoker(proxy,method,args);
            }
        }

        private Object rpcInvoker(Object proxy, Method method, Object[] args) {
            //先构造一个协议信息
            InvokerProtocol msg = new InvokerProtocol();
            msg.setClassName(this.clazz.getName());
            msg.setMethodName(method.getName());
            msg.setParams(method.getParameterTypes());
            msg.setValues(args);

            final RpcProxyHandler rpcProxyHandler = new RpcProxyHandler();

            //发送网络请求
            EventLoopGroup workGroup = new NioEventLoopGroup();

            try {
                Bootstrap client = new Bootstrap();
                client.group(workGroup)
                        .channel(NioSocketChannel.class)
                        .option(ChannelOption.TCP_NODELAY,true)
                        .handler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel socketChannel) throws Exception {
                                ChannelPipeline pipeline = socketChannel.pipeline();
                                //先对自定义协议进行编、解码
                                pipeline.addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE,0,4,0,4))
                                        //解码
                                        .addLast(new LengthFieldPrepender(4));
                                //实参处理
                                pipeline.addLast("encoder",new ObjectEncoder());
                                pipeline.addLast("decoder",new ObjectDecoder(Integer.MAX_VALUE, ClassResolvers.cacheDisabled(null)));

                                //前面的编解码是对数据的解析
                                //最后执行自己定义的逻辑
                                //调用网络服务
                                pipeline.addLast(rpcProxyHandler);
                            }
                        });

                ChannelFuture future = client.connect("localhost", 8080).sync();
                future.channel().writeAndFlush(msg).sync();
                future.channel().closeFuture().sync();
            } catch (Exception e) {
                e.printStackTrace();
            }finally {
                workGroup.shutdownGracefully();
            }

            return rpcProxyHandler.getResponse();
        }
    }
}
