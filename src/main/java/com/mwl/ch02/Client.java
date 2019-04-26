package com.mwl.ch02;

import java.io.IOException;
import java.net.Socket;

/**
 * @author mawenlong
 * @date 2019/01/10
 */
public class Client {
    private static final String HOST = "127.0.0.1";
    private static final int PORT = 8000;
    private static final int SLEEP_TIME = 1000;

    public static void main(String[] args) throws IOException {
        // NioEvent
        final Socket socket = new Socket(HOST, PORT);
        new Thread(() -> {
            System.out.println("客户端启动成功");
            for (int i=0;i<3;i++){
                try {
                    String message = "hello world";
                    System.out.println("客户端发送数据：" + message);
                    socket.getOutputStream().write(message.getBytes());

                } catch (Exception e) {
                    System.out.println("写数据出错");
                }
                sleep();
            }
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private static void sleep() {
        try {
            Thread.sleep(SLEEP_TIME);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
