import Console.Console;
import Socket.Server;

public class main {
  private static Server server = new Server();

  public static void enviarArquivo() {
    Server.tipo = Server.TIPO.ENVIANDO;
    String port = Console.askForString("Digite a porta para enviar: ");
    Console.log("TODO: CORE SENDER : ", port);
    server.start();
  }

  public static void receberArquivo() {
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
      System.out.println("======================================================");
      switch (op) {
        case "1" -> {
          enviarArquivo();
        }
        case "2" -> {
          receberArquivo();
        }
        case "0" -> {
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
