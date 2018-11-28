import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.net.MulticastSocket;
import java.nio.file.*;
import java.io.*;
import java.net.Socket;
import java.net.ServerSocket;

/* 
    P2P Protocol
    ------------

    Client Methods
    - requestSong(): request .mp3 file from clients to see who has the song
    - confirmSong(): let requestor know that client has the song they are looking for
    - receiveSong(): receive .mp3 file from client (sender)
    - sendSong(): send .mp3 file to requestor  
    - songNotFound(): let requestor know that client no longer has the .mp3 file
    - getLocalSongs(): get local .mp3 files owned by the client on start-up
    - receiveUDPMessage(): receive UDP message from other client(s)
    - playSong(): play .mp3 song for user

    Message Targets
    - ALL (0): Message that is intended for all clients on the P2P network
    - DIRECTED (1): Message that is intended for a specific client on the P2P Network

    Message Types
    - Request
    - Confirm
    - Send
    - Receive
    - NotFound
*/

public class UDPMulticastServer implements Runnable {
    // broadcast IP address
    static String ipAddress = "230.0.0.0";

    // collection of songs owned by the user
    static HashSet<String> userMusic = new HashSet<String>();


    public static void searchForSongOnNetwork(String message, String ipAddress, int port) throws IOException {
        // ask who has the song
        MulticastSocket socket = new MulticastSocket(4321);
        InetAddress group = InetAddress.getByName(ipAddress);

        // message byte array
        byte[] msg = message.getBytes(); 

        DatagramPacket requestPacket = new DatagramPacket(msg, msg.length, group, port);
        socket.joinGroup(group);
        socket.send(requestPacket);

        // get current user's IP address
        String currentAddress = InetAddress.getLocalHost().toString();
        String currentUserIP = currentAddress.substring(currentAddress.indexOf("/")+1, currentAddress.length());

        // get response from network
        byte[] buffer = new byte[2048];

        while (true) {
            DatagramPacket responsePacket = new DatagramPacket(buffer, buffer.length);
            socket.receive(responsePacket);

            // get the IP of the sender
            String userAddress = responsePacket.getSocketAddress().toString().substring(1);
            String userIP = userAddress.substring(0, userAddress.indexOf(":"));
            
            // check if message received is from current user
            if (!userIP.equals(currentUserIP)) {
                byte[] resData = responsePacket.getData();  // full data in packet
                String responseText = new String(resData, StandardCharsets.UTF_8);
    
                System.out.println("Packet received from: " + userIP);
                System.out.println("Message received is: " + responseText);
                System.out.println();

                if (responseText.equals("Confirm")) {
                    // peer has confirmed that they have the song the user is looking for
                    // accept file
                    Socket incomingFileSocket = new Socket(userIP, 4322);
                    byte[] contents = new byte[10000];

                    // initialize the FileOutputStream to the output file's full path.
                    FileOutputStream fos = new FileOutputStream("message.mp3");
                    BufferedOutputStream bos = new BufferedOutputStream(fos);
                    InputStream is = incomingFileSocket.getInputStream();

                    // no of bytes read in one read() call
                    int bytesRead = 0; 

                    while((bytesRead = is.read(contents)) != -1) {
                        bos.write(contents, 0, bytesRead); 
                    }

                    bos.flush(); 
                    incomingFileSocket.close(); 

                    System.out.println("File saved successfully!"); 
                }
            }

            if (userIP == "123") {
                break;
            }
        }

        socket.close();

        /*
        //Initialize Sockets
        ServerSocket ssock = new ServerSocket(4322);
        Socket mySocket = ssock.accept();
        System.out.println("server socket opened...");

        //Specify the file
        File file = new File("music.mp3");
        FileInputStream fis = new FileInputStream(file);
        BufferedInputStream bis = new BufferedInputStream(fis); 
          
        //Get socket's output stream
        OutputStream os = mySocket.getOutputStream();
                
        //Read File Contents into contents array 
        byte[] contents1;
        long fileLength = file.length(); 
        long current = 0;
         
        long start = System.nanoTime();
        
        while(current != fileLength) { 
            int size = 10000;
            if (fileLength - current >= size)
                current += size;    
            else { 
                size = (int)(fileLength - current); 
                current = fileLength;
            } 
            contents1 = new byte[size]; 
            bis.read(contents1, 0, size); 
            os.write(contents1);
            System.out.print("Sending file ... "+(current*100)/fileLength+"% complete!");
        }   
        
        // os.flush(); 
        // //File transfer done. Close the socket connection!
        // mySocket.close();
        // ssock.close();
        // System.out.println("File sent succesfully!");
*/

        
    }

    public void receiveUDPMessage(String ip, int port) throws IOException {

    }

    public static void main(String[] args) throws IOException {
        // get list of user's songs in directory

        // start listening for requests on the network
        Thread t = new Thread(new UDPMulticastClient());
        t.start();

        // listen to user's commands

        searchForSongOnNetwork("keke - 6ix9ine", ipAddress, 4321);
    }

    public static void getAllMusic() {

    }

    @Override
    public void run() {
       try {
          receiveUDPMessage(ipAddress, 4321);
       } catch (IOException ex) {
          ex.printStackTrace();
       }
    }
}