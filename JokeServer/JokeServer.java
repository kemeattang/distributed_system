
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
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;

public class JokeServer {
    static int DEFAULT_PORT = 4545;
    static int SECONDARY_PORT = 4546;
    static String DEFAULT_HOST = "localhost";
    static String SECONDARY = "secondary";
    public static int QUEUE_LENGTH = 6; // Queue size of request

    public enum Type {
        JOKE, PROVERB
    }

    public static Type type = Type.JOKE;

    public static void main(String args[]) throws IOException {
        ArrayList<ClientData> clientData = new ArrayList<ClientData>();
        // Two map store the J and B answer
        HashMap<String, String> jokesDatabase = new HashMap<String, String>();
        HashMap<String, String> proverbsDatabase = new HashMap<String, String>();
        jokesDatabase.put("JA", "Why did the chicken cross the road? To get to the other side!");
        jokesDatabase.put("JB", "Why do birds fly south for the winter? It's easier than walking!");
        jokesDatabase.put("JC","What happens to a frog's car when it breaks down? It gets toad away.");
        jokesDatabase.put("JD", "Why did the picture go to jail? Because it was framed.");
        proverbsDatabase.put("PA", "Better to light a candle than to curse the darkness.");
        proverbsDatabase.put("PB", "Fortune favors the brave.");
        proverbsDatabase.put("PC", "Comparison is the thief of joy.");
        proverbsDatabase.put("PD", "The early bird gets the worm");

        InetAddress inetAddress = InetAddress.getByName(DEFAULT_HOST);
        if (args.length == 0) {
            // Just start first server
            System.out.println("Abasiekeme's primary server starting up, listening at port " + DEFAULT_PORT + ".");
            // Modify the server to accept a connection at port 5050 from an administration
            // client
            ModifyServer modifyServer = new ModifyServer(5050);
            Thread thread = new Thread(modifyServer);
            thread.start();

            // Open socket for listen client
            ServerSocket server = new ServerSocket(DEFAULT_PORT, QUEUE_LENGTH, inetAddress);
            // Server looping for process client request
            while (true) {
                Socket socket = server.accept();
                // Create worker for handle client request
                new ServerWorker(socket, clientData, jokesDatabase, proverbsDatabase, false)
                        .start();
            }
        } else {
            // Start secondary server
            System.out.println(
                    "Abasiekeme's secondary server starting up, listening at port " + SECONDARY_PORT + ".");
            // Modify the server to accept a connection at port 5051 for the secondary
            // server
            ModifyServer modifyServer = new ModifyServer(5051);
            Thread thread = new Thread(modifyServer);
            thread.start();

            // Open socket for listen client
            ServerSocket server = new ServerSocket(SECONDARY_PORT, QUEUE_LENGTH, inetAddress);
            // Server looping for process client request
            while (true) {
                Socket socket = server.accept();
                // Create worker for handle client request
                new ServerWorker(socket, clientData, jokesDatabase, proverbsDatabase, true).start();
            }
        }
    }
}

class ClientData {
    final int uuid; // unique identify client
    LinkedList<String> jokeLabel; // Keep label of joke have not sent
    LinkedList<String> proverbLabel; // Kepp label of proverb not sent

    // Constructor with ID of client
    ClientData(int uuid) {
        this.uuid = uuid;
        jokeLabel = new LinkedList<String>();
        proverbLabel = new LinkedList<String>();
        generateJokeLabels();
        generateProverbLabels();
    }

    // Generate Joke Label with random position
    void generateJokeLabels() {
        jokeLabel.add("JA");
        jokeLabel.add("JB");
        jokeLabel.add("JC");
        jokeLabel.add("JD");
        Collections.shuffle(jokeLabel);
    }

    // Generate proverb label with random position
    void generateProverbLabels() {
        proverbLabel.add("PA");
        proverbLabel.add("PB");
        proverbLabel.add("PC");
        proverbLabel.add("PD");
        Collections.shuffle(proverbLabel);
    }
}

class ModifyServer implements Runnable {
    int port;

    ModifyServer(int p) {
        port = p;
    }

    public void run() {
        // Socket connect to Admin Client
        Socket socket;
        try {
            ServerSocket server = new ServerSocket(port, JokeServer.QUEUE_LENGTH);
            while (true) {
                // Handle logic with Admin Client
                socket = server.accept();
                new ModifyWorker(socket).start();
            }
        } catch (IOException ioe) {
            System.out.println(ioe);
        }
    }
}

