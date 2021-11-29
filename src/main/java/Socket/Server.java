package Socket;

import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.concurrent.Semaphore;

public abstract class Server extends Thread {
  public static DatagramSocket socket;
  protected byte[] buf = new byte[999];
  public Semaphore mainWaiting = new Semaphore(0);

  static {
    try {
      socket = new DatagramSocket();
    } catch (SocketException e) {
      e.printStackTrace();
    }
  }
}
