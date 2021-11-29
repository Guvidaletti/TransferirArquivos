package Socket;

import Console.Console;

import java.io.IOException;
import java.net.DatagramPacket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Semaphore;

public class Sender extends Server {

  // Configurações da Conexão
  public String nomeDoArquivo;
  public List<String> arquivo;
  public int sendingPort;

  // Configurações de Congestionamento
  public int tamanhoDaJanela = 32;
  private int i = 1;

  // Configurações de timeout
  public int timeout = 1000 * 60; // 1 minuto
  private Calendar calendar;

  // Configurações de Envio
  private Semaphore esperandoAck;
  private List<Byte> bytesEnviados = new ArrayList<>();
//  protected Semaphore zonaDePerigo;

  private class ACKReceiver extends Thread {
    @Override
    public void run() {
      try {
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        socket.receive(packet);
        String received = new String(packet.getData(), 0, packet.getLength()).trim();
        System.out.println("RECEBI" + received);
//        Core.comandoRecebido(received, packet.getPort(), packet.getAddress());
      } catch (IOException e) {
        Console.log("Erro no ACKReceiver!");
      }
    }
  }

  @Override
  public void run() {
    try {
      new ACKReceiver().start();
      Console.log("Handshake");
      String send = "HS;";
      byte[] sendArr = send.getBytes();
      DatagramPacket HandShake = new DatagramPacket(sendArr, sendArr.length, socket.getInetAddress(), sendingPort);
      socket.send(HandShake);

      Console.log("Slow Start começando!");
      for (int i = 1; i < tamanhoDaJanela; i = i * 2) {

      }
      Console.log("Congestion Avoidance começando!");
    } catch (IOException e) {
      Console.error("Erro ao enviar arquivo");
    } finally {
      mainWaiting.release();
    }
  }

}
//  Console.log("Recebendo arquivo");
//  byte[] bytearr = String.join("\n", arquivo).getBytes();
//  calendar = new GregorianCalendar();
//    Console.log("Definindo Timeout!");
//
//
////    CRC32 crc = new CRC32();
////    crc.update(bytearr);
//    Console.log("Começando Slow Start");
//    for (int i = 0; i < tamanhoDaJanela; i = i * 2) {
//
//  }

//  DatagramPacket packet = new DatagramPacket(byteArr, byteArr.length, address, port);
//  Server.socket.send(packet);
