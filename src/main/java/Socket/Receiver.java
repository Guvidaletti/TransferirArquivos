package Socket;

import Console.Console;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;

public class Receiver extends Server {
  public String nomeDoArquivo;

  private void comandoRecebido(String str, int port, InetAddress address) throws IOException {
    String[] recebido = str.split(";");
    String comando = recebido[0];
    Console.println("======================================================");
    Console.log("Comando recebido: ", comando);

    switch (comando) {
      case "HS" -> {
        Console.println("======================================================");
        Console.log("Conexão bem sucedida!");
        nomeDoArquivo = recebido[1].trim();
        enviarPacote(comando, port);
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
    } catch (IOException e) {
      Console.log("Erro no Receiver!");
    } finally {
      mainWaiting.release();
    }
  }
}
