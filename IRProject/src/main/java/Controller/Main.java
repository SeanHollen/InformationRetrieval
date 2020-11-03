package Controller;

import java.util.Scanner;

public class Main {

  public static void main(String[] args) {
    Controller controller = new Controller();
    while (true) {
      System.out.println("Options: standardStart, parseQueries, parseFiles, parseStemming, " +
              "elasticIndex, index, postFiles, query, merge, test\n");
      Scanner input = new Scanner(System.in);
      String command = input.next();
      if (command.equals("standardStart")) {
        controller.standardStart();
      } else if (command.equals("parseQueries")) {
        controller.parseQueries();
      } else if (command.equals("elasticIndex")) {
        controller.createElasticIndex();
      } else if (command.equals("postFiles")) {
        controller.postFiles();
      } else if (command.equals("parseFiles")) {
        controller.parseFiles();
      } else if (command.equals("query")) {
        controller.query();
      } else if (command.equals("parseStemming")) {
        controller.parseStemming();
      } else if (command.equals("index")) {
        controller.privateIndex();
      } else if (command.equals("merge")) {
        controller.merge();
      } else if (command.equals("test")) {
        controller.test();
      } else {
        System.out.println("not a command");
        break;
      }
    }
  }
}
