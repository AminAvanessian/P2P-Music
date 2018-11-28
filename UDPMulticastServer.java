import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.net.MulticastSocket;
import java.nio.file.*;
import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.ServerSocket;
import java.io.Console;


/* 
    P2P Protocol
    ------------

    Message Targets
    - ALL (UDP): Message that is intended for all clients on the P2P network
    - DIRECTED (TCP): Message that is intended for a specific client on the P2P Network for downloading song file

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
        System.out.println("Searching for song...");

        if (userMusic.contains(message)) {
            System.out.println("Found song!");

            // play song
            playSong(message + ".mp3");

            // ask user for next song to play
            serveUserRequests();
            
            return;
        }

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
        

        while (true) {
            byte[] buffer = new byte[2048];
            
            DatagramPacket responsePacket = new DatagramPacket(buffer, buffer.length);
            socket.setSoTimeout(5000);  // timeout after 5 seconds
            try {
                socket.receive(responsePacket);
            } catch (SocketTimeoutException ex) {
                break;
            }

            // get the IP of the sender
            String userAddress = responsePacket.getSocketAddress().toString().substring(1);
            String userIP = userAddress.substring(0, userAddress.indexOf(":"));
            
            // check if message received is from current user
            if (!userIP.equals(currentUserIP)) {
                byte[] resData = responsePacket.getData();  // full data in packet
                String responseText = new String(resData, StandardCharsets.UTF_8);
    
                System.out.println("Message received is: " + responseText);

                // check if peer has song
                if (responseText.trim().equals("Confirm")) {
                    // peer has confirmed that they have the song the user is looking for
                    // accept file
                    Socket incomingFileSocket = new Socket(userIP, 4322);
                    byte[] contents = new byte[10000];

                    // initialize the FileOutputStream to the output file's full path.
                    FileOutputStream fos = new FileOutputStream("Music/" + message + ".mp3");
                    BufferedOutputStream bos = new BufferedOutputStream(fos);
                    InputStream is = incomingFileSocket.getInputStream();

                    // read file bytes
                    int bytesRead = 0;
                    while((bytesRead = is.read(contents)) != -1) {
                        bos.write(contents, 0, bytesRead); 
                    }
                    bos.flush(); 
                    incomingFileSocket.close(); 
                    bos.close();
                    fos.close();

                    System.out.println("File saved successfully!"); 

                    // add song to local db
                    userMusic.add(message);

                    // play song
                    playSong(message + ".mp3");
                }
            }

            if (userIP == "") {
                break;
            }
        }
        System.out.println("song not found");
        socket.close();
        serveUserRequests();
    }

    public void receiveUDPMessage(String ip, int port) throws IOException {
        byte[] buffer = new byte[2048];
        MulticastSocket socket = new MulticastSocket(4321);
        InetAddress group = InetAddress.getByName(ipAddress);
        socket.joinGroup(group);
        
        while (true) {
            buffer = null;
            buffer = new byte[2048];

            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            socket.receive(packet);

            // print the IP of the sender
            String userAddress = packet.getSocketAddress().toString().substring(1);
            String userIP = userAddress.substring(0, userAddress.indexOf(":"));

            String address = InetAddress.getLocalHost().toString();
            String myIP = address.substring(address.indexOf("/")+1, address.length());

            if (!userIP.equals(myIP)) {
                byte[] data = packet.getData();  // full data in packet

                // song name that the peer is looking for
                String msg = new String(data, StandardCharsets.UTF_8);

                System.out.println("Message is: " + msg);

                // let peer know that we have the song the peer is looking for
                if (userMusic.contains(msg.trim())) {
                    String myMessage = "Confirm";
                    byte[] myBytes = myMessage.getBytes();
                    DatagramPacket requestPacket = new DatagramPacket(myBytes, myBytes.length, group, port);
                    socket.send(requestPacket);

                    ServerSocket ssock = new ServerSocket(4322);
                    Socket mySocket = ssock.accept();
                    System.out.println("server socket opened..." + msg);

                    //Specify the file
                    File musicDir = new File("Music");
                    File file = new File(musicDir, msg.trim() + ".mp3");
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
                            size = (int) (fileLength - current); 
                            current = fileLength;
                        } 
                        contents1 = new byte[size]; 
                        bis.read(contents1, 0, size); 
                        os.write(contents1);
                        System.out.print("Sending file ... " + (current*100) / fileLength + "% complete!");
                    }   
                    
                    os.flush();
                    bis.close();
                    // file transfer done, close the socket connection
                    mySocket.close();
                    ssock.close();
                }
            }

            if ("OK".equals("no")) {
                break;
            }
        }
        socket.leaveGroup(group);
        socket.close();
    }

    public static void getAllMusic() {
        File[] files = new File("Music").listFiles();

        for (File file : files) {
            if (file.isDirectory()) {
                // is directory
            } else {
                String ext = file.getName().substring(file.getName().length()-3, file.getName().length());
                String fileName = file.getName().substring(0, file.getName().length()-4);

                if (ext.equals("mp3")) {
                    // save mp3 file to hashset
                    userMusic.add(fileName);
                    System.out.println(fileName);
                }
            }
        }
    }

    public static void playSong(String songName) {

    }

    public static void serveUserRequests() {
        System.out.print("Enter song name: ");
        Console console = System.console();
        String songName = console.readLine();
        
        try {
            searchForSongOnNetwork(songName, ipAddress, 4321);
        } catch (Exception ex) {
            System.out.println(ex.toString());
        }
    }

    public static void main(String[] args) throws IOException {
        // get list of user's songs in directory
        getAllMusic();

        // start listening for requests on the network on a new thread
        Thread t = new Thread(new UDPMulticastServer());
        t.start();

        // listen to user's commands
        serveUserRequests();
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