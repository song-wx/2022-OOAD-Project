import java.net.*;
import java.io.*;

public class Server {
    private static ServerSocket server;
    private static int port;

    public static void main(String[] args) throws IOException {

        port = 3333;
        server = new ServerSocket(port);
        System.out.println("等待客户连接...");
        //主线程监听连接
        while(true){

            Socket client = server.accept();
            //新建线程处理连接
            ServerThread serverThread = new ServerThread(client);
            serverThread.start();
        }
    }
}