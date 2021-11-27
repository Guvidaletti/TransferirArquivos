package Socket;

import java.net.DatagramPacket;
import java.net.InetAddress;

public class Client {
  public static void sendTo(InetAddress address, int port, byte[] byteArr) {
    try {
      DatagramPacket packet = new DatagramPacket(byteArr, byteArr.length, address, port);
      Server.socket.send(packet);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
