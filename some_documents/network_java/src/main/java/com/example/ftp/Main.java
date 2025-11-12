package com.example.ftp;

import com.example.ftp.gui.ClientFrame;
import com.example.ftp.gui.ServerFrame;
import javax.swing.SwingUtilities;

public class Main {

    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Usage: java -jar <jarfile> [server|client]");
            System.exit(1);
        }

        String mode = args[0];

        if ("server".equalsIgnoreCase(mode)) {
            SwingUtilities.invokeLater(() -> {
                new ServerFrame().setVisible(true);
            });
        } else if ("client".equalsIgnoreCase(mode)) {
            SwingUtilities.invokeLater(() -> {
                new ClientFrame().setVisible(true);
            });
        } else {
            System.err.println("Invalid mode: " + mode);
            System.err.println("Usage: java -jar <jarfile> [server|client]");
            System.exit(1);
        }
    }
}
