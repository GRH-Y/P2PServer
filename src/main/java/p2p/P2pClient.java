package p2p;


import connect.network.nio.NioClientFactory;
import connect.network.nio.NioClientTask;
import connect.network.nio.NioReceive;
import connect.network.nio.NioSender;
import json.JsonUtils;
import p2p.bean.AddressBean;
import util.LogDog;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

/**
 * P2P客户端
 * Created by prolog on 11/23/2016.
 */

public class P2pClient extends NioClientTask {

    private int localPort;

    public P2pClient(String ip, int port, int localPort) {
        setAddress(ip, port);
        this.localPort = localPort;
        setSender(new NioSender());
        setReceive(new NioReceive(this, "onReceiveData"));
    }

    @Override
    protected void onConfigSocket(SocketChannel socket) {
        try {
            socket.bind(new InetSocketAddress(getHost(), localPort));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onConnectSocketChannel(boolean isConnect) {
        LogDog.i("==> P2pClient onConnect = " + isConnect);
    }

    private void onReceiveData(byte[] data) {
        String json = new String(data);
        LogDog.d("==> NioReceive data = " + json);
        if (json.contains("ip")) {
            AddressBean addressBean = JsonUtils.toEntity(AddressBean.class, json);
            ConnectTask task = new ConnectTask(addressBean);
            NioClientFactory clientFactory = NioClientFactory.getFactory();
            clientFactory.addTask(task);

        }
    }

}
