package com.example.ftp.gui;

import com.example.ftp.client.Client;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class ClientFrame extends JFrame {

    private final JTextField ipField;
    private final JTextField portField;
    private final JTextField usernameField;
    private final JButton connectButton;
    private final JButton disconnectButton;
    private final DefaultListModel<String> fileListModel;
    private final JList<String> fileList;
    private final JTextArea chatArea;
    private final JTextField messageField;

    private Client client;

    public ClientFrame() {
        setTitle("FTP Client");
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // --- 上部パネル (接続情報) ---
        JPanel topPanel = new JPanel();
        topPanel.add(new JLabel("IP:"));
        ipField = new JTextField("localhost", 10);
        topPanel.add(ipField);
        topPanel.add(new JLabel("Port:"));
        portField = new JTextField("8080", 5);
        topPanel.add(portField);
        topPanel.add(new JLabel("User:"));
        usernameField = new JTextField("user" + (int)(Math.random()*100), 8);
        topPanel.add(usernameField);
        connectButton = new JButton("Connect");
        disconnectButton = new JButton("Disconnect");
        disconnectButton.setEnabled(false);
        topPanel.add(connectButton);
        topPanel.add(disconnectButton);
        add(topPanel, BorderLayout.NORTH);

        // --- 中央パネル (ファイル一覧とチャット) ---
        JSplitPane splitPane = new JSplitPane();
        splitPane.setResizeWeight(0.5);

        // ファイル一覧
        JPanel filePanel = new JPanel(new BorderLayout());
        filePanel.add(new JLabel("Server Files:"), BorderLayout.NORTH);
        fileListModel = new DefaultListModel<>();
        fileList = new JList<>(fileListModel);
        filePanel.add(new JScrollPane(fileList), BorderLayout.CENTER);
        JPanel fileButtons = new JPanel();
        JButton uploadButton = new JButton("Upload");
        JButton downloadButton = new JButton("Download");
        fileButtons.add(uploadButton);
        fileButtons.add(downloadButton);
        filePanel.add(fileButtons, BorderLayout.SOUTH);
        splitPane.setLeftComponent(filePanel);

        // --- アクションリスナーの追加 ---
        uploadButton.addActionListener(e -> uploadFile());
        downloadButton.addActionListener(e -> downloadFile());

        // チャット
        JPanel chatPanel = new JPanel(new BorderLayout());
        chatPanel.add(new JLabel("Chat:"), BorderLayout.NORTH);
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatPanel.add(new JScrollPane(chatArea), BorderLayout.CENTER);
        JPanel messagePanel = new JPanel(new BorderLayout());
        messageField = new JTextField();
        messagePanel.add(messageField, BorderLayout.CENTER);
        JButton sendButton = new JButton("Send");
        messagePanel.add(sendButton, BorderLayout.EAST);
        chatPanel.add(messagePanel, BorderLayout.SOUTH);
        splitPane.setRightComponent(chatPanel);
        add(splitPane, BorderLayout.CENTER);

        // --- アクションリスナー ---
        connectButton.addActionListener(e -> connectToServer());
        disconnectButton.addActionListener(e -> disconnectFromServer());
        sendButton.addActionListener(e -> sendMessage());
        messageField.addActionListener(e -> sendMessage()); // Enterキーでも送信
    }

    private void downloadFile() {
        String selectedFile = fileList.getSelectedValue();
        if (client == null || selectedFile == null) {
            JOptionPane.showMessageDialog(this, "No file selected or not connected.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Specify a directory to save");
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            java.io.File selectedDir = fileChooser.getSelectedFile();
            new Thread(() -> {
                try {
                    log("Downloading file: " + selectedFile);
                    client.downloadFile(selectedFile, selectedDir.getAbsolutePath());
                    log("File download complete.");
                } catch (IOException e) {
                    log("File download failed: " + e.getMessage());
                    JOptionPane.showMessageDialog(this, "File download failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }).start();
        }
    }

    private void uploadFile() {
        if (client == null) {
            JOptionPane.showMessageDialog(this, "Not connected to server.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            java.io.File selectedFile = fileChooser.getSelectedFile();
            new Thread(() -> {
                try {
                    log("Uploading file: " + selectedFile.getName());
                    client.uploadFile(selectedFile.getAbsolutePath());
                    log("File upload complete.");
                    // ファイル一覧を更新
                    client.sendMessage("LIST");
                } catch (IOException e) {
                    log("File upload failed: " + e.getMessage());
                    JOptionPane.showMessageDialog(this, "File upload failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }).start();
        }
    }

    private void sendMessage() {
        String message = messageField.getText();
        if (client != null && !message.isEmpty()) {
            client.sendMessage("MSG:" + message);
            // 自分の画面にもメッセージを表示
            log("[You]: " + message);
            messageField.setText("");
        }
    }

    private void connectToServer() {
        try {
            String ip = ipField.getText();
            int port = Integer.parseInt(portField.getText());
            client = new Client();
            client.connect(ip, port);

            // サーバーからのメッセージ受信を開始
            client.startListening(this::handleServerMessage);

            log("Connected to server.");
            connectButton.setEnabled(false);
            disconnectButton.setEnabled(true);

            // 接続したらファイル一覧を要求
            client.sendMessage("LIST");

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid port number.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Connection failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void disconnectFromServer() {
        try {
            if (client != null) {
                client.disconnect();
            }
            log("Disconnected from server.");
        } catch (IOException ex) {
            log("Error disconnecting: " + ex.getMessage());
        } finally {
            connectButton.setEnabled(true);
            disconnectButton.setEnabled(false);
        }
    }

    private void handleServerMessage(String message) {
        if (message.startsWith("BROADCAST:")) {
            String parsedMessage = message.substring("BROADCAST:".length());
            logToChat(parsedMessage);
        } else if (message.startsWith("FILE_LIST:")) {
            String fileData = message.substring("FILE_LIST:".length());
            updateFileList(fileData.split(","));
        } else {
            logToChat("[System]: " + message);
        }
    }

    private void updateFileList(String[] files) {
        SwingUtilities.invokeLater(() -> {
            fileListModel.clear();
            for (String file : files) {
                if (!file.isEmpty()) {
                    fileListModel.addElement(file);
                }
            }
        });
    }

    private void logToChat(String message) {
        SwingUtilities.invokeLater(() -> chatArea.append(message + "\n"));
    }

    private void log(String message) {
        SwingUtilities.invokeLater(() -> chatArea.append("[System]: " + message + "\n"));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ClientFrame().setVisible(true);
        });
    }
}
