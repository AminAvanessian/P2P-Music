import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

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
    static String ipAddress = "239.0.0.0";

    public static void sendUDPMessage(String message, String ipAddress, int port) throws IOException {
        DatagramSocket socket = new DatagramSocket();
        InetAddress group = InetAddress.getByName(ipAddress);
        byte[] msg = message.getBytes();
        DatagramPacket packet = new DatagramPacket(msg, msg.length, group, port);

        socket.send(packet);
        socket.close();
    }

    public static void main(String[] args) throws IOException {
        sendUDPMessage("This is a multicast messge", ipAddress, 4321);
        sendUDPMessage("This is the second multicast messge", ipAddress, 4321);
        sendUDPMessage("OKKK", ipAddress, 4321);
    }
}