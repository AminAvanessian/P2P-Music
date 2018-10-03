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
    public static final String WITHDRAW_MONEY = "WITHDRAW";

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

    // withdraw money
    public static void withdrawMoney(String client, int amount) {
        // get users bank balance
        int balance = BankServer.usersData.get(client);

        // add money to users account
        usersData.put(client, balance - amount);
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
         BankServer.usersData.put(clientAddress, 0);

         while ((inputLine = in.readLine()) != null) {
                System.out.println("Received from: " + clientAddress + " Input: " + inputLine);
                String[] userCommands = inputLine.split(" ");
                String firstCommand = userCommands[0];

                // text to return to client
                String returnText = "Please choose a valid request: DEPOSIT | WITHDRAW | CHECK | TRANSFER";

                // get users balance
                int usersBalance = BankServer.usersData.get(clientAddress);

                switch(firstCommand) {
                    // deposit money
                    case BankServer.DEPOSIT_MONEY:
                        // get transfer ammount
                        Integer amountDEP = tryParse(userCommands[1]);

                        if (amountDEP == null) {
                            // set return text to user
                            returnText = "Please choose a valid value for deposit";
                            break;
                        }

                        // add money to users account 
                        BankServer.depositMoney(clientAddress, amountDEP);

                        // set return text to user
                        returnText = "$" + amountDEP + " was deposited into your account";
                        break;

                    // withdraw money
                    case BankServer.WITHDRAW_MONEY:
                        // get transfer ammount
                        Integer amountWITH = tryParse(userCommands[1]);
                            
                        if (amountWITH == null) {
                            returnText = "Please choose a valid value for withdrawl";
                            break;
                        }

                        // check if user has enough money for withdrawl
                        if (amountWITH > usersBalance) {
                            // user does not have sufficient money for withdrawl
                            returnText = "Insufficient funds for withdrawl, please choose a smaller amount";
                        } else {
                            BankServer.withdrawMoney(clientAddress, amountWITH);

                            // set return text to user
                            returnText = "$" + amountWITH + " was withdrawn from your account";
                        }

                        break;

                    case BankServer.CHECK_BALANCE:
                        // set return text to user
                        returnText = "Your account balance is $" + usersBalance;
                        
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

    public static Integer tryParse(String text) {
        try {
          return Integer.parseInt(text);
        } catch (NumberFormatException e) {
          return null;
        }
    }
}