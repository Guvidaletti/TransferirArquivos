package Socket;

import Console.Console;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class Server extends Thread {
  public enum TIPO {
    ENVIANDO, RECEBENDO;
  }

  public static DatagramSocket socket;
  public static TIPO tipo;
  private byte[] buf = new byte[999];

  static {
    try {
      socket = new DatagramSocket();
    } catch (SocketException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void run() {
    if (tipo == TIPO.ENVIANDO) {
//      TODO: ENVIAR ARQUIVO!
    } else {
      Console.println("Informe a porta: " + socket.getLocalPort());
      try {
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        socket.receive(packet);
        String received = new String(packet.getData(), 0, packet.getLength()).trim();
        System.out.println("RECEBI: " + received);
      } catch (Exception ex) {
        Console.error("Não foi possível receber o arquivo");
      }
    }
  }
}
