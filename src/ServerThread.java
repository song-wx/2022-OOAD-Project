import java.net.*;
import java.io.*;
import java.util.*;

public class ServerThread extends Thread{
        private Socket myclient;

        //与客户端连接的输入输出流
        private BufferedReader reader;
        private BufferedWriter writer;

        //服务器端允许查看的根目录
        private final String rootPath = "F:/root";
        //当前目录路径
        private String currentPath = rootPath;
        //当前的目录
        private  File currentFile = new File(rootPath);


    public ServerThread(Socket client) {
        //将主线程监听到的用户连接分配给新线程处理
        myclient = client;
    }

    //线程启动，开始处理连接用户的命令请求
    @Override
    public void run() {
        try {
            String clientName = myclient.getInetAddress().getHostAddress() + ":" + myclient.getPort();
            System.out.println(clientName + " 连接成功！");

            //读取用户命令行输入
            reader = new BufferedReader(new InputStreamReader(myclient.getInputStream()));
            //向客户端返回服务器端的结果
            writer = new BufferedWriter(new OutputStreamWriter(myclient.getOutputStream()));

            //向用户展示根目录路径
            mywriter("Current Directory: " + rootPath);

            String clientInput;

            //接受用户的多次输入，直到用户输入exit断连
            while (true) {
                try {
                    if ((clientInput = reader.readLine()) == null) break;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                if (clientInput.equalsIgnoreCase("exit"))
                    break;
                System.out.println(clientName + " 说：" + clientInput);


                //用于区分用户输入的命令
                int commandType = 0;
                String input = new String();
                if(clientInput.startsWith("dir")) commandType = 1;
                else if(clientInput.startsWith("cd")) commandType = 2;
                else if(clientInput.startsWith("view")) commandType = 3;

                switch (commandType){

                    case 0: mywriter("Command Error!");
                            break;

                    case 1: input = clientInput.replaceFirst("dir","");
                            input = input.trim();
                            Dir(input);
                            break;

                    case 2: input = clientInput.replaceFirst("cd","");
                            input = input.trim();
                            Cd(input);
                            break;

                    case 3: input = clientInput.replaceFirst("view", "");
                            input = input.trim();
                            String input1,input2;
                            if(input.contains(" ")){
                                input1 = input.substring(0,input.indexOf(" "));
                                input2 = input.substring(input1.length()+1, input.length());
                                input2 = input2.trim();
                            }else{
                                input1 = input;
                                input2 = "";
                            }
                            View(input1, input2);

                            break;

                }
                //服务器端传输数据结束的标志
                mywriter("theend");

            }

            reader.close();
            writer.close();
            System.out.println(clientName + " 已断开");
        }catch (IOException e){
            throw new RuntimeException(e);
        }
    }


    public void Dir(String input) throws IOException {
    switch(input){
        case"": File[] Filelist = currentFile.listFiles();
                List<File> allFiles= new ArrayList<>();
                List<File> allDirectories = new ArrayList<>();

                if(Filelist==null){
                    mywriter("Current Directory Is Empty");
                    break;
                }

                mywriter("Current Directory: " + currentPath);
                //用队列把目录下的文件和文件夹分类
                for(File file:Filelist) {
                    if(file.isDirectory()) allDirectories.add(file);
                    else allFiles.add(file);
                }
                //向用户输出所有文件夹信息
                for(File file:allDirectories){
                    mywriter("[" + file.getName() + "]");
                }
                //向用户输出所有文件信息
                for(File file:allFiles){
                    mywriter(file.getName() + " " + file.length() / 1024.0 + " KB");
                }
                break;

        default: mywriter("Command Error!");
                 break;

    }
    }


    public void Cd(String input) throws IOException {
           switch(input){
               case "..": if(currentPath.equalsIgnoreCase(rootPath)){
                   mywriter("Already In Root Directory");
               }else
               {
                   currentPath = currentFile.getParent();
                   currentFile = new File(currentPath);
                   mywriter("Done");
                   mywriter("Current Directory: " + currentPath);
               }
                   break;

               default: File[] Filelist = currentFile.listFiles();
                   if(Filelist == null) {
                       mywriter("There Is No Such A Directory Named" + " " + input);
                       return;
                   }
                   //标记是否找到了对应文件
                   boolean mark = false;
                   for(File file:Filelist){
                       if(input.equals(file.getName())){
                           currentPath=file.getAbsolutePath();
                           currentFile= new File(currentPath);
                           mywriter("Current Directory: " + currentPath);
                           mark = true;
                           break;
                       }
                   }
                        if(!mark) mywriter("There Is No Such A Directory Named" + " " + input);
                        break;
           }
    }


    public void View(String str1,String str2) throws IOException {
            File[] Filelist = currentFile.listFiles();
            if(Filelist == null) {
                mywriter("There Is No Such A Directory Named" + " " + str1);
                return;
            }
            //标记是否找到了对应文件
            boolean mark = false;
            for(File file:Filelist){
                if(file.getName().equals(str1)){
                    //读取csv文件
                    ArrayList<ArrayList<String>> data = readCsv(file.getAbsolutePath());
                    //以特定顺序显示csv表格数据
                    DispInDefinedOrder(data, str2);
                    mark = true;
                    break;
                }
            }
            if(!mark) mywriter("There Is No Such A Directory Named" + " " + str1);

    }

    //合并常用的writer方法
    public void mywriter(String str) throws IOException {
        writer.write(str);
        writer.newLine();
        writer.flush();
    }


    //读取csv文件，返回数据列表
    public ArrayList<ArrayList<String>> readCsv(String path) throws IOException {
        BufferedReader in =new BufferedReader(new InputStreamReader(new FileInputStream(path),"UTF-8"));
        ArrayList<ArrayList<String>> Data=new ArrayList<ArrayList<String>>();
        //csv中单行数据,以一整个字符串为单位
        String rowInCsv;
        //将csv中的整个字符串以逗号为分隔转化为字符串数组
        String[] rowAsArray;
        while ((rowInCsv=in.readLine())!=null) {
            rowAsArray = rowInCsv.split(",");  //默认分割符为逗号
            List<String> rowlist = Arrays.asList(rowAsArray);//转化为列表
            ArrayList<String> rowaArrayList = new ArrayList<String>(rowlist);
            Data.add(rowaArrayList);
        }
        in.close();
        return Data;

    }

    public void DispInDefinedOrder(ArrayList<ArrayList<String>> allData, String str) throws IOException {
        List<Student> students = new ArrayList<>();
        List<ArrayList<String>> Data = allData.subList(1,allData.size());
        String lable = String.format("%10s  %10s  %10s  %10s  %10s  %10s  %10s", allData.get(0).get(0), allData.get(0).get(1), allData.get(0).get(2), allData.get(0).get(3), allData.get(0).get(4), allData.get(0).get(5), allData.get(0).get(6));
        switch(str){
            case "ID"   :    for(ArrayList<String> data : Data) students.add(new Student(data, 0));
                             Collections.sort(students);
                             mywriter(lable);
                             for (Student student : students) {
                                 student.Disp();
                             }
                             break;
            case "EXAM" :    for(ArrayList<String> data : Data) students.add(new Student(data, 1));
                             Collections.sort(students);
                             mywriter(lable);
                             for (Student student : students) {
                                student.Disp();
                             }
                             break;
            case "HW1"  :    for(ArrayList<String> data : Data) students.add(new Student(data, 2));
                             Collections.sort(students);
                             mywriter(lable);
                             for (Student student : students) {
                                 student.Disp();
                             }
                             break;
            case "HW2"  :    for(ArrayList<String> data : Data) students.add(new Student(data, 3));
                             Collections.sort(students);
                             mywriter(lable);
                             for (Student student : students) {
                                 student.Disp();
                             }
                             break;
            case "HW3"  :    for(ArrayList<String> data : Data) students.add(new Student(data, 4));
                             Collections.sort(students);
                             mywriter(lable);
                             for (Student student : students) {
                                 student.Disp();
                             }
                             break;
            case "Overall"  :for(ArrayList<String> data : Data) students.add(new Student(data, 5));
                             Collections.sort(students);
                             mywriter(lable);
                             for (Student student : students) {
                                 student.Disp();
                             }
                             break;

            case "":         for(ArrayList<String> data : Data) students.add(new Student(data, 0));//不进行排序
                             mywriter(lable);
                             for (Student student : students) {
                                 student.Disp();
                             }
                             break;

            default:         mywriter("There Is Not A Type named " + str +", The Table Is Shown In Default Order");
                             for(ArrayList<String> data : Data) students.add(new Student(data, 0));//不进行排序
                             mywriter(lable);
                             for (Student student : students) {
                                 student.Disp();
                             }
        }
    }

    public class Student implements Comparable<Student> {
        ArrayList<Integer> data = new ArrayList<>();
        String name;
        int Order;

        public Student(ArrayList<String> initdata, int order) {
            data.add(0, Integer.parseInt(initdata.get(0)));
            data.add(1, Integer.parseInt(initdata.get(2)));
            data.add(2, Integer.parseInt(initdata.get(3)));
            data.add(3, Integer.parseInt(initdata.get(4)));
            data.add(4, Integer.parseInt(initdata.get(5)));
            data.add(5, Integer.parseInt(initdata.get(6)));
            name = initdata.get(1);
            Order = order;
        }

        @Override
        public int compareTo(Student o) {
            return this.data.get(Order) - o.data.get(Order);
        }

        public void Disp() throws IOException {
            String str = String.format("%10d  %10s  %10d  %10d  %10d  %10d  %10d",data.get(0), name, data.get(1), data.get(2), data.get(3), data.get(4), data.get(5));
            mywriter(str);
        }
    }
}
