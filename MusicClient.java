import java.io.*;
import java.net.*;
import java.util.*;
 
public class MusicClient {
    public static void main(String[] args) {
        int minPortNum = 5000;
        int maxPortNum = 5020;

        String hostName = "localhost";
        
        System.out.println("Scanning for clients...");
        for (int portNum = 5000; portNum <= maxPortNum; portNum++) {
            System.out.println("Scanning port: " + portNum);
            try {
               // Socket socket = new Socket(hostName, portNum, null, portNum);
               Socket socket = new Socket(hostName, portNum);
                PrintWriter out =
                    new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in =
                    new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));
                BufferedReader stdIn =
                    new BufferedReader(
                        new InputStreamReader(System.in));

                System.out.print("Connected to peer at: " + socket.getInetAddress() + ":" + portNum + "");
            
                String userInput;
                // while ((userInput = stdIn.readLine()) != null) {
                //     out.println(userInput);

                //     String serverResponse = in.readLine();

                //     System.out.println();
                //     System.out.println("-------------------------");

                //     System.out.println(serverResponse);
                // }
                socket.close();
            } catch (UnknownHostException e) {
                System.err.println("No Available host(s): " + hostName);
            } catch (IOException e) {
                System.err.println("No client at port " + hostName + "\n");
            }
        }

        // start server
        Random r = new Random();
		int portNumber = r.nextInt((maxPortNum - minPortNum) + 1) + minPortNum;
        MusicClient mc = new MusicClient();
        mc.startServer(portNumber);
    }

    public void startServer(int portNumber) {
        try {
            ServerSocket serverSocket = new ServerSocket(portNumber);
            System.out.println("This server is on port: " + portNumber);

            while (true) {
               Socket client = serverSocket.accept();
               Connection cc = new Connection(client);
            }
        } catch(Exception e) {
           System.out.println("Exception: " + e);
        }
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

         System.out.println("Peer client address is: " + clientAddress);
         //client.close();
       } catch (Exception e) {
           System.out.println("Exception has been caught...");
           System.out.println(e.getMessage());
       }
    }

}
