import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class ChatClient {
    private final String hostname;
    private final int port;
    private PrintWriter out;
    private BufferedReader in;

    public ChatClient(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java ChatClient <host> <port>");
            return;
        }

        String hostname = args[0];
        int port = Integer.parseInt(args[1]);

        new ChatClient(hostname, port).execute();
    }

    public void execute() {
        try (Socket socket = new Socket(hostname, port)) {
            System.out.println("Connected to the chat server");

            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // Thread to read messages from the server
            new Thread(() -> {
                String serverMessage;
                try {
                    while ((serverMessage = in.readLine()) != null) {
                        System.out.println(serverMessage);
                    }
                } catch (IOException e) {
                    System.out.println("Error reading from server: " + e.getMessage());
                }
            }).start();

            // Main thread to read user input and send to the server
            BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));
            String userInput;
            while ((userInput = consoleReader.readLine()) != null) {
                out.println(userInput);
            }

        } catch (UnknownHostException ex) {
            System.out.println("Server not found: " + ex.getMessage());
        } catch (IOException ex) {
            System.out.println("I/O Error: " + ex.getMessage());
        }
    }
}
