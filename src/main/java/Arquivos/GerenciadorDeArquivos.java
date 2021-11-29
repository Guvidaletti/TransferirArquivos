package Arquivos;

import java.io.*;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class GerenciadorDeArquivos {
  public static String getPathName(String folderName) {
    return Paths.get("").toAbsolutePath() + "/" + folderName;
  }

  public static List<String> readFile(String filename) throws FileNotFoundException {
    File f = new File(getPathName("") + filename);
    FileReader arquivo = new FileReader(f);
    BufferedReader br = new BufferedReader(arquivo);
    return br.lines().collect(Collectors.toList());
  }

  public static boolean createFolderIfNotExists() throws Exception {
    File f = new File(getPathName("received"));
    return f.mkdirs();
  }

  public static void createFile(String fileName, String lines) throws IOException {
    FileWriter arquivo = new FileWriter(getPathName("received") + "/" + fileName);
    PrintWriter gravarNoArquivo = new PrintWriter(arquivo);
    gravarNoArquivo.print(lines);
    arquivo.close();
  }

  public static String[] listFilesToSend() {
    File f = new File(getPathName(""));
    return f.list();
  }
}
