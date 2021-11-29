package Socket;

import Console.Console;

import java.io.IOException;
import java.net.DatagramPacket;
import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.zip.CRC32;

public class Sender extends Server {
  private boolean running;

  // Configurações de Arquivo
  public String nomeDoArquivo;
  public List<String> arquivo;
  private byte[] arquivoByteArray;

  // Configurações da Conexão
  public int sendingPort;

  // Configurações de Congestionamento
  public int tamanhoDaJanela = 8;
  private int quantidadeDePacotes = 1;
  private int pacoteAtual = 0;

  // Configurações de timeout
  private Calendar timeout;

  // Configurações de Envio
  private Semaphore esperandoAck = new Semaphore(0);
  private Map<Integer, List<Byte>> pacotesEnviados = new HashMap<>();

  // Configurações de Confirmação
  protected Semaphore zonaDePerigo = new Semaphore(1);
  List<String> acksRecebidos = new ArrayList<>();


  protected void enviarPacote(String mensagem) throws IOException {
    super.enviarPacote(mensagem, sendingPort);
  }

  private void separarEnviarProximoPacote() throws IOException, InterruptedException {
    Thread.sleep(700);
    byte[] paraContagem = new byte[tamanhoPacote];
    List<Byte> paraEnvio = new ArrayList<>(tamanhoPacote);
    int seq = pacoteAtual;
    int limite = seq + tamanhoPacote;
    int j = 0;
    while (pacoteAtual < limite && pacoteAtual < arquivoByteArray.length) {
      paraContagem[j] = arquivoByteArray[pacoteAtual];
      paraEnvio.add(arquivoByteArray[pacoteAtual]);
      j++;
      pacoteAtual++;
    }
    CRC32 crc = new CRC32();
    crc.update(paraContagem);
    pacotesEnviados.put(seq, paraEnvio);
    if (seq != 300) {
      enviarPacote("PCK;" + seq + ";" + crc.getValue() + ";" + paraEnvio);
    }
  }

  private void reenviarPacote(int s) throws IOException {
    byte[] paraContagem = new byte[tamanhoPacote];
    List<Byte> paraEnvio = pacotesEnviados.get(s);
    if (paraEnvio == null) return;
    for (int i = 0; i < paraEnvio.size(); i++) {
      paraContagem[i] = paraEnvio.get(i);
    }
    CRC32 crc = new CRC32();
    crc.update(paraContagem);
    Console.println("======================================================");
    Console.log("Reenviando o Pacote " + s);
    enviarPacote("PCK;" + s + ";" + crc.getValue() + ";" + paraEnvio);
  }

  private void verificarErros() throws InterruptedException {
    zonaDePerigo.acquire();
    ACKReceiver.waiting = true;
    zonaDePerigo.release();
    esperandoAck.acquire();
  }

  private class ACKReceiver extends Thread {
    public static boolean waiting = false;

    private void ackRecebido(String str) throws InterruptedException, IOException {
      if (!running) return;
      if (timeout != null && new GregorianCalendar().after(timeout)) {
        Console.println("======================================================");
        Console.log("Constatado TIMEOUT");
        running = true;
        timeout = new GregorianCalendar();
        timeout.add(Calendar.MINUTE, 1);
        pacoteAtual = 0;
        quantidadeDePacotes = 1;
        enviarPacote("TIMEOUT");

        if (waiting) {
          esperandoAck.release();
          waiting = false;
        }

        return;
      }

      if (str.startsWith("HS")) {
        esperandoAck.release();

        timeout = new GregorianCalendar();
        timeout.add(Calendar.SECOND, 15);

      } else if (str.startsWith("ACK")) {
        String[] comandos = str.split(";");

        zonaDePerigo.acquire();
        int atual = acksRecebidos.size();
        acksRecebidos.add(comandos[1]);
        if (acksRecebidos.size() > 3 && acksRecebidos.get(atual).equals(acksRecebidos.get(atual - 1)) && acksRecebidos.get(atual).equals(acksRecebidos.get(atual - 2))) {
          reenviarPacote(Integer.parseInt(acksRecebidos.get(atual)));
        }
        Console.println("======================================================");
        Console.log("ACK Recebido : " + comandos[1]);

        if (waiting) {
          esperandoAck.release();
          waiting = false;
        }
        zonaDePerigo.release();
      } else if (str.startsWith("ERROR")) {
        Console.println("======================================================");
        Console.error("Erro de transmissao!");
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
      Console.log("Conexao bem sucedida!");

      arquivoByteArray = String.join("\n", arquivo).getBytes();

      Console.println("======================================================");
      Console.log("Slow Start começando!");
      while (running && quantidadeDePacotes < tamanhoDaJanela && pacoteAtual < arquivoByteArray.length) {
        Console.println("======================================================");
        Console.log("Quantidade de pacotes = " + quantidadeDePacotes);
        for (int i = 0; i < quantidadeDePacotes && pacoteAtual < arquivoByteArray.length; i++) {
          separarEnviarProximoPacote();
        }
        verificarErros();
        quantidadeDePacotes = quantidadeDePacotes * 2;
      }

      if (running) {
        Console.println("======================================================");
        Console.log("Congestion Avoidance comecando!");
      }
      while (running && pacoteAtual < arquivoByteArray.length) {
        Console.println("======================================================");
        Console.log("Quantidade de pacotes = " + quantidadeDePacotes);
        for (int i = 0; i < quantidadeDePacotes && pacoteAtual < arquivoByteArray.length; i++) {
          separarEnviarProximoPacote();
        }
        verificarErros();
        quantidadeDePacotes++;
      }

      Console.println("======================================================");
      Console.log("Fechando conexao");
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
