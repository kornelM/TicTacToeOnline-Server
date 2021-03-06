
import model.Game;
import view.Printer;

import java.net.*;
import java.io.*;
import java.util.Scanner;

public class Server extends Thread {
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String BLINK = "\u001B[4m";

    String printSampleBoard = "printSampleBoard\n";
    String printPlayerId = "printPlayerId\n";
    String startOfAGame = "startOfAGame\n";
    String makeMove = "makeMove\n";
    String isSet = "isSet\n";
    String isFull = "isFull\n";
    String setSign = "setSign\n";
    String currentBoard = "";

    public static int playedId = 1;

    private ServerSocket serverSocket;

    public Server(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        //        serverSocket.setSoTimeout(10000);
    }

    public static void switchPlayer(int playerdId) {
        if (playedId == 1)
            playedId = 2;
        else
            playedId = 1;
    }

    public void welcomeMessage() {
        System.out.println(ANSI_RED + "Hello in TicTacToe game!" + ANSI_RESET);
        System.out.println();
        System.out.println(ANSI_CYAN + "Let's start the game!" + ANSI_RESET);
        System.out.println();
        System.out.println(ANSI_RED + "Here is board with numbers." + ANSI_RESET);
    }

    public void run() {
        while (true) {
            try {
                System.out.println("Waiting for client on port " + serverSocket.getLocalPort() + "...");
                Socket server = serverSocket.accept();
                System.out.println("Just connected to " + server.getRemoteSocketAddress());

                BufferedReader in = new BufferedReader(new InputStreamReader(server.getInputStream()));
                System.out.println(in.readLine());

                DataOutputStream out = new DataOutputStream(server.getOutputStream());
                out.writeBytes("Thank you for connecting to " + server.getLocalSocketAddress() + "\n");

                int ROWS = 3;
                int COLS = 3;
                int chosenField;

                Game game = new Game(ROWS, COLS);
                Printer printer = new Printer();
                Scanner scanner = new Scanner(System.in);

                welcomeMessage();
                printer.printSampleGameBoardWithNumbers(ROWS, COLS);


                printer.printTable(game);


                while (!game.isDone()) {
                    if (playedId == 2) {

                        while (true) {
                            chosenField = in.read();

                            if (!game.makeMove(chosenField, playedId)) {
                                out.writeBytes("INVALID " +game.parseBoardToString()+ "\n");
                            } else {
                                if (game.isDone()) {
                                    out.writeBytes(String.valueOf(game.getWinner().getNumOfPlayer()));
                                    break;
                                }
                                out.writeBytes(game.parseBoardToString() + "\n");
                                break;
                            }
                        }
                    } else {

                        printer.printMessageToPlayerOnConsole(playedId);

                        while (true) {
                            chosenField = scanner.nextInt();

                            if (!game.makeMove(chosenField, playedId)) {
                                printer.printMessageToPlayerOnConsole(playedId);
                            } else {
                                if (game.isDone()) {
                                    out.writeBytes(String.valueOf(game.getWinner().getNumOfPlayer()));
                                    break;
                                }
                                out.writeBytes(game.parseBoardToString() + "\n");
                                break;
                            }
                        }
                    }

                    printer.printTable(game);
                    switchPlayer(playedId);

                }
                System.out.println("End of game!");
                printer.printInfo(game);


                server.close();

            } catch (SocketTimeoutException s) {
                System.out.println("Socket timed out!");
                break;
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }
    }
}