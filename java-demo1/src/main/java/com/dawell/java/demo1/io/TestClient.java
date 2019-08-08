package com.dawell.java.demo1.io;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class TestClient {
    public static void main(String[] args) {






        Socket socket=null;
        try {
            socket=new Socket("127.0.0.1",9527);
            SendThread send=new SendThread(socket);
            ReceiveThread receive=new ReceiveThread(socket);
            receive.start();
            send.start();

            send.join();
            receive.join();

        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try {
            socket.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public static class SendThread extends Thread{
        private Socket socket;

        public SendThread(Socket socket) {
            super();
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                OutputStream os=socket.getOutputStream();
                PrintStream ps=new PrintStream(os);
                Scanner input=new Scanner(System.in);
                while (true) {
                    System.out.println("please input");
                    String string = input.nextLine();
                    ps.println(string);
                    if ("bye".equals(string)) {
                        break;
                    }

                }
                ps.close();

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public static class ReceiveThread extends Thread{
        private Socket socket;

        public ReceiveThread(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
// 一旦服务器强制关闭，这边socket认为还是开放的，所以readLine一直都是null 数据 很奇怪，因为是socket异常关闭 socket内部当成ok的，实际对方终止了，所以每次读取都是终止符，但是内容是空的

                // 注意这里接收一行 以\r\n结尾，否则会一直卡主
                BufferedReader br=new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String string;
                while (true) {
                    string = br.readLine();
                    System.out.println("revice"+string);
                    if ("bye".equals(string)) {
                        break;
                    }
                    System.out.println(string);
                }
                br.close();

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

}