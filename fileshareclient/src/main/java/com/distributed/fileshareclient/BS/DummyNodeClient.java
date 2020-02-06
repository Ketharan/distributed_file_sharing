package com.distributed.fileshareclient.BS;

import java.io.IOException;
import java.net.*;

public class DummyNodeClient {
    public DummyNodeClient() {
    }
    public static void  main(String[] args) throws Exception  {

        DatagramSocket ds = null;
        try {
            ds = new DatagramSocket(5000);
        } catch (SocketException e) {
            e.printStackTrace();
        }
        String client_message;
        for(int i=0;i<1;i++){

//            String str = "0036 REG 127.0.0.1 5003 1234abcd";
        String str = "0028 UNREG 127.0.0.1 5003 1234abcd";
            InetAddress ip = null;
            try {
                ip = InetAddress.getByName("127.0.0.1");
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }

            DatagramPacket dp = new DatagramPacket(str.getBytes(), str.length(), ip, 3002);
            try {
                assert ds != null;
                ds.send(dp);
            } catch (IOException e) {
                e.printStackTrace();
            }

            byte[] buffer = new byte[65536];
            DatagramPacket incoming = new DatagramPacket(buffer, buffer.length);
            ds.receive(incoming);


            byte[] data = incoming.getData();
            client_message = new String(data, 0, incoming.getLength());
            echo(incoming.getAddress().getHostAddress() + " : " + incoming.getPort() + " - " + client_message);
        }


    }
    public static void echo(String msg)
    {
        System.out.println(msg);
    }

}
