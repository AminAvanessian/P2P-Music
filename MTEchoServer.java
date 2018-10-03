import java.net.*;
import java.util.HashMap;
import java.io.*;
import java.util.*;

 
public class MTEchoServer {

    public static void main(String[] args) {
         
        if (args.length != 1) {
            System.err.println("Usage: java EchoServer <port number>");
            System.exit(1);
        }
         
        int portNumber = Integer.parseInt(args[0]);
        MTEchoServer es = new MTEchoServer();
        es.startServer(portNumber);
     }

     public void startServer(int portNumber) {
        try {
            ServerSocket serverSocket = new ServerSocket(portNumber);
            while(true) {
               Socket client = serverSocket.accept();
               Connection cc = new Connection(client);
            }
        } catch(Exception e) {
           System.out.println("Exception: " + e);
        }
    }
}

class BankServer {
    // user ip and amount of $
    public static Map<String,Integer> usersData = new HashMap<String,Integer>();  

    // protocol commands
    public static final String DEPOSIT_MONEY = "DEPOSIT";
    public static final String TRANSFER_MONEY = "TRANSFER";
    public static final String CHECK_BALANCE = "CHECK";

    // transfer money from sender to receiver
    public static void transferMoney(String sender, String receiver, Integer amount) {
        // subtract money from sender

        // add money to receiver

    }

    // deposit money 
    public static void depositMoney(String client, int amount) {
        // get users bank balance
        int balance = BankServer.usersData.get(client);

        // add money to users account
        usersData.put(client, balance + amount);
    }
}

class Connection extends Thread {
    Socket client;
    PrintWriter out;
    BufferedReader in;

    public Connection(Socket s) { // constructor
       client = s;

       try {
           out = new PrintWriter(client.getOutputStream(), true);                   
           in = new BufferedReader(new InputStreamReader(client.getInputStream()));
       } catch (IOException e) {
           try {
             client.close();
           } catch (IOException ex) {
             System.out.println("Error while getting socket streams.." + ex);
           }
           return;
       }
        this.start(); // Thread starts here...this start() will call run()
    }
 
    public void run() {
      try {
         String inputLine;
         String clientSocketAddress = client.getRemoteSocketAddress().toString();
         String clientAddress = clientSocketAddress.substring(1);

         // add user data to map
         BankServer.usersData.put(clientAddress, 5000);

         while ((inputLine = in.readLine()) != null) {
                System.out.println("Received from: " + clientAddress + " Input: " + inputLine);
                String[] userCommands = inputLine.split(" ");
                String firstCommand = userCommands[0];

                String returnText = "Please choose a valid request: DEPOSIT | CHECK | TRANSFER";

                switch(firstCommand) {
                    case BankServer.DEPOSIT_MONEY:
                        // get transfer ammount
                        int amount = Integer.parseInt(userCommands[1]);

                        // add money to users account 
                        BankServer.depositMoney(clientAddress, amount);

                        // set return text to user
                        returnText = "$" + amount + " was deposited into your account";
                        break;

                    case BankServer.CHECK_BALANCE:
                        System.out.println("YOU CHECK BALANCE!");
                        
                        break;
                    
                    case BankServer.TRANSFER_MONEY: 
                        System.out.println("YOU TRANSFER MONEY");
                    
                        break;
                }
                out.println(returnText);

            for (Map.Entry m:BankServer.usersData.entrySet()) {  
                System.out.println(m.getKey()+" "+m.getValue());  
            }  
         }
         client.close();
       } catch (IOException e) {
           System.out.println("Exception caught...");
           System.out.println(e.getMessage());
       }
    }
}
