package com.goldthumb.chess;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.concurrent.Executors;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class ChessController implements ChessDelegate, ActionListener {
    private String SOCKET_SERVER_ADDR = "localhost";
    private int PORT = 50000;
    
    private ChessModel chessModel = new ChessModel();
    
    private JFrame frame;
    private ChessView chessBoardPanel;
    private JButton resetBtn;
    private JButton serverBtn;
    private JButton clientBtn;
    
    private ServerSocket listener;
    private Socket socket;
    private PrintWriter printWriter;
    
    ChessController() {
        chessModel.reset();
        
        frame = new JFrame("Chess - Local Game");
        frame.setSize(500, 550);
        frame.setLocation(200, 1300);
        frame.setLayout(new BorderLayout());
        
        chessBoardPanel = new ChessView(this);
        
        frame.add(chessBoardPanel, BorderLayout.CENTER);
        
        var buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        resetBtn = new JButton("Reset");
        resetBtn.addActionListener(this);
        buttonsPanel.add(resetBtn);
        
        serverBtn = new JButton("Host Game");
        buttonsPanel.add(serverBtn);
        serverBtn.addActionListener(this);
        
        clientBtn = new JButton("Join Game");
        buttonsPanel.add(clientBtn);
        clientBtn.addActionListener(this);
        
        frame.add(buttonsPanel, BorderLayout.PAGE_END);
        
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                closeNetworkResources();
            }
        });
    }

    private void closeNetworkResources() {
        if (printWriter != null) printWriter.close();
        try {
            if (listener != null) listener.close();
            if (socket != null) socket.close();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    private String getLocalIPAddress() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return "localhost";
        }
    }

    public static void main(String[] args) {
        new ChessController();
    }

    @Override
    public ChessPiece pieceAt(int col, int row) {
        return chessModel.pieceAt(col, row);
    }

    @Override
    public void movePiece(int fromCol, int fromRow, int toCol, int toRow) {
        chessModel.movePiece(fromCol, fromRow, toCol, toRow);
        chessBoardPanel.repaint();
        if (printWriter != null) {
            printWriter.println(fromCol + "," + fromRow + "," + toCol + "," + toRow);
        }
    }
    
    private void receiveMove(Scanner scanner) {
        while (scanner.hasNextLine()) {
            var moveStr = scanner.nextLine();
            System.out.println("chess move received: " + moveStr);
            var moveStrArr = moveStr.split(",");
            var fromCol = Integer.parseInt(moveStrArr[0]);
            var fromRow = Integer.parseInt(moveStrArr[1]);
            var toCol = Integer.parseInt(moveStrArr[2]);
            var toRow = Integer.parseInt(moveStrArr[3]);
            SwingUtilities.invokeLater(() -> {
                chessModel.movePiece(fromCol, fromRow, toCol, toRow);
                chessBoardPanel.repaint();
            });
        }
    }
    
    private void runSocketServer() {
        Executors.newFixedThreadPool(1).execute(() -> {
            try {
                listener = new ServerSocket(PORT);
                System.out.println("Server is listening on " + getLocalIPAddress() + ":" + PORT);
                socket = listener.accept();
                System.out.println("Connected from " + socket.getInetAddress());
                printWriter = new PrintWriter(socket.getOutputStream(), true);
                var scanner = new Scanner(socket.getInputStream());
                receiveMove(scanner);
            } catch (IOException e1) {
                e1.printStackTrace();
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(frame, "Server error: " + e1.getMessage());
                    resetConnection();
                });
            }
        });
    }
    
    private void runSocketClient(String serverIP) {
        Executors.newFixedThreadPool(1).execute(() -> {
            try {
                socket = new Socket(serverIP, PORT);
                System.out.println("Connected to server at " + serverIP + ":" + PORT);
                var scanner = new Scanner(socket.getInputStream());
                printWriter = new PrintWriter(socket.getOutputStream(), true);
                receiveMove(scanner);
            } catch (IOException e1) {
                e1.printStackTrace();
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(frame, "Failed to connect to server: " + e1.getMessage());
                    resetConnection();
                });
            }
        });
    }

    private void resetConnection() {
        closeNetworkResources();
        serverBtn.setEnabled(true);
        clientBtn.setEnabled(true);
        frame.setTitle("Chess - Local Game");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == resetBtn) {
            chessModel.reset();
            chessBoardPanel.repaint();
            resetConnection();
        } else if (e.getSource() == serverBtn) {
            String portStr = JOptionPane.showInputDialog(frame, "Enter port number:", PORT);
            if (portStr != null && !portStr.trim().isEmpty()) {
                try {
                    PORT = Integer.parseInt(portStr.trim());
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(frame, "Invalid port number");
                    return;
                }
            }
            
            serverBtn.setEnabled(false);
            clientBtn.setEnabled(false);
            SOCKET_SERVER_ADDR = getLocalIPAddress();
            frame.setTitle("Chess Server - " + SOCKET_SERVER_ADDR + ":" + PORT);
            runSocketServer();
            
            JOptionPane.showMessageDialog(frame, 
                "Server is running!\n\n" +
                "Your IP: " + SOCKET_SERVER_ADDR + "\n" +
                "Port: " + PORT + "\n\n" +
                "Share these with your friend to connect");
        } else if (e.getSource() == clientBtn) {
            String serverIP = JOptionPane.showInputDialog(frame, 
                "Enter server IP:", 
                SOCKET_SERVER_ADDR);
            
            if (serverIP == null || serverIP.trim().isEmpty()) {
                return;
            }
            
            String portStr = JOptionPane.showInputDialog(frame, "Enter port number:", PORT);
            if (portStr != null && !portStr.trim().isEmpty()) {
                try {
                    PORT = Integer.parseInt(portStr.trim());
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(frame, "Invalid port number");
                    return;
                }
            }
            
            serverBtn.setEnabled(false);
            clientBtn.setEnabled(false);
            frame.setTitle("Chess Client - Connected to " + serverIP + ":" + PORT);
            runSocketClient(serverIP.trim());
        }
    }
}