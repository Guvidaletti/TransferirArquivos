package Socket;

import Console.Console;

import java.io.IOException;
import java.net.DatagramPacket;

public class Receiver extends Server {
  @Override
  public void run() {
    try {
      DatagramPacket packet = new DatagramPacket(buf, buf.length);
      socket.receive(packet);
      String received = new String(packet.getData(), 0, packet.getLength()).trim();
      System.out.println("RECEBI" + received);
//        Core.comandoRecebido(received, packet.getPort(), packet.getAddress());
    } catch (IOException e) {
      Console.log("Erro no Receiver!");
    } finally {
      mainWaiting.release();
    }
  }
}
