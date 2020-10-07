package Controller;

import java.util.Scanner;

public class Main {

  public static void main(String[] args) {
    Controller controller = new Controller();
    while (true) {
      System.out.println("Use parseQueries, parseFiles, createIndex, postFiles, & query");
      Scanner input = new Scanner(System.in);
      String command = input.next();
      if (command.equals("parseQueries")) {
        controller.parseQueries();
      } else if (command.equals("createIndex")) {
        controller.createIndex();
      } else if (command.equals("postFiles")) {
        controller.postFiles();
      } else if (command.equals("parseFiles")) {
        controller.parseFiles();
      } else if (command.equals("query")) {
        controller.query();
      } else {
        System.out.println("not a command");
        break;
      }
    }
  }
}
