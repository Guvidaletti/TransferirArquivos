package Socket;

import Console.Console;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.zip.CRC32;

public class Receiver extends Server {
  public String nomeDoArquivo;

  private void comandoRecebido(String str, int port, InetAddress address) throws IOException, InterruptedException {
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
        Thread.sleep(1000);
        String sequencia = recebido[1];
        byte[] byteArr = new byte[100];
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
        } else {
          //TODO: Verificar se o proximo seq é o sequencia + 100
          Console.println("======================================================");
          Console.log("A Sequência " + sequencia + " CRC Conferida!");
          enviarPacote("ACK;" + sequencia, port);
        }
      }
    }
  }

  @Override
  public void run() {
    try {
      Console.println("======================================================");
      Console.println("Sua porta é: " + socket.getLocalPort());
      Console.println("======================================================");
      Console.println("Esperando o arquivo...");

      String received = "";
      while (!received.startsWith("END")) {
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        socket.receive(packet);
        received = new String(packet.getData(), 0, packet.getLength()).trim();
        comandoRecebido(received, packet.getPort(), packet.getAddress());
      }
      Console.println("======================================================");
      Console.log("Conexão Fechada!");
    } catch (IOException | InterruptedException e) {
      Console.log("Erro no Receiver!");
    } finally {
      mainWaiting.release();
    }
  }
}
