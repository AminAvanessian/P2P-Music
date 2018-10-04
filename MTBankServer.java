import java.net.*;
import java.util.HashMap;
import java.io.*;
import java.util.*;
import java.text.SimpleDateFormat;

 
public class MTBankServer {
    public static void main(String[] args) {
         
        if (args.length != 1) {
            System.err.println("Usage: java MTBankServer <port number>");
            System.exit(1);
        }
         
        int portNumber = Integer.parseInt(args[0]);
        MTBankServer es = new MTBankServer();

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

    // client transaction data
    public static Map<String,List<String>> clientTransactions = new HashMap<String,List<String>>();

    // protocol commands
    public static final String DEPOSIT_MONEY = "DEPOSIT";
    public static final String TRANSFER_MONEY = "TRANSFER";
    public static final String CHECK_BALANCE = "CHECK";
    public static final String WITHDRAW_MONEY = "WITHDRAW";
    public static final String TRANSACTIONS = "TRANSACTIONS";

    // transfer money from sender to receiver
    public static void transferMoney(String sender, String receiver, Integer amount) {
        // get users bank balance
        int balance = BankServer.usersData.get(sender);

        // get receivers balance
        int balance2 = BankServer.usersData.get(receiver);

        // subtract money from sender
        usersData.put(sender, balance - amount);

        // add money to receiver
        usersData.put(receiver, balance2 + amount);
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
             System.out.println("Got an error getting socket streams.." + ex);
           }
           return;
       }
        this.start();
    }
 
    public void run() {
      try {
         String inputLine;
         String clientSocketAddress = client.getRemoteSocketAddress().toString();
         String clientAddress = clientSocketAddress.substring(1);

         // add user data to map
         BankServer.usersData.put(clientAddress, 0);

         Date dNow = new Date();
         SimpleDateFormat ft = new SimpleDateFormat ("E yyyy.MM.dd 'at' hh:mm:ss a zzz");

         // original transaction message
         List original = new ArrayList();
         original.add(0, ft.format(dNow) + " - " + "You have opened an account");

         // add first transaction for client
         BankServer.clientTransactions.put(clientAddress, original);

         // server loop
         while ((inputLine = in.readLine()) != null) {
                System.out.println("client: " + clientAddress + " Message: " + inputLine);
                String[] userCommands = inputLine.split(" ");
                String firstCommand = userCommands[0];

                // text to return to client
                String returnText = "Please choose a valid request:~DEPOSIT | WITHDRAW | CHECK | TRANSFER | TRANSACTIONS";

                // get users balance
                int usersBalance = BankServer.usersData.get(clientAddress);

                switch(firstCommand) {
                    // deposit money
                    case BankServer.DEPOSIT_MONEY:
                        Date dNow1 = new Date();

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

                        // add transaction to client transaction records
                        List newTransactions = BankServer.clientTransactions.get(clientAddress);
                        newTransactions.add(0, ft.format(dNow1) + " - " + returnText);
                        BankServer.clientTransactions.put(clientAddress, newTransactions);

                        break;

                    // withdraw money
                    case BankServer.WITHDRAW_MONEY:
                        Date dNow2 = new Date();

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

                            // add transaction to client transaction records
                            List newTransactions2 = BankServer.clientTransactions.get(clientAddress);
                            newTransactions2.add(0, ft.format(dNow2) + " - " + returnText);
                            BankServer.clientTransactions.put(clientAddress, newTransactions2);
                        }

                        break;

                    case BankServer.CHECK_BALANCE:
                        // set return text to user
                        returnText = "Your account balance is $" + usersBalance;
                        
                        break;
                    
                    case BankServer.TRANSFER_MONEY: 
                        Date dNow3 = new Date();

                        if (userCommands.length < 4) {
                            returnText = "Not enough arguments for money transfer.~Format: TRANSFER # TO IP:SOCKET#";
                            break;
                        }

                        // get transfer ammount
                        Integer transferAmount = tryParse(userCommands[1]);
                        String transferReceiver = userCommands[3];

                        if (transferAmount == null || transferReceiver == "") {
                            returnText = "Incorrect format for money transfer.~Format: TRANSFER $$$ TO IP:SOCKET#";
                            break;
                        }

                        // check if receiving user exists
                        Integer receiver = BankServer.usersData.get(transferReceiver);

                        if (receiver == null) {
                            returnText = "Receiver not found, please try a different user";
                            break;
                        }

                        if (transferReceiver == clientAddress) {
                            // client is trying to transfer money to themselves which is not allowed
                            returnText = "You cannot transfer money to yourself";
                            break;
                        }

                        if (transferAmount > usersBalance) {
                            // client does not have enough money to transfer
                            returnText = "Insufficient funds for transfer, please choose lower amount";
                            break;
                        }
                        // transfer money from client to receiver
                        BankServer.transferMoney(clientAddress, transferReceiver, transferAmount);

                        returnText = "$" + transferAmount + " transferred to " + transferReceiver + " successfully";

                        String msgToSender = "$" + transferAmount + " was transferred to " + transferReceiver;
                        String msgToReceiver = transferReceiver + " transferred $" + transferAmount + " to your account";

                        // add transaction to sender transaction records
                        List newTransactions3 = BankServer.clientTransactions.get(clientAddress);
                        newTransactions3.add(0, ft.format(dNow3) + " - " + msgToSender);
                        BankServer.clientTransactions.put(clientAddress, newTransactions3);

                        // add transaction to receiver transaction records
                        List newTransactions4 = BankServer.clientTransactions.get(transferReceiver);
                        newTransactions4.add(0, ft.format(dNow3) + " - " + msgToReceiver);
                        BankServer.clientTransactions.put(transferReceiver, newTransactions4);

                        break;
                    
                    case BankServer.TRANSACTIONS:
                        List<String> clientTransactions = BankServer.clientTransactions.get(clientAddress);

                        returnText = "";

                        for (String trans : clientTransactions) {
                            returnText = returnText + trans + "~";
                        }

                        break;
                }
                out.println(returnText);

            for (Map.Entry m : BankServer.usersData.entrySet()) {  
                System.out.println("User " + m.getKey() + " has $" + m.getValue());
            }
         }
         client.close();

       } catch (IOException e) {
           System.out.println("Exception has been caught...");
           System.out.println(e.getMessage());
       }
    }

    // try converting string to integer
    public static Integer tryParse(String text) {
        try {
          return Integer.parseInt(text);
        } catch (NumberFormatException e) {
          return null;
        }
    }
}