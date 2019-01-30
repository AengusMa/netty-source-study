package com.mwl.ch10;

import io.netty.util.concurrent.FastThreadLocal;

/**
 * @author mawenlong
 * @date 2019/1/30
 */
public class FastThreadLocalTest {
    private static FastThreadLocal<Object> threadLocal0 = new FastThreadLocal<Object>() {
        @Override
        protected Object initialValue() {
            return new Object();
        }

        @Override
        protected void onRemoval(Object value) throws Exception {
            System.out.println("onRemoval");
        }
    };

    public static void main(String[] args) {
        new Thread(() -> {
            Object object = threadLocal0.get();
            // .... do with object
            System.out.println(object);
            threadLocal0.set(new Object());
            // while (true) {
            //     threadLocal0.set(new Object());
            //     try {
            //         Thread.sleep(1);
            //     } catch (InterruptedException e) {
            //         e.printStackTrace();
            //     }
            // }
        }).start();

        new Thread(() -> {
            Object object = threadLocal0.get();
            // ... do with object
            System.out.println(object);
            // while (true) {
            //     System.out.println(threadLocal0.get() == object);
            //     try {
            //         Thread.sleep(1000);
            //     } catch (InterruptedException e) {
            //         e.printStackTrace();
            //     }
            // }
        }).start();
    }
}
