import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client1 {

    private static Socket socket;

    //与服务器端连接的输入输出流
    private static BufferedWriter writer;
    private static BufferedReader reader;

    public static void main(String[] args) throws IOException {

        socket = new Socket("127.0.0.1", 3333);
        //用于写入指令，传送给服务器端
        writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        //用于接受服务器端传输的数据
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        System.out.println("服务器连接成功！");
        Scanner input = new Scanner(System.in);
        //接受服务器的初始输出，并展示
        String serverOutput = reader.readLine();
        System.out.println(serverOutput);

        while (true) {
            //传输用户输入给服务器端
            String clientInput = input.nextLine();
            writer.write(clientInput);
            writer.newLine();
            writer.flush();

            //退出连接
            if (clientInput.equalsIgnoreCase("exit"))
                break;

            //持续读取服务器的输出，并显示
            while(true) {

                serverOutput=reader.readLine();
                if(serverOutput.equals("theend"))break;
                else System.out.println(serverOutput);

            }


        }
        socket.close();
        System.out.println("连接已断开！");
    }
}
