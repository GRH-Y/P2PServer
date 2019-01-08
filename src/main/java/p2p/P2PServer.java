package p2p;


import connect.network.nio.*;
import json.JsonUtils;
import p2p.bean.AddressBean;
import p2p.bean.RegBean;
import util.IoUtils;
import util.LogDog;
import util.NetUtils;
import util.StringUtils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * P2PServer p2p服务端
 * Created by prolog on 10/25/2016.
 */

public class P2PServer extends NioServerTask {

    private Map<String, NioClientTask> nioClientTaskMap;

    public P2PServer(int port) {
        this(NetUtils.getLocalIp("wlan"), port);
    }

//    public P2PServer(String netInterface, int port) {
//        setAddress(NetUtils.getLocalIp(netInterface), port);
//        nioClientTaskMap = new ConcurrentHashMap<>();
//    }

    public P2PServer(String address, int port) {
        setAddress(address, port);
        nioClientTaskMap = new ConcurrentHashMap<>();
    }


    @Override
    protected void onAcceptServerChannel(SocketChannel channel) {
        LogDog.e("has client connect P2PServer ======>");
        Client client = new Client(channel);
        NioClientFactory clientFactory = NioClientFactory.getFactory();
        clientFactory.open();
        clientFactory.addTask(client);
    }

    @Override
    protected void onOpenServerChannel(boolean isSuccess) {
        LogDog.i("==> P2PServer onConnect = " + isSuccess);
        LogDog.d("==> P2PServer address = " + getServerHost() + ":" + getServerPort());
    }

    @Override
    protected void onCloseServerChannel() {
        LogDog.e("==> P2PServer 正在结束 ！！！");
    }

    private class Client extends NioClientTask {
        private String key;

        public Client(SocketChannel channel) {
            super(channel);
            NioSender sender = new NioSender();
            setSender(sender);
            ClientReceive receive = new ClientReceive();
            setReceive(receive);
        }

        @Override
        protected void onConnectSocketChannel(boolean isConnect) {
            LogDog.e("==> NioServer Client onConnect = " + isConnect);
        }

        @Override
        protected void onCloseSocketChannel() {
            LogDog.e("============================================= ");
            LogDog.e("==> Client onCloseSocketChannel !!! ");
            LogDog.e("============================================= ");
            if (StringUtils.isNotEmpty(key)) {
                nioClientTaskMap.remove(key);
            }
        }

        private class ClientReceive extends NioReceive {

            private void sendAddressInfo(SocketChannel channel, NioSender sender) {
                try {
                    AddressBean addressBean = new AddressBean();
                    InetSocketAddress address = (InetSocketAddress) channel.getRemoteAddress();
                    addressBean.setIp(address.getAddress().getHostAddress());
                    addressBean.setPort(address.getPort());
                    sender.sendData(JsonUtils.toJson(addressBean).getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }


            @Override
            protected void onRead(SocketChannel channel) throws Exception {
                byte[] data = IoUtils.tryRead(channel);
                if (data != null) {
                    String json = new String(data);
                    LogDog.d("==> ClientReceive data = " + json);
                    RegBean keyBean = JsonUtils.toEntity(RegBean.class, json);
                    if (nioClientTaskMap.containsKey(keyBean.getKey())) {
                        NioClientTask task = nioClientTaskMap.get(keyBean.getKey());
                        NioSender otherSender = task.getSender();
                        sendAddressInfo(channel, otherSender);
                        SocketChannel otherChannel = task.getSocketChannel();
                        sendAddressInfo(otherChannel, getSender());
                    } else {
                        nioClientTaskMap.put(keyBean.getKey(), Client.this);
                        key = keyBean.getKey();
                    }
                }
            }

        }

    }
}
