package Socket;

import Console.Console;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Semaphore;

public class Sender extends Server {
  private boolean running;
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
  private Semaphore esperandoAck = new Semaphore(0);
  private List<Byte> bytesEnviados = new ArrayList<>();
//  protected Semaphore zonaDePerigo;

  protected void enviarPacote(String mensagem) throws IOException {
    super.enviarPacote(mensagem, sendingPort);
  }

  private class ACKReceiver extends Thread {
    private void ackRecebido(String str) {
      Console.println("======================================================");
      Console.log("Ack recebido: " + str);

      if (str.startsWith("HS")) {
        esperandoAck.release();
      }
    }

    @Override
    public void run() {
      try {
        while (running) {
          DatagramPacket packet = new DatagramPacket(buf, buf.length);
          socket.receive(packet);
          String received = new String(packet.getData(), 0, packet.getLength()).trim();
          ackRecebido(received);
        }
      } catch (IOException e) {
        Console.println("======================================================");
        Console.log("Erro no ACKReceiver!");
      }
    }
  }

  @Override
  public void run() {
    try {
      running = true;
      new ACKReceiver().start();
      Console.println("======================================================");
      Console.log("Handshake");
      enviarPacote("HS;" + nomeDoArquivo);
      esperandoAck.acquire();
      Console.println("======================================================");
      Console.log("Conexão bem sucedida!");
      Console.println("======================================================");
      Console.log("Slow Start começando!");
      while (i < tamanhoDaJanela) {
        
        i = i * 2;
      }
      Console.println("======================================================");
      Console.log("Congestion Avoidance começando!");
//      while (false) {
//
//      }
      Console.println("======================================================");
      Console.log("Fechando conexão");
      enviarPacote("END");
    } catch (IOException e) {
      Console.println("======================================================");
      Console.error("Erro ao enviar arquivo");
    } catch (InterruptedException interrupt) {
      Console.println("======================================================");
      Console.error("Thread Sender Interrompida");
    } finally {
      running = false;
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
