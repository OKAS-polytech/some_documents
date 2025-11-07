# Manual

## 1. Prerequisites
- Java Development Kit (JDK) 8 or higher must be installed.
- You can verify the installation by running `java -version` and `javac -version` in your terminal.

## 2. How to Compile
1. Open a terminal or command prompt.
2. Navigate to the `src` directory inside the `java-socket-chat` project folder.
   ```sh
   cd java-socket-chat/src
   ```
3. Compile all the Java source files.
   ```sh
   javac *.java
   ```
This will generate `.class` files for each `.java` file in the `src` directory.

## 3. How to Run the Application

### 3.1. Start the Server
1. In the terminal, while still in the `src` directory, run the following command:
   ```sh
   java ChatServer <port>
   ```
   Replace `<port>` with the port number you want the server to listen on (e.g., 8080).
2. The server will start and display a message indicating it is waiting for clients.

### 3.2. Start the Client
1. Open a new terminal or command prompt for each client you want to run.
2. Navigate to the `src` directory.
   ```sh
   cd path/to/java-socket-chat/src
   ```
3. Run the following command:
   ```sh
   java ChatClient <host> <port>
   ```
   - Replace `<host>` with the IP address of the server (e.g., `127.0.0.1` if running on the same machine).
   - Replace `<port>` with the same port number the server is using.
4. After running the command, the client will prompt you to enter a nickname.
5. Once you provide a nickname, you can start sending and receiving messages.

## 4. How to Use
- **Sending Messages**: Type your message in the client's console and press `Enter`.
- **Receiving Messages**: Messages from other clients will appear in your console, prefixed with the sender's nickname.
- **Exiting**: To disconnect a client, close its terminal window or press `Ctrl+C`.
