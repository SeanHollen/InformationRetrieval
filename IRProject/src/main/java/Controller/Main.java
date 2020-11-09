package Controller;

import java.util.Scanner;

public class Main {

  public static void main(String[] args) {
    System.out.println("48=1470221522|17|37,43,171,198,214,221,235,246,285,299,378,385,396,417,464,521,546,;-1841754192|1|429,;-394834763|2|13,17,;-394834638|2|18,23,;-394834693|1|18,;-394833926|1|19,;-394834824|1|19,;1216471702|3|237,253,255,;-394833859|2|39,44,;-394833895|1|17,;-1271041285|1|36,;1334807253|1|121,;1334806293|1|276,;-455659887|1|196,;-497709390|1|51,;431841662|1|115,;-1813123982|2|6,31,;1393935692|1|31,;-1841753045|1|56,;2082732089|1|375,;574989464|7|6,7,11,12,13,14,15,;357254616|1|196,;-1299324834|2|182,189,;1187842496|6|27,71,109,212,242,294,;386226296|3|259,339,462,;-1253710408|1|36,;193554468|1|250,;-436633317|1|36,;1392064558|1|294,;1392065363|1|36,;2054102852|1|246,;-444362786|1|281,;-493889732|1|17,;439573238|1|8,;1195574810|1|21,;-2072657414|15|29,41,86,146,188,191,194,197,200,206,224,227,245,287,326,;1187843422|3|214,269,648,;364982906|1|36,;357252698|1|113,;-1188719722|1|158,;243082521|1|93,;1130584219|1|386,;1101955105|1|414,;-1167820934|1|237,;-1413841230|1|185,;477802805|1|4,;477801653|1|4,;464039833|1|378,;1365305428|1|227,;-612225028|1|4,;422241306|1|178,;-554967937|1|36,;1330989306|2|135,145,;1298447738|1|36,;-1413841351|1|54,;300338999|1|36,;-465262373|1|247,;1344407635|1|196,;536759864|1|4,;1344408718|2|98,318,;410943034|1|184,;365327483|1|42,;1220289499|1|36,;-1232809572|1|24,;1159214491|1|4,;1412964403|1|90,;1159213430|3|261,310,347,;1565711350|1|155,;-1032405582|1|126,;471769051|1|36,;1367003131|1|36,;1166599228|1|36,;-1303238183|1|382,;595888119|1|211,;-2129914789|1|325,;-2129914574|1|180,;357597983|2|383,549,;279442043|1|336,;1273729948|1|195,;-1339595846|1|398,;1594338546|1|36,;1281115768|11|24,105,117,132,156,213,225,234,282,285,291,;1281115767|1|4,;-406132165|1|130,;222181726|1|241,;1363437173|1|47,;".getBytes().length);
    Controller controller = new Controller();
    while (true) {
      System.out.println("Options: standardStart, parseQueries, parseFiles, parseStemming, " +
              "elasticIndex, index, postFiles, queryElastic, query, merge, clear, " +
              "dummyTest, fileAccessTest\n");
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
      } else if (command.equals("queryElastic")) {
        controller.queryElastic();
      } else if (command.equals("query")) {
        controller.queryPrivate();
      }else if (command.equals("parseStemming")) {
        controller.parseStemming();
      } else if (command.equals("index")) {
        controller.privateIndex();
      } else if (command.equals("merge")) {
        controller.merge();
      } else if (command.equals("dummyTest")) {
        controller.test();
      } else if (command.equals("fileAccessTest")){
        controller.testRandomAccessFile();
      } else if (command.equals("clear")) {
        controller.clear();
      } else {
        System.out.println("not a command");
        break;
      }
    }
  }
}
