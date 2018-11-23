import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class UDPMulticastClient implements Runnable {
   String ipAddress = "239.0.0.0";

   public static void main(String[] args) {
      Thread t = new Thread(new UDPMulticastClient());
      t.start();
      System.out.println("Search for song...");
   }

   public void receiveUDPMessage(String ip, int port) throws IOException {
      byte[] buffer = new byte[1024];
      MulticastSocket socket = new MulticastSocket(4321);
      InetAddress group = InetAddress.getByName(ipAddress);
      socket.joinGroup(group);
      
      while (true) {
         System.out.println("Waiting for multicast message...");

         DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
         socket.receive(packet);
         String msg = new String(packet.getData(), packet.getOffset(), packet.getLength());

         // print the IP of the sender
         String userAddress = packet.getSocketAddress().toString().substring(1);
         String userIP = userAddress.substring(0, userAddress.indexOf(":"));

         System.out.println("Packet sender address: " + userIP);

         System.out.println("[Multicast UDP message received] >> " + msg);

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
      } catch(IOException ex) {
         ex.printStackTrace();
      }
   }
}