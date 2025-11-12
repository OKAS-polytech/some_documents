package com.example.ftp.gui;

import com.example.ftp.server.Server;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class ServerFrame extends JFrame {

    private final JTextField portField;
    private final JButton startButton;
    private final JButton stopButton;
    private final JTextArea logArea;
    private final DefaultListModel<String> clientListModel;

    private Thread serverThread;
    private Server server;

    public ServerFrame() {
        setTitle("FTP Server");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // --- 上部パネル ---
        JPanel topPanel = new JPanel();
        topPanel.add(new JLabel("Port:"));
        portField = new JTextField("8080", 5);
        startButton = new JButton("Start Server");
        stopButton = new JButton("Stop Server");
        stopButton.setEnabled(false);
        topPanel.add(portField);
        topPanel.add(startButton);
        topPanel.add(stopButton);
        add(topPanel, BorderLayout.NORTH);

        // --- 中央パネル (ログとクライアント一覧) ---
        JSplitPane splitPane = new JSplitPane();
        splitPane.setResizeWeight(0.7);

        logArea = new JTextArea();
        logArea.setEditable(false);
        splitPane.setLeftComponent(new JScrollPane(logArea));

        clientListModel = new DefaultListModel<>();
        JList<String> clientList = new JList<>(clientListModel);
        splitPane.setRightComponent(new JScrollPane(clientList));
        add(splitPane, BorderLayout.CENTER);

        // --- アクションリスナー ---
        startButton.addActionListener(e -> startServer());
        stopButton.addActionListener(e -> stopServer());
    }

    private void startServer() {
        try {
            int port = Integer.parseInt(portField.getText());
            server = new Server();
            serverThread = new Thread(() -> {
                try {
                    server.start(port);
                } catch (IOException ex) {
                    SwingUtilities.invokeLater(() ->
                        JOptionPane.showMessageDialog(this, "Server error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE)
                    );
                }
            });
            serverThread.start();
            log("Server started on port " + port);
            startButton.setEnabled(false);
            stopButton.setEnabled(true);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid port number.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void stopServer() {
        if (server != null) {
            try {
                server.stop();
                serverThread.interrupt(); // accept()でブロックしているスレッドを中断
                log("Server stopped.");
            } catch (IOException ex) {
                log("Error stopping server: " + ex.getMessage());
            }
        }
        startButton.setEnabled(true);
        stopButton.setEnabled(false);
    }

    private void log(String message) {
        SwingUtilities.invokeLater(() -> logArea.append(message + "\n"));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ServerFrame().setVisible(true);
        });
    }
}
