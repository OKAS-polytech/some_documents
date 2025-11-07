# External Design Document

## 1. System Architecture
The system consists of two main components: a central server and multiple clients.
- **Server**: Accepts connections from multiple clients, receives messages, and relays them to all other connected clients.
- **Client**: Connects to the server, sends user-input messages, and displays messages received from the server.

The communication protocol is based on TCP/IP sockets.

## 2. User Interface (CUI)
Both the server and client applications will have a simple command-line interface.

### Server Interface
- When started, the server will display the IP address and port number it is listening on.
- It will log events such as client connections and disconnections.

### Client Interface
- When started, the client will prompt the user for the server's IP address, port number, and a nickname.
- After connecting, the user can type messages and press Enter to send them.
- Received messages will be displayed on the console, prefixed with the sender's nickname.

## 3. Communication Protocol
- Messages are transmitted as plain text strings.
- Each message is terminated by a newline character (`\n`).
- The server will handle multiple client connections simultaneously using multithreading.

## 4. Key Scenarios
- **Client Connection**: A client sends a connection request to the server. The server accepts and establishes a new thread to handle the client.
- **Message Sending**: A client sends a message to the server.
- **Message Broadcasting**: The server receives a message and forwards it to all other connected clients.
- **Client Disconnection**: A client closes the connection. The server detects this and terminates the corresponding handler thread.
