package p2p;

import connect.network.nio.NioServerFactory;
import task.executor.TaskExecutorPoolManager;
import util.LogDog;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ServerBoot {

    private static final int DEFAULT_PORT = 7878;

    public static void main(String[] args) {
        if (args.length > 2) {
            printCmdTitle();
            return;
        }

        int port = DEFAULT_PORT;
        P2PServer server;

        if (args.length == 2) {
            String address = args[0];
            try {
                port = Integer.parseInt(args[1]);
            } catch (Exception e) {
                LogDog.d("非法的端口号！！！");
                printCmdTitle();
                return;
            }
            server = new P2PServer(address, port);
        } else if (args.length == 1) {
            String cmd = args[0];
            if ("--help".equals(cmd)) {
                printCmdTitle();
                return;
            } else {
                try {
                    port = Integer.parseInt(cmd);
                } catch (Exception e) {
                    LogDog.d("非法的端口号！！！");
                    printCmdTitle();
                    return;
                }
            }
            server = new P2PServer(port);
        } else {
            server = new P2PServer(port);
        }

        NioServerFactory.getFactory().open();
        NioServerFactory.getFactory().addTask(server);

        boolean isExit = false;
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        printExitTitle();

        try {
            do {
                String inputCmd = reader.readLine();
                if ("q".equals(inputCmd) || "exit".equals(inputCmd)) {
                    isExit = true;
                } else if ("clear".equals(inputCmd) || "cls".equals(inputCmd)) {
                    String osName = System.getProperty("os.name").toLowerCase();
                    if ("windows".equals(osName)) {
                        Runtime.getRuntime().exec("cls");
                    } else {
                        Runtime.getRuntime().exec("clear");
                    }
                } else {
                    LogDog.e("输入有误,请重新输入！！！");
                    printExitTitle();
                }
            } while (!isExit);
        } catch (IOException e) {
            e.printStackTrace();
        }

        LogDog.e("正在结束所有的服务线程！！！");
        NioServerFactory.destroy();
        TaskExecutorPoolManager.getInstance().destroyAll();
    }

    private static void printExitTitle() {
        LogDog.d("输入 q or exit 则退出系统!!!");
    }

    private static void printCmdTitle() {
        System.out.println(">============== 帮助");
        System.out.println(">============== jar -jar *.jar [address],[port]");
        System.out.println(">============== address 绑定指定的地址");
        System.out.println(">============== port 绑定指定的服务端口号,默认端口是7878");
    }
}
