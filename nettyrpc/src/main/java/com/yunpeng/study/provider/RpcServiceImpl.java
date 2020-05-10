package com.yunpeng.study.provider;

import com.yunpeng.study.api.RpcService;

/**
 * *@ClassName RpcServiceImpl
 * *@Description TODO
 * *@Author yunpeng.zhao
 * *@Date 2020/5/5 9:37 下午
 **/
public class RpcServiceImpl implements RpcService {
    @Override
    public int add(int a, int b) {
        return a + b;
    }

    @Override
    public int sub(int a, int b) {
        return a - b;
    }
}