class ModifyWorker extends Thread {
    Socket socket;

    ModifyWorker(Socket s) {
        socket = s;
    }

    public void run() {
        BufferedReader in = null;
        try {
            // When Admin Client press enter. Change type of server
            // JokeClientAdmin that connects at the administration port and toggles the
            // server between Joke Mode and Proverb Mode
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            if (JokeServer.type == JokeServer.Type.JOKE) {
                System.out.println("Type of server change to PROVERB.");
                JokeServer.type = JokeServer.Type.PROVERB;
            } else {
                System.out.println("Type of server change to JOKE.");
                JokeServer.type = JokeServer.Type.JOKE;
            }
            // Closes this socket connection.
            socket.close();
        } catch (IOException ioe) {
            System.out.println(ioe);
        }
    }
}

class ServerWorker extends Thread {
    Socket socket;
    ArrayList<ClientData> clients; // List client
    boolean secondary; // Identify this is first or secondary server
    static HashMap<String, String> jokesMap; // Map joke label
    static HashMap<String, String> proverbsMap; // Map proverbs label

    ServerWorker(Socket sock, ArrayList<ClientData> clientList, HashMap<String, String> jokes,
            HashMap<String, String> proverbs, boolean second) {
        socket = sock;
        clients = clientList;
        jokesMap = jokes;
        proverbsMap = proverbs;
        secondary = second;
    }

    static void processJokeServer(PrintStream out, ClientData client, boolean secondary) {
        try {
            // Take joke from joke maps
            String jokeLabel = client.jokeLabel.pop();
            // If this is secondary server. Add <S2>
            if (secondary)
                out.println("<S2> " + jokeLabel);
            else
                out.println(jokeLabel);
            out.println(jokesMap.get(jokeLabel));

            // If send all joke message. Print JOKE CYCLE COMPLETED. And generate new joke
            // order map
            if (client.jokeLabel.isEmpty()) {
                System.out.println("JOKE CYCLE COMPLETED");
                client.generateJokeLabels();
            }

        } catch (Exception e) {
            out.println("Failed when handle mode JOKE.");
        }
    }

    static void processProverbServer(PrintStream out, ClientData client, boolean secondary) {
        try {
            String proverbLabel = client.proverbLabel.pop();
            // If this is secondary server. Add <S2> to proverbLabel
            if (secondary)
                out.println("<S2> " + proverbLabel);
            else
                out.println(proverbLabel);
            out.println(proverbsMap.get(proverbLabel));

            // If send all provebLabel. Print PROVERB CYCLE COMPLETED. And generate new
            // proverb database
            if (client.proverbLabel.isEmpty()) {
                client.generateProverbLabels();
                System.out.println("PROVERB CYCLE COMPLETED");
            }

        } catch (Exception e) {
            out.println("Failed when handle mode PROVERB.");
        }
    }

    public void run() {
        try {
            // Buffer read from client
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            // Sent response to client
            PrintStream out = new PrintStream(socket.getOutputStream());

            try {
                int uuid;
                int index = 0;
                uuid = Integer.parseInt(in.readLine()); // read UIID from Joke Client
                // Save client data
                if (clients.isEmpty()) {
                    clients.add(new ClientData(uuid));
                } else {
                    // Check client exist or not
                    boolean isExist = false;
                    for (int i = 0; i < clients.size(); i++) {
                        if (uuid == clients.get(i).uuid) {
                            isExist = true;
                            index = i;
                        }
                    }
                    if (!isExist) { 
                        // Client list is not empty and client is not in the list
                        // Add client to list
                        clients.add(new ClientData(uuid));
                        index = clients.size() - 1;
                    }
                }
                if (JokeServer.type == JokeServer.Type.JOKE) {
                    // If this is JOKE server. Just response joke from map
                    processJokeServer(out, clients.get(index), secondary);
                } else {
                    // If this is PROVERB server. Response proverb
                    processProverbServer(out, clients.get(index), secondary);
                }
            } catch (IOException x) {
                System.out.println("Server read error.");
                x.printStackTrace();
            }
            // Closes this socket connection.
            socket.close();
        } catch (IOException ioe) {
            System.out.println(ioe);
        }
    }

}