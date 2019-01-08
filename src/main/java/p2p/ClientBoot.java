package p2p;

import connect.network.nio.NioClientFactory;
import connect.network.nio.NioClientTask;
import connect.network.nio.NioReceive;
import connect.network.nio.NioSender;
import json.JsonUtils;
import p2p.bean.AddressBean;
import p2p.bean.RegBean;
import util.LogDog;

import java.io.IOException;
import java.net.InetSocketAddress;

public class ClientBoot {

    private static final String KEY = "ClientBoot";

    public ClientBoot() {
        ClientP2p clientTask = new ClientP2p("10.1.140.128", 7878);
//        clientTask.setAddress("47.93.51.111", 7878);
        NioClientFactory.getFactory().open();
        NioClientFactory.getFactory().addTask(clientTask);
        LogDog.d("==> 正在链接服务端....");

        RegBean regBean = new RegBean();
        regBean.setKey(KEY);
        clientTask.getSender().sendData(JsonUtils.toJson(regBean).getBytes());
        LogDog.d("==> 发送key ....");
    }


    class ClientP2p extends NioClientTask {

        private InetSocketAddress localAddress = null;
        private AddressBean addressBean = null;

        ClientP2p(String host, int port) {
            setAddress(host, port);
            setSender(new NioSender());
            setReceive(new NioReceive(this, "onP2PReceive"));
        }

        private void onP2PReceive(byte[] data) {
            try {
                localAddress = (InetSocketAddress) getSocketChannel().getLocalAddress();
                LogDog.d("==> 当前链接信息 ：" + localAddress.getAddress().getHostAddress() + ":" + localAddress.getPort());
            } catch (IOException e) {
                e.printStackTrace();
            }

            String json = new String(data);
            LogDog.d("==> 接收到服务器返回的数据 ：" + json);

            addressBean = JsonUtils.toEntity(AddressBean.class, json);
//            onRecovery();
            NioClientFactory.getFactory().removeTask(this);
        }

        @Override
        protected void onCloseSocketChannel() {
            LogDog.e("==> 开始断开p2p 服务端链接 ！！！");
        }

        @Override
        protected void onRecovery() {
            if (addressBean != null) {
                LogDog.e("==> 开始p2p点对点链接 ！！！");
                P2pClient p2pTask = new P2pClient(addressBean.getIp(), addressBean.getPort(), localAddress.getPort());
                NioSender sender = new NioSender();
                p2pTask.setSender(sender);
                p2pTask.setReceive(new NioReceive(this, "onConnectP2PReceive"));
                p2pTask.setAddress(addressBean.getIp(), addressBean.getPort());
                NioClientFactory.getFactory().addTask(p2pTask);
                sender.sendData(("hello 我是" + System.currentTimeMillis()).getBytes());
            }
        }

        private void onConnectP2PReceive(byte[] data) {
            String json = new String(data);
            LogDog.d("==> 接收p2p的数据 ：" + json);
        }

    }


    public static void main(String[] args) {
        new ClientBoot();
//        try {
//            Thread.sleep(15000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        new ClientBoot();
    }

}
