import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final ChatServer server;
    private PrintWriter out;
    private BufferedReader in;
    private String nickname;

    public ClientHandler(Socket socket, ChatServer server) {
        this.socket = socket;
        this.server = server;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            out.println("Enter your nickname:");
            nickname = in.readLine();
            server.broadcastMessage(nickname + " has joined the chat.", this);

            String message;
            while ((message = in.readLine()) != null) {
                server.broadcastMessage("[" + nickname + "]: " + message, this);
            }

        } catch (IOException e) {
            // Connection reset is expected when a client disconnects
        } finally {
            server.removeClient(this);
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    void sendMessage(String message) {
        out.println(message);
    }

    public String getNickname() {
        return nickname;
    }
}
