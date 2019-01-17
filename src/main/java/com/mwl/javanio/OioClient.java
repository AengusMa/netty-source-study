package com.mwl.javanio;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * @author mawenlong
 * @date 2019/01/17
 */
@Slf4j
public class OioClient {
    public static void main(String[] args) {
        String msg = "Client data";
        try {
            Socket socket = new Socket("127.0.0.1", 9090);
            //使用PrintWriter和BufferedReader发送和接收数据
            PrintWriter pw = new PrintWriter(socket.getOutputStream());
            BufferedReader is = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            //发送数据
            pw.println(msg);
            pw.flush();
            //接收数据
            String line = is.readLine();
            System.out.println("received from server:" + line);
            pw.close();
            is.close();
            socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
