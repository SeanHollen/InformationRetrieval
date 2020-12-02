package Controller;

import java.util.Scanner;

public class Main {

  public static void main(String[] args) {
    Controller controller = new Controller();
    while (true) {
      System.out.println("Options: standardStart, parseQueries, parseFiles, parseSites, " +
              "parseStemming, elasticIndex, index, post, teamCloud, queryElastic, query, " +
              "merge, clear, crawl, print, dummyTest");
      Scanner input = new Scanner(System.in);
      String command = input.next();
      if (command.equals("standardStart") || command.equals("start")) {
        controller.standardStart();
      } else if (command.equals("parseQueries")) {
        controller.parseQueries();
      } else if (command.equals("elasticIndex")) {
        controller.createElasticIndex();
      } else if (command.equals("post")) {
        controller.postFiles();
      } else if (command.equals("teamCloud")) {
        controller.teamCloud();
      } else if (command.equals("parseFiles")) {
        controller.parseTestDocuments();
      } else if (command.equals("parseSites")) {
        controller.parseWebsites();
      } else if (command.equals("queryElastic")) {
        controller.queryElastic();
      } else if (command.equals("query")) {
        controller.queryPrivate();
      } else if (command.equals("parseStemming")) {
        controller.parseStemming();
      } else if (command.equals("index")) {
        controller.privateIndex();
      } else if (command.equals("merge")) {
        controller.merge();
      } else if (command.equals("dummyTest")) {
        controller.test();
      } else if (command.equals("clear")) {
        controller.clear();
      } else if (command.equals("crawl")) {
        controller.crawl();
      } else if (command.equals("print")) {
        controller.printFiles();
      } else {
        System.out.println("not a command");
        break;
      }
    }
  }
}
