package Socket;

import Arquivos.GerenciadorDeArquivos;
import Console.Console;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.zip.CRC32;

public class Receiver extends Server {
  private boolean running;
  public String nomeDoArquivo;
  private Semaphore zonaDoPerigo = new Semaphore(1);
  private Map<Integer, byte[]> pacotesRecebidos = new TreeMap<>();
  private int ultimoRecebido = 0;

  private boolean temFaltante() {
    return pacotesRecebidos.values().size() < (100 + ultimoRecebido) / tamanhoPacote;
  }

  private int qualPacoteFalta() {
    for (int i = 0; i < ultimoRecebido; i = i + 100) {
      if (!pacotesRecebidos.containsKey(i)) {
        return i;
      }
    }
    return -1;
  }

  private void comandoRecebido(String str, int port, InetAddress address) throws Exception {
    String[] recebido = str.split(";");
    String comando = recebido[0];

    switch (comando) {
      case "HS" -> {
        Console.println("======================================================");
        Console.log("Conexão bem sucedida!");
        nomeDoArquivo = recebido[1].trim();
        enviarPacote(comando, port);
      }
      case "PCK" -> {
        Thread.sleep(700);
        String sequencia = recebido[1];
        byte[] byteArr = new byte[tamanhoPacote];
        String byteString = recebido[3].replaceAll("(\\[|]|\\s+)", "");
        String[] sArr = byteString.split(",");
        for (int i = 0; i < sArr.length; i++) {
          if (sArr[i] == null || sArr[i].length() == 0) break;
          byteArr[i] = Byte.parseByte(sArr[i]);
        }
        long sendedCrcValue = Long.parseLong(recebido[2]);
        CRC32 crc = new CRC32();
        crc.update(byteArr);
        if (sendedCrcValue != crc.getValue()) {
          Console.error("CRC com problema!");
          running = false;
        } else {
          Console.println("======================================================");
          Console.log("Sequência: " + sequencia + " - CRC Conferida!");
          int seq = Integer.parseInt(sequencia);
          pacotesRecebidos.put(seq, byteArr);
          ultimoRecebido = seq;
          if (temFaltante()) {
            System.out.println("TEMM FALTANTE! " + qualPacoteFalta());
            enviarPacote("ACK;" + qualPacoteFalta(), port);
          } else {
            enviarPacote("ACK;" + seq, port);
          }
        }
      }
      case "END" -> {
        running = false;
        Console.println("======================================================");
        Console.log("Criando arquivo...");
        List<Byte> bytes = new ArrayList<>();
        pacotesRecebidos.values().forEach(bts -> {
          for (byte b : bts) {
            bytes.add(b);
          }
        });
        byte[] byteArray = new byte[bytes.size()];
        for (int i = 0; i < byteArray.length; i++) {
          byteArray[i] = bytes.get(i);
        }
        String linhas = new String(byteArray, StandardCharsets.UTF_8).trim();
        GerenciadorDeArquivos.createFolderIfNotExists();
        GerenciadorDeArquivos.createFile(nomeDoArquivo, linhas);
      }
      case "TIMEOUT" -> {
        Console.println("======================================================");
        Console.error("ERRO: TIMEOUT");
      }
    }
  }

  @Override
  public void run() {
    running = true;
    try {
      Console.println("======================================================");
      Console.println("Sua porta é: " + socket.getLocalPort());
      Console.println("======================================================");
      Console.println("Esperando o arquivo...");

      String received = "";
      while (running && !received.startsWith("END")) {
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        socket.receive(packet);
        received = new String(packet.getData(), 0, packet.getLength()).trim();
        comandoRecebido(received, packet.getPort(), packet.getAddress());
      }
      Console.println("======================================================");
      Console.log("Conexão Fechada!");
    } catch (Exception e) {
      Console.log("Erro no Receiver!");
    } finally {
      mainWaiting.release();
    }
  }
}
