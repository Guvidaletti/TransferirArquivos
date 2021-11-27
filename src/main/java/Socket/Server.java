package Socket;

import java.net.DatagramSocket;
import java.net.SocketException;

public class Server extends Thread {
  public enum TIPO {
    ENVIANDO, RECEBENDO;
  }

  public static DatagramSocket socket;
  public static TIPO tipo;

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

    } else {
      System.out.println("");
    }
  }
}
