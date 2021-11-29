import Arquivos.GerenciadorDeArquivos;
import Console.Console;
import Socket.Server;

import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

public class main {
  public static Semaphore semaphore = new Semaphore(0);

  public static void enviarArquivo() {
    Server server = new Server();
    Server.tipo = Server.TIPO.ENVIANDO;
    List<String> listaDeArquivos = Arrays.stream(GerenciadorDeArquivos.listFilesToSend()).filter(f -> f.matches("^(.*)\\.txt$")).collect(Collectors.toList());
    if (listaDeArquivos.size() > 0) {
      String op = "";
      int index = -1;
      while (index < 0 || index > listaDeArquivos.size() - 1) {
        Console.println("======================================================");
        for (int i = 0; i < listaDeArquivos.size(); i++) {
          Console.println(i + " - " + listaDeArquivos.get(i));
        }
        Console.println("======================================================");
        op = Console.askForString("Digite sua opção: ");
        index = Integer.parseInt(op);
        if (index < 0 || index > listaDeArquivos.size() - 1) {
          Console.println("======================================================");
          Console.error("Opção inválida!");
        }
      }
      try {
        server.arquivo = GerenciadorDeArquivos.readFile(listaDeArquivos.get(index));
        Console.println("======================================================");
        String port = Console.askForString("Digite a porta para enviar: ");
        server.sendingPort = Integer.parseInt(port);
        server.start();
        server.mainWaiting.acquire();
      } catch (InterruptedException e) {
        Console.error("Não foi possível manter a Thread Main!");
      } catch (FileNotFoundException fnf) {
        Console.error("Arquivo não encontrado!");
      }
    } else {
      Console.println("Não há arquivos para serem enviados!");
    }
  }

  public static void receberArquivo() {
    Server server = new Server();
    Server.tipo = Server.TIPO.RECEBENDO;
    server.start();
  }

  public static void main(String[] args) {
    System.out.println("======================================================");
    System.out.println("==Transferência de Arquivos por Alessandro e Gustavo==");
    String op = "";
    while (!op.equals("0")) {
      System.out.println("======================================================");
      System.out.println("1 - Enviar Arquivo");
      System.out.println("2 - Receber Arquivo");
      System.out.println("0 - Sair");
      System.out.println("======================================================");
      op = Console.askForString("Digite sua opção: ");
      switch (op) {
        case "1" -> {
          enviarArquivo();
        }
        case "2" -> {
          receberArquivo();
        }
        case "0" -> {
          Console.println("======================================================");
          Console.println("Saíndo");
          Console.print("======================================================");
        }
        default -> {
          Console.error("Opção inválida");
        }
      }
    }
  }
}
