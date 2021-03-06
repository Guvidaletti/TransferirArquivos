import Arquivos.GerenciadorDeArquivos;
import Console.Console;
import Socket.Receiver;
import Socket.Sender;

import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class main {
  public static void enviarArquivo() {
    Sender sender = new Sender();
    List<String> listaDeArquivos = Arrays.stream(GerenciadorDeArquivos.listFilesToSend()).filter(f -> f.matches("^(.*)\\.txt$")).collect(Collectors.toList());
    if (listaDeArquivos.size() > 0) {
      String op = "";
      int index = -1;
      while (index < 0 || index > listaDeArquivos.size() - 1) {
        Console.println("======================================================");
        Console.println("========== Lista de arquivos Disponíveis =============");
        Console.println("======================================================");
        for (int i = 0; i < listaDeArquivos.size(); i++) {
          Console.println(i + " - " + listaDeArquivos.get(i));
        }
        Console.println("======================================================");
        op = Console.askForString("Digite sua opcao: ");
        index = Integer.parseInt(op);
        if (index < 0 || index > listaDeArquivos.size() - 1) {
          Console.println("======================================================");
          Console.error("Opcao inválida!");
        }
      }
      try {
        sender.nomeDoArquivo = listaDeArquivos.get(index);
        sender.arquivo = GerenciadorDeArquivos.readFile(sender.nomeDoArquivo);
        Console.println("======================================================");
        String port = Console.askForString("Digite a porta para enviar: ");
        sender.sendingPort = Integer.parseInt(port);
        sender.start();
        sender.mainWaiting.acquire();
      } catch (InterruptedException e) {
        Console.error("A Thread foi interrompida!");
      } catch (FileNotFoundException fnf) {
        Console.error("Arquivo não encontrado!");
      }
    } else {
      Console.error("Não há arquivos para serem enviados!");
    }
  }

  public static void receberArquivo() {
    try {
      Receiver receiver = new Receiver();
      receiver.start();
      receiver.mainWaiting.acquire();
    } catch (InterruptedException e) {
      Console.error("A Thread foi interrompida!");
    }
  }

  public static void main(String[] args) {
    System.out.println("======================================================");
    System.out.println("= Transferencia de Arquivos por Alessandro e Gustavo =");
    String op = "";
    while (!op.equals("0")) {
      System.out.println("======================================================");
      System.out.println("1 - Enviar Arquivo");
      System.out.println("2 - Receber Arquivo");
      System.out.println("0 - Sair");
      System.out.println("======================================================");
      op = Console.askForString("Digite sua opcao: ");
      switch (op) {
        case "1" -> {
          enviarArquivo();
        }
        case "2" -> {
          receberArquivo();
        }
        case "0" -> {
          Console.println("======================================================");
          Console.println("Saindo");
          Console.print("======================================================");
        }
        default -> {
          Console.error("Opcao invalida");
        }
      }
    }
  }
}
