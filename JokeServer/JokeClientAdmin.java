
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
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class JokeClientAdmin {

    static String DEFAULT_HOST = "localhost";
    static int DEFAULT_PORT = 5050;
    static int SECONDARY_PORT = 5051;

    // Method to connect to JokeServer to change mode between JOKE and PROVEB
    static void changeMode(String server, int port) {
        Socket socket;
        try {
            socket = new Socket(server, port);
            socket.close();
        } catch (IOException x) {
            System.out.println("Socket error.");
            x.printStackTrace();
        }
    }

    public static void main(String[] args) {
        String firstServerName = null;
        String secondServerName = null;
        try {
            if (args.length < 1) {
                firstServerName = DEFAULT_HOST;
            } else {
                // Take fist server host via argument 1
                firstServerName = args[0];
            }
            System.out.println("Abasiekeme's Admin client connect to primary server : "
                    + InetAddress.getByName(firstServerName) + ", port " + DEFAULT_PORT);
            if (args.length >= 2) {
                // Take secondary server via argument 2
                secondServerName = args[1];
                System.out.println("Abasiekeme's Admin client connect to secondary server : "
                        + InetAddress.getByName(secondServerName) + ", port " + SECONDARY_PORT);
            }
        } catch (UnknownHostException ex) {
            System.out.println("Failed to connected server");
        }
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        // Wait for user input command to connect to JokeServer to toggle mode, or
        // switch the server that JokeClientAdmin is connected to.
        try {
            String mode;
            String server = firstServerName;
            int port = DEFAULT_PORT;
            do {
                System.out.print(
                        "Press enter to change mode server, type (s) to switch servers, (quit) to end: ");
                System.out.flush();
                mode = in.readLine();
                if (mode.isEmpty())
                    // If user type enter. Change mode of current server
                    changeMode(server, port);

                // If user switch server
                // Check if switch server command
                if (mode.equals("s")) {
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
            } while (mode.indexOf("quit") < 0); // Loop until user type quit
            System.out.println("Cancelled by user request.");
        } catch (UnknownHostException uhe) {
            System.out.println("Failed attempt to look up server.");
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

}
