package com.tanhua.server.interceptor;


import com.tanhua.model.domain.User;

/**
 * @Function: 功能描述 工具类实现向threadlocal存储数据的方法
 * @Author: ChenXW
 * @Date: 17:05 2022/7/16
 */
public class UserHolder {

    private static ThreadLocal<User> t1=new ThreadLocal<>();

    //将用户对象存入ThreadLocal
    public static void set(User user){
        t1.set(user);
    }

    //从当前线程获取对象
    public static User get(){
        return t1.get();
    }

    //获取当前用户对象id
    public static Long getUserId(){
        return t1.get().getId();
    }

    public static String getMobile(){
        return t1.get().getMobile();
    }

    //清空
    public static void remove(){
        t1.remove();
    }
}
