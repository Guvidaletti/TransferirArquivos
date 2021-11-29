package Socket;

import Console.Console;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.concurrent.Semaphore;

public class Server extends Thread {
  public enum TIPO {
    ENVIANDO, RECEBENDO;
  }

  public static DatagramSocket socket;
  public static TIPO tipo;
  private byte[] buf = new byte[999];
  public Semaphore mainWaiting = new Semaphore(0);

  static {
    try {
      socket = new DatagramSocket();
    } catch (SocketException e) {
      e.printStackTrace();
    }
  }

  // Enviando
  public static int tamanhoDaJanela = 6;
  public static String[] arquivo;

  // RECEBENDO


  @Override
  public void run() {
    if (tipo == TIPO.ENVIANDO) {
//      byte[] bytearr = String.join("\n", arquivo).getBytes();
      mainWaiting.release();
    } else {
      try {
        Console.println("Esperando arquivo...");
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        socket.receive(packet);
        String received = new String(packet.getData(), 0, packet.getLength()).trim();
        System.out.println("RECEBI: " + received);
        mainWaiting.release();
      } catch (Exception ex) {
        Console.error("Não foi possível receber o arquivo");
        mainWaiting.release();
      }
    }
  }
}
