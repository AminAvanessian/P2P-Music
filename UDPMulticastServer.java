import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.*;


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
    - ALL: Message that is intended for all clients on the P2P network
    - DIRECTED: Message that is intended for a specific client on the P2P Network

    Message Types
    - Request
    - Confirm
    - Send
    - Receive
    - NotFound
*/

public class UDPMulticastServer {
    static String ipAddress = "230.0.0.0";

    public static void sendUDPMessage(String message, String ipAddress, int port) throws IOException {
        DatagramSocket socket = new DatagramSocket();
        InetAddress group = InetAddress.getByName(ipAddress);
        
        byte[] target = "10".getBytes();    // target & type
        byte[] msg = message.getBytes();    // message

        // full data to send
        byte[] fullData = new byte[target.length + msg.length];
        System.arraycopy(target, 0, fullData, 0, target.length);
        System.arraycopy(msg, 0, fullData, target.length, msg.length);

      //  DatagramPacket packet = new DatagramPacket(msg, msg.length, group, port);
        DatagramPacket packet = new DatagramPacket(fullData, fullData.length, group, port);

        // Message message1 = new Message(Integer.parseInt(strType), strTarget, fullMsg, ipAddress);
        // System.out.println("Type is: " + message1.msgType);

        // sample message
    //    Message message1 = new Message(Message.MESSAGE_TYPE_REQUEST, "ALL", msg, ipAddress);
        socket.send(packet);
        socket.close();
    }

    public static void main(String[] args) throws IOException {
        sendUDPMessage("This is a multicast messge", ipAddress, 4321);
        sendUDPMessage("This is the second multicast messge", ipAddress, 4321);
        sendUDPMessage("Whats up man", ipAddress, 4321);
        sendUDPMessage("shoot it", ipAddress, 4321);
    }
}