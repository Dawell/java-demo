package com.dawell.java.demo1.io;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerThread extends Thread {


    private Socket socket = null;
    private InputStream inputStream = null;
    private InputStreamReader inputStreamReader = null;
    private BufferedReader bufferedReader = null;
    private OutputStream outputStream = null;
    private PrintStream printStream = null;
    private String a;
    private String b = "地瓜地瓜,我是土豆\r\n";

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(9527);
        System.out.println("服务器已启动,等待客户连接...");
        while (true) {
            Socket socket2 = serverSocket.accept();
            ServerThread st = new ServerThread(socket2);
            st.start();
            System.out.println("此用户的IP地址为" + socket2.getInetAddress().getHostAddress());
        }
    }

    public ServerThread(Socket socket) throws IOException {
        this.socket = socket;
    }


    @Override
    public void run() {
        try {
            inputStream = socket.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        inputStreamReader = new InputStreamReader(inputStream);
        bufferedReader = new BufferedReader(inputStreamReader);
        try {
            while ((a = bufferedReader.readLine()) != null) {
                System.out.println("客户端说:" + a);
                socket.shutdownInput();
                if(a.equals("exit")){
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            outputStream = socket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        printStream = new PrintStream(outputStream);
        try {
            while (1==1) {
                printStream.write(b.getBytes());
                Thread.sleep(5000);
                System.out.println(222);

            }
//            socket.shutdownOutput();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


}
