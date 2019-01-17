package com.mwl.javanio;

import com.google.common.base.Charsets;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author mawenlong
 * @date 2019/01/17
 */
@Slf4j
public class PlainOioServer {
    public void server(int port) throws IOException {
        //创建ServerSocket监听port端口
        final ServerSocket serverSocket = new ServerSocket(port);
        try {
            while (true) {
                //阻塞等待请求
                final Socket clientSocket = serverSocket.accept();
                log.info("Accepted connection from " + clientSocket);
                new Thread(() -> {
                    OutputStream out;
                    try {
                        //接收数据
                        BufferedReader is = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                        String line = is.readLine();
                        System.out.println("receive from client" + line);
                        //发送数据
                        out = clientSocket.getOutputStream();
                        out.write("hello \n".getBytes(Charsets.UTF_8));
                        out.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            clientSocket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        new PlainOioServer().server(9090);
    }
}
