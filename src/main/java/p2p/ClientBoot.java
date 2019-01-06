package p2p;

import connect.network.nio.NioClientFactory;
import connect.network.nio.NioClientTask;
import connect.network.nio.NioReceive;
import connect.network.nio.NioSender;
import json.JsonUtils;
import p2p.bean.KeyBean;
import util.LogDog;

public class ClientBoot {

    private static final String KEY = "ClientBoot";

    public ClientBoot() {
        NioClientTask clientTask = new NioClientTask();
        clientTask.setSender(new NioSender());
        clientTask.setReceive(new NioReceive(this, "onP2PReceive"));
        clientTask.setAddress("47.93.51.111", 7878);
        NioClientFactory.getFactory().open();
        NioClientFactory.getFactory().addTask(clientTask);

        KeyBean keyBean = new KeyBean();
        keyBean.setKey(KEY);
        clientTask.getSender().sendData(JsonUtils.toJson(keyBean).getBytes());
    }

    private void onP2PReceive(byte[] data) {
        String json = new String(data);
        LogDog.d("==> 接收到服务器返回的数据 ：" + json);
//        AddressBean addressBean = JsonUtils.toEntity(AddressBean.class, json);
    }


    public static void main(String[] args) {
        new ClientBoot();
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        new ClientBoot();
    }

}
