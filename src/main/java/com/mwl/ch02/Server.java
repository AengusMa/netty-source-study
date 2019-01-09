package com.mwl.ch02;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author mawenlong
 * @date 2019/01/09
 */
public class Server {
    private ServerSocket serverSocket;

    public Server(int port){
        try {
            serverSocket= new ServerSocket(port);
            System.out.println("服务启动成功，端口："+port);
        } catch (IOException e) {
            System.out.println("服务启动失败");
        }
    }
    public void start(){
        new Thread(()->{
           doStart();
        }).start();
    }
    private void doStart(){
        while(true){
            try {
                Socket client = serverSocket.accept();
                new ClientHandler(client).start();
            } catch (IOException e) {
                System.out.println("服务端异常");
            }
        }
    }
}
