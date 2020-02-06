package com.distributed.fileshareclient.BS;

import java.util.Scanner;

public class BsServerApplication {

    public static void main(String args[]){
        Scanner scanner = new Scanner(System.in);
        System.out.println("Welcome to Bootstrap Server initiative. Please enter the proper port to start the server...");
        String portnumber = scanner.nextLine();
        while (!(validatePortNumber(portnumber))){
            System.out.println("You entered the wrong port number. Please enter the proper port to start the server...");
            portnumber = scanner.nextLine();

        }
        System.out.println("Please enter maximum number of nodes (1-15)...");
        String numOfNodes = scanner.nextLine();
        while (!(validateNumOfNodes(numOfNodes))){
            System.out.println("You entered the wrong input. Please enter maximum number of nodes (1-15)...");
            numOfNodes = scanner.nextLine();

        }
        BootstrapServer bootstrapServer =new BootstrapServer(Integer.parseInt(portnumber),Integer.parseInt(numOfNodes));
        bootstrapServer.start();
    }

    private static boolean validateNumOfNodes(String numOfNodes) {
        try {
            int portNumber = Integer.parseInt(numOfNodes);
            return (portNumber > 0) && (portNumber < 15);
        }catch (Exception e){
            return false;
        }
    }

    private static boolean validatePortNumber(String portnumber) {
        try {
            int portNumber = Integer.parseInt(portnumber);
            return (portNumber > 1023) && (portNumber < 65530);
        }catch (Exception e){
            return false;
        }

    }
}
