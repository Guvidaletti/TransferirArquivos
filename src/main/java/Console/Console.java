package Console;

import java.util.*;
import java.util.stream.Collectors;

public class Console {
  public static boolean showDate = true;
  private static List<String> esperando = new ArrayList<>();
  private static Scanner scanner = new Scanner(System.in);

  public static final String ANSI_RESET = "\u001B[0m";
  public static final String ANSI_BLACK = "\u001B[30m";
  public static final String ANSI_RED = "\u001B[31m";
  public static final String ANSI_GREEN = "\u001B[32m";
  public static final String ANSI_YELLOW = "\u001B[33m";
  public static final String ANSI_BLUE = "\u001B[34m";
  public static final String ANSI_PURPLE = "\u001B[35m";
  public static final String ANSI_CYAN = "\u001B[36m";
  public static final String ANSI_WHITE = "\u001B[37m";

  public static void log(Object... str) {
    if (esperando.size() > 0) {
      System.out.println("");
    }
    GregorianCalendar c = new GregorianCalendar();
    if (showDate) {
      System.out.print(ANSI_CYAN + "[");
      System.out.print(c.get(Calendar.HOUR_OF_DAY) + ":");
      System.out.print(c.get(Calendar.MINUTE) + ":");
      System.out.print(c.get(Calendar.SECOND));
      System.out.print("] : " + ANSI_RESET);
    }
    System.out.println(Arrays.stream(str).map(Object::toString).collect(Collectors.joining(" ")));
    esperando.forEach(System.out::print);
  }

  public static void println(String msg) {
    if (esperando.size() > 0) {
      System.out.println("");
    }
    System.out.println(msg);
    esperando.forEach(System.out::print);
  }

  public static void print(String msg) {
    if (esperando.size() > 0) {
      System.out.println("");
    }
    System.out.print(msg);
    esperando.forEach(System.out::print);
  }

  public static String askForString(String msg) {
    System.out.print(msg);
    esperando.add(msg);
    String entrada = scanner.nextLine();
    esperando.remove(0);
    return entrada;
  }

  public static void error(String msg) {
    if (esperando.size() > 0) {
      System.out.println("");
    }
    System.out.println(ANSI_RED + msg + ANSI_RESET);
    esperando.forEach(System.out::print);
  }

}
