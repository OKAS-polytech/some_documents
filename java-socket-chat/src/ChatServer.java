import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ChatServer {
    private final int port;
    private final List<ClientHandler> clientHandlers = new ArrayList<>();

    public ChatServer(int port) {
        this.port = port;
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java ChatServer <port>");
            return;
        }
        int port = Integer.parseInt(args[0]);
        new ChatServer(port).start();
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Chat Server is listening on port " + port);

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("New client connected");

                ClientHandler newUser = new ClientHandler(socket, this);
                clientHandlers.add(newUser);
                new Thread(newUser).start();
            }

        } catch (IOException ex) {
            System.out.println("Error in the server: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    void broadcastMessage(String message, ClientHandler excludeUser) {
        for (ClientHandler aUser : clientHandlers) {
            if (aUser != excludeUser) {
                aUser.sendMessage(message);
            }
        }
    }

    void removeClient(ClientHandler aUser) {
        clientHandlers.remove(aUser);
        System.out.println("Client disconnected: " + aUser.getNickname());
    }
}
