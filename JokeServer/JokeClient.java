
/*--------------------------------------------------------

1. Name / Date: Abasiekeme Attang/ 26-Sep-2019

2. Java 1.8

3. Precise command-line compilation examples / instructions:

> javac JokeClient.java


4. Precise examples / instructions to run this program:

In separate shell windows:

> java JokeServer
> java JokeClient
> java JokeClientAdmin

All acceptable commands are displayed on the various consoles.

This runs across machines, in which case you have to pass the IP address of
the server to the clients. For exmaple, if the server is running at
140.192.1.22 then you would type:

> java JokeClient 140.192.1.22
> java JokeClientAdmin 140.192.1.22

5. List of files needed for running the program.

 a. checklist-joke.html
 b. JokeServer.java
 c. JokeClient.java
 d. JokeClientAdmin.java

5. Notes:

----------------------------------------------------------*/

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.UUID;

public class JokeClient {
    static String DEFAULT_HOST = "localhost";
    static int DEFAULT_PORT = 4545;
    static int SECONDARY_PORT = 4546;

    static void processServerResponse(int uuid, String userName, String server, int port) {
        Socket socket;
        BufferedReader fromServer;
        PrintStream toServer;
        String[] textFromServer = new String[2];

        try {
            // Open socket to communicate with server
            socket = new Socket(InetAddress.getByName(server), port);
            // Read response from server
            fromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Send data to server
            toServer = new PrintStream(socket.getOutputStream());

            // Send uuid and username to server
            toServer.println(uuid);
            toServer.println(userName);
            toServer.flush();
            for (int i = 0; i < 2; i++) {
                // Reads line of text/data from the server
                textFromServer[i] = fromServer.readLine();
            }
            System.out.println(textFromServer[0] + " " + userName + ": " + textFromServer[1]);

            // Closes this socket's connection
            socket.close();
        } catch (IOException x) {
            System.out.println("Socket error.");
            x.printStackTrace();
        }
    }

    public static void main(String args[]) throws IOException {
        String firstServerName = null;
        String secondServerName = null;
        try {
            if (args.length < 1) {
                firstServerName = DEFAULT_HOST;
            } else {
                // Take fist server host via argument 1
                firstServerName = args[0];
            }
            System.out.println("Abasiekeme's client connect to primary server : "
                    + InetAddress.getByName(firstServerName) + ", port " + DEFAULT_PORT);
            if (args.length >= 2) {
                // Take secondary server via argument 2
                secondServerName = args[1];
                System.out.println("Abasiekeme's client connect to secondary server : "
                        + InetAddress.getByName(secondServerName) + ", port " + SECONDARY_PORT);
            }
        } catch (UnknownHostException ex) {
            System.out.println("Failed to connected server");
        }
        // Ask user name
        System.out.print("Your name: ");
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        String userName = "";
        try {
            userName = in.readLine();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        // Generate UUID for user
        int uuid = UUID.randomUUID().variant();
        String input;
        String server = firstServerName;
        int port = DEFAULT_PORT;
        try {
            do {
                System.out.print(
                        "Press enter to receive a joke or proverb message, (s) to switch servers, (quit) to end: ");
                input = in.readLine();
                if (input.isEmpty()) {
                    processServerResponse(uuid, userName, server, port);
                }
                // Check if switch server command
                if (input.equals("s")) {
                    if (secondServerName == null) {
                        // If this is no secondary server
                        System.out.println("Currently run with one server only");
                    } else {
                        // If we have secondary server. Change port to process response
                        if (port == DEFAULT_PORT) {
                            // If currently connect to primary server. Change connect to secondary
                            // server
                            server = secondServerName;
                            port = SECONDARY_PORT;
                            System.out.println("Abasiekeme's client change connect to : "
                                    + InetAddress.getByName(server) + ", port " + port);
                        } else {
                            // If current is connect to secondary server. Change to primary server
                            server = firstServerName;
                            port = DEFAULT_PORT;
                            System.out.println("Abasiekeme's client change connect to: "
                                    + InetAddress.getByName(server) + ", port " + port);
                        }
                    }
                }
            } while (input.indexOf("quit") < 0); // Loop until user type quit
            System.out.println("Cancelled by user request.");
        } catch (UnknownHostException uhe) {
            System.out.println("Failed attempt to look up server.");
        }
    }
}
