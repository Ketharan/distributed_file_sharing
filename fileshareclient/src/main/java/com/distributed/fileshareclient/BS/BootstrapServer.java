package com.distributed.fileshareclient.BS;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BootstrapServer extends Thread{
    private DatagramSocket datagramSocket = null;
    private String client_message;
    private List<Neighbour> nodes = new ArrayList<>();
    private int portNumber;
    private int maxNodesCount;
    private Pattern IpPattern;
    private Matcher Ipmatcher;
    private static final String IPADDRESS_PATTERN =
            "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                    "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                    "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                    "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";



    protected BootstrapServer(int portNumber, int maxNodescount){
        try {
        this.portNumber= portNumber;
        this.maxNodesCount=maxNodescount;
        datagramSocket = new DatagramSocket(portNumber);
        echo("Bootstrap Server created at "+portNumber+". Waiting for incoming data...");
        IpPattern = Pattern.compile(IPADDRESS_PATTERN);
        } catch (SocketException e) {
            System.err.println("SocketException " + e);
        }
    }
    //simple function to echo data to terminal
    public static void echo(String msg)
    {
        System.out.println(msg);
    }

    @Override
    public void run() {
        while(true)
        {
            try
            {
                byte[] buffer = new byte[65536];
                DatagramPacket incoming = new DatagramPacket(buffer, buffer.length);
                datagramSocket.receive(incoming);


                byte[] data = incoming.getData();
                client_message = new String(data, 0, incoming.getLength());

                //echo the details of incoming data - client ip : client port - client message
                echo(incoming.getAddress().getHostAddress() + " : " + incoming.getPort() + " - " + client_message);
                StringTokenizer st_test = new StringTokenizer(client_message, " ");

                if (validateRequest(st_test)){
                    String reply = "0010 ERROR";
                    DatagramPacket dpReply = new DatagramPacket(reply.getBytes() , reply.getBytes().length , incoming.getAddress() , incoming.getPort());
                    datagramSocket.send(dpReply);
                }else{
                    StringTokenizer st = new StringTokenizer(client_message, " ");
                    String length = st.nextToken();
                    String command = st.nextToken();

                    if (command.equals("REG")) {
                        handleREG(st,incoming);

                    } else if (command.equals("UNREG")) {
                        handleUNREG(st,incoming);

                    } else if (command.equals("ECHO")) {
                        for (Neighbour node : nodes) {
                            echo(node.getIp() + " " + node.getPort() + " " + node.getUsername());
                        }
                        String reply = "0012 ECHOK 0";
                        DatagramPacket dpReply = new DatagramPacket(reply.getBytes() , reply.getBytes().length , incoming.getAddress() , incoming.getPort());
                        datagramSocket.send(dpReply);
                    }
                    }
                } catch (IOException e) {
                    System.err.println("IOException " + e);
            }
        }
    }

    private void handleUNREG(StringTokenizer st, DatagramPacket incoming) {
        String ip = st.nextToken();
        int port = Integer.parseInt(st.nextToken());
        String username = st.nextToken();
        String reply="UNROK";
        if (invalidIpPort(ip, port, incoming)) {
            reply += " 9999";
            reply = String.format("%04d", reply.length() + 5) + " " + reply;

            DatagramPacket dpReply = new DatagramPacket(reply.getBytes(), reply.getBytes().length, incoming.getAddress(), incoming.getPort());
            try {
                datagramSocket.send(dpReply);
            } catch (IOException e) {
                System.err.println("IOException " + e);
            }
        }else {
            boolean inactive=true;
            for (int i = 0; i < nodes.size(); i++) {
                if (nodes.get(i).getPort() == port) {
                    inactive=false;
                    nodes.remove(i);
                    reply = "0012 UNROK 0";
                    DatagramPacket dpReply = new DatagramPacket(reply.getBytes(), reply.getBytes().length, incoming.getAddress(), incoming.getPort());
                    try {
                        datagramSocket.send(dpReply);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            if (inactive){
                reply += " 9999";
                reply = String.format("%04d", reply.length() + 5) + " " + reply;

                DatagramPacket dpReply = new DatagramPacket(reply.getBytes(), reply.getBytes().length, incoming.getAddress(), incoming.getPort());
                try {
                    datagramSocket.send(dpReply);
                } catch (IOException e) {
                    System.err.println("IOException " + e);
                }
            }
        }
    }

    private boolean validateRequest(StringTokenizer st_test) {

        if (st_test.countTokens()>1){
            int length=Integer.parseInt(st_test.nextToken());
            String command=st_test.nextToken();
            if (command.equals("ECHO")){
                return false;
            }else if ((command.equals("REG")) ||(command.equals("UNREG"))){
                if(st_test.countTokens()==3){

                    String ip = st_test.nextToken();
                    int port = Integer.parseInt(st_test.nextToken());
                    String username = st_test.nextToken();
                    if (!(validateIP(ip))){
                        return true;
                    } if((port<0)||(port>65536)){
                        return true;
                    }
                    return username.isEmpty();
                }else {
                    return true;
                }
            }
        }else {
            return true;
        }
        return false;
    }

    private void printNodes(){
        for (Neighbour node :nodes){
            echo("IP : "+node.getIp()+" port : "+node.getPort()+" username : "+node.getUsername());
        }
    }

    public boolean validateIP(final String ip){
        Ipmatcher = IpPattern.matcher(ip);
        return Ipmatcher.matches();
    }

    private void handleREG(StringTokenizer st, DatagramPacket incoming) {
        String reply = "REGOK ";

        String ip = st.nextToken();
        int port = Integer.parseInt(st.nextToken());
        String username = st.nextToken();
        if (invalidIpPort(ip, port, incoming)) {
            reply += "9999";
            reply = String.format("%04d", reply.length() + 5) + " " + reply;

            DatagramPacket dpReply = new DatagramPacket(reply.getBytes(), reply.getBytes().length, incoming.getAddress(), incoming.getPort());
            try {
                datagramSocket.send(dpReply);
            } catch (IOException e) {
                System.err.println("IOException " + e);
            }
        }else{
            if (nodes.size() == 0) {
                reply += "0";
                nodes.add(new Neighbour(ip, port, username));
            } else {
                boolean isOkay = true;
                for (Neighbour node : nodes) {
                    if (node.getPort() == port) {
                        if (node.getUsername().equals(username)) {
                            reply += "9998";
                        } else {
                            reply += "9997";
                        }
                        isOkay = false;
                    }
                }
                if (isOkay) {
                    if (nodes.size() == 1) {
                        reply += "1 " + nodes.get(0).getIp() + " " + nodes.get(0).getPort();
                    } else if (nodes.size() == 2) {
                        reply += "2 " + nodes.get(0).getIp() + " " + nodes.get(0).getPort() + " " + nodes.get(1).getIp() + " " + nodes.get(1).getPort();
                    } else {
                        Random r = new Random();
                        int Low = 0;
                        int High = nodes.size();
                        int random_1 = r.nextInt(High - Low) + Low;
                        int random_2 = r.nextInt(High - Low) + Low;
                        while (random_1 == random_2) {
                            random_2 = r.nextInt(High - Low) + Low;
                        }
                        echo(random_1 + " " + random_2);
                        reply += "2 " + nodes.get(random_1).getIp() + " " + nodes.get(random_1).getPort() + " " + nodes.get(random_2).getIp() + " " + nodes.get(random_2).getPort();
                    }
                    nodes.add(new Neighbour(ip, port, username));
                }
            }

            reply = String.format("%04d", reply.length() + 5) + " " + reply;

            DatagramPacket dpReply = new DatagramPacket(reply.getBytes(), reply.getBytes().length, incoming.getAddress(), incoming.getPort());
            try {
                datagramSocket.send(dpReply);
            } catch (IOException e) {
                System.err.println("IOException " + e);
            }
//            printNodes();
        }
    }

    private boolean invalidIpPort(String ip, int port, DatagramPacket incoming) {

        if ((incoming.getPort()==port)&&(incoming.getAddress().getHostAddress().equals(ip))){
            return true;
        }else{
            return false;
        }
    }

}
