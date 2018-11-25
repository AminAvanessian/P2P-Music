import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.nio.charset.StandardCharsets;
import java.util.*;


public class UDPMulticastClient implements Runnable {
   String ipAddress = "230.0.0.0";

   public static void main(String[] args) {
      Thread t = new Thread(new UDPMulticastClient());
      t.start();
      System.out.println("Search for song...");
   }

   public void receiveUDPMessage(String ip, int port) throws IOException {
      byte[] buffer = new byte[2048];
      MulticastSocket socket = new MulticastSocket(4321);
      InetAddress group = InetAddress.getByName(ipAddress);
      socket.joinGroup(group);
      
      while (true) {
         System.out.println("Waiting for multicast message...");

         DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
         socket.receive(packet);

         // String msg1 = new String(packet.getData(), packet.getOffset(), packet.getLength());
         // System.out.println(msg1);

         // print the IP of the sender
         String userAddress = packet.getSocketAddress().toString().substring(1);
         String userIP = userAddress.substring(0, userAddress.indexOf(":"));

         byte[] data = packet.getData();  // full data in packet
         byte[] targetBytes = Arrays.copyOfRange(data, 0, 1);  // target of message
         byte[] msgTypeBytes = Arrays.copyOfRange(data, 1, 2); // type of message
         byte[] fullMsg = Arrays.copyOfRange(data, 2, packet.getLength()); // message itself

         String strTarget = new String(targetBytes, StandardCharsets.UTF_8);
         String strType = new String(msgTypeBytes, StandardCharsets.UTF_8);
         String msg = new String(fullMsg, StandardCharsets.UTF_8);

         System.out.println("Target is: " + strTarget);
         System.out.println("Type is: " + strType);
         System.out.println("Message is: " + msg.trim());
         System.out.println();

         // System.out.println("Packet sender address: " + userIP);
         // System.out.println("[Multicast UDP message received] >> " + msg);

         if ("OK".equals(msg)) {
            System.out.println("No more message. Exiting : " + msg);
            break;
         }
      }
      socket.leaveGroup(group);
      socket.close();
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