package com.yunpeng.study.registry;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

/**
 * *@ClassName RpcRegistry
 * *@Description TODO
 * *@Author yunpeng.zhao
 * *@Date 2020/5/5 9:43 下午
 **/
public class RpcRegistry {

    private int port;

    public RpcRegistry(int port) {
        this.port = port;
    }

    public void start() {
        //相当于是serverSocket | serverSocketChannel
        //基于NIO来实现
        //Selector  主线程 和工作线程

        try {
            //主线程池
            //构造方法中可以传具体的线程数量，如果为空表示当前cpu支持的线程数的2倍
            EventLoopGroup bossGroup = new NioEventLoopGroup();
            //工作线程池
            EventLoopGroup workerGroup = new NioEventLoopGroup();
            //1.创建
            ServerBootstrap server = new ServerBootstrap();
            //2.配置
            server.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            //在Netty中把所有的业务逻辑归总到一个队列中
                            //这些处理逻辑封装成一个对象，无锁化，串行任务队列，PipLine
                            //使用责任链模式，实现使用的双向链表，Inbound 、 Outbound
                            ChannelPipeline pipeline = socketChannel.pipeline();
                            //先对自定义协议进行编、解码
                            pipeline.addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4))
                                    //解码
                                    .addLast(new LengthFieldPrepender(4));
                            //实参处理
                            pipeline.addLast("encoder", new ObjectEncoder());
                            pipeline.addLast("decoder", new ObjectDecoder(Integer.MAX_VALUE, ClassResolvers.cacheDisabled(null)));

                            //前面的编解码是对数据的解析
                            //最后执行自己定义的逻辑
                            //1.注册，给每一个对象起一个名字，对外提供服务的名字
                            //2.服务的位置要做一个登记
                            pipeline.addLast(new RpcRegistryHandler());
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    //保证子线程可以一直被回收利用
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            //正式启动服务，相当于使用一个死循环开始轮训
            //3.启动
            ChannelFuture future = server.bind(this.port).sync();
            System.out.println("RPC Registry start listen at " + this.port);
            future.channel().closeFuture().sync();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new RpcRegistry(8080).start();
    }
}
