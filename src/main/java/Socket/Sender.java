package Socket;

import Console.Console;

import java.io.IOException;
import java.net.DatagramPacket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.zip.CRC32;

public class Sender extends Server {
  private boolean running;

  // Configurações de Arquivo
  public String nomeDoArquivo;
  public List<String> arquivo;
  private byte[] arquivoByteArray;
//  private int pacotesNecessarios;

  // Configurações da Conexão
  public int sendingPort;

  // Configurações de Congestionamento
  public int tamanhoDaJanela = 4;
  private int quantidadeDePacotes = 1;
  private int pacoteAtual = 0;

  // Configurações de timeout
//  public int timeout = 1000 * 60; // 1 minuto
//  private Calendar calendar;

  // Configurações de Envio
  private Semaphore esperandoAck = new Semaphore(0);
  private List<Byte> bytesEnviados = new ArrayList<>();

  // Configurações de Confirmação
  protected Semaphore zonaDePerigo = new Semaphore(1);
  List<String> acksRecebidos = new ArrayList<>();


  protected void enviarPacote(String mensagem) throws IOException {
    super.enviarPacote(mensagem, sendingPort);
  }

  private void separarEnviarProximoPacote() throws IOException, InterruptedException {
    byte[] paraContagem = new byte[100];
    List<Byte> paraEnvio = new ArrayList<>(100);
    int seq = pacoteAtual;
    int limite = seq + 100;
    int j = 0;
    while (pacoteAtual < limite && pacoteAtual < arquivoByteArray.length) {
      paraContagem[j] = arquivoByteArray[pacoteAtual];
      paraEnvio.add(arquivoByteArray[pacoteAtual]);
      j++;
      pacoteAtual++;
    }
    CRC32 crc = new CRC32();
    crc.update(paraContagem);
    bytesEnviados.addAll(paraEnvio);
    enviarPacote("PCK;" + seq + ";" + crc.getValue() + ";" + paraEnvio);
    Thread.sleep(500);
  }

  private void verificarErros() throws InterruptedException {
    zonaDePerigo.acquire();
    ACKReceiver.waiting = true;
    zonaDePerigo.release();
    esperandoAck.acquire();
    System.out.println("IAEEEEE");
    // TODO: Verificar 3 ultimos ACKS recebidos
    // TODO: Verificar timeout
  }

  private class ACKReceiver extends Thread {
    public static boolean waiting = false;

    private void ackRecebido(String str) throws InterruptedException {
      if (str.startsWith("HS")) {
        esperandoAck.release();
      } else if (str.startsWith("ACK")) {
        String[] comandos = str.split(";");
        zonaDePerigo.acquire();
        acksRecebidos.add(comandos[1]);
        int atual = acksRecebidos.size() - 1;
        if (
          acksRecebidos.size() >= 3
            && acksRecebidos.get(atual).equals(acksRecebidos.get(atual - 1))
            && acksRecebidos.get(atual).equals(acksRecebidos.get(atual - 2))) {
          //TODO: ERRO! &  verificar se precisa  do else ali pra baixo
          acksRecebidos.remove(atual);
          acksRecebidos.remove(atual - 1);
          acksRecebidos.remove(atual - 2);

        }
        if (waiting) {
          esperandoAck.release();
        }
        zonaDePerigo.release();
      } else if (str.startsWith("ERROR")) {
        Console.println("======================================================");
        Console.error("Erro de transmissão!");
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
      } catch (IOException | InterruptedException e) {
        Console.println("======================================================");
        Console.error("Erro no ACKReceiver!");
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

      arquivoByteArray = String.join("\n", arquivo).getBytes();

      Console.println("======================================================");
      Console.log("Slow Start começando!");
      while (quantidadeDePacotes < tamanhoDaJanela && pacoteAtual < arquivoByteArray.length) {
        for (int i = 0; i < quantidadeDePacotes && pacoteAtual < arquivoByteArray.length; i++) {
          separarEnviarProximoPacote();
        }
        verificarErros();
        quantidadeDePacotes = quantidadeDePacotes * 2;
      }
      Console.println("======================================================");
      Console.log("Congestion Avoidance começando!");
      while (pacoteAtual < arquivoByteArray.length) {
        for (int i = 0; i < quantidadeDePacotes && pacoteAtual < arquivoByteArray.length; i++) {
          separarEnviarProximoPacote();
        }
        verificarErros();
        quantidadeDePacotes++;
      }
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
