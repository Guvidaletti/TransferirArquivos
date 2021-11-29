package Socket;

import java.io.IOException;
import java.net.*;
import java.util.concurrent.Semaphore;

public abstract class Server extends Thread {
  public static DatagramSocket socket;
  protected static InetAddress localhost;
  protected byte[] buf = new byte[999];
  protected int tamanhoPacote = 100;
  public Semaphore mainWaiting = new Semaphore(0);

  static {
    try {
      socket = new DatagramSocket();
      localhost = InetAddress.getByName("localhost");
    } catch (SocketException | UnknownHostException e) {
      e.printStackTrace();
    }
  }

  protected void enviarPacote(String mensagem, int sendingPort) throws IOException {
    byte[] sendArr = mensagem.getBytes();
    DatagramPacket HandShake = new DatagramPacket(sendArr, sendArr.length, localhost, sendingPort);
    socket.send(HandShake);
  }
}
