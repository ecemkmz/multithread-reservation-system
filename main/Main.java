package main;

import database.ReservationDatabase;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import model.Flight;
import threads.async.ReaderThreadAsync;
import threads.async.WriterThreadAsync;
import threads.sync.ReaderThreadSync;
import threads.sync.WriterThreadSync;

public class Main {
    private static final ReservationDatabase database = new ReservationDatabase();
    private static JButton[] seatButtons;
    private static JTextArea logArea;
    private static JLabel flightInfoLabel;
    private static int lastReservedSeat = -1;
    private static String lastReservingThread = "";

    public static void main(String[] args) {
        Flight flight1 = new Flight("Istanbul", "Ankara", 9);
        Flight flight2 = new Flight("Izmir", "Antalya", 9);
        database.addFlight("IST-ANK", flight1);
        database.addFlight("IZM-ANT", flight2);

        JFrame frame = new JFrame("Flight Reservation System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLayout(new BorderLayout());

        JPanel flightInfoPanel = new JPanel();
        flightInfoPanel.setLayout(new BorderLayout());
        flightInfoPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        flightInfoLabel = new JLabel("Select a flight to start reservation", SwingConstants.CENTER);
        flightInfoLabel.setFont(new Font("Arial", Font.BOLD, 16));
        flightInfoPanel.add(flightInfoLabel, BorderLayout.NORTH);

        JPanel seatPanel = new JPanel();
        seatPanel.setLayout(new GridLayout(3, 3, 10, 10));
        seatPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        seatButtons = new JButton[9];
        for (int i = 0; i < 9; i++) {
            seatButtons[i] = new JButton("Seat " + (i + 1));
            seatButtons[i].setBackground(new Color(144, 238, 144));
            seatButtons[i].setFont(new Font("Arial", Font.BOLD, 14));
            seatButtons[i].setFocusPainted(false);
            seatButtons[i].setBorder(new RoundedBorder(20)); 
            seatPanel.add(seatButtons[i]);
        }
        flightInfoPanel.add(seatPanel, BorderLayout.CENTER);

        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new GridLayout(2, 3, 10, 10)); 
        JButton startButton = new JButton("Start IST-ANK Sync");
        JButton startButton2 = new JButton("Start IZM-ANT Sync");
        JButton startButtonNoLock = new JButton("Start IST-ANK Async");
        JButton startButton2NoLock = new JButton("Start IZM-ANT Async");
        JButton cancelReservationButton = new JButton("Cancel Reservation");

        styleButton(startButton);
        styleButton(startButton2);
        styleButton(startButtonNoLock);
        styleButton(startButton2NoLock);
        styleButton(cancelReservationButton);

        controlPanel.add(startButton);
        controlPanel.add(startButton2);
        controlPanel.add(startButtonNoLock);
        controlPanel.add(startButton2NoLock);
        controlPanel.add(cancelReservationButton);

        logArea = new JTextArea(10, 30);
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        logArea.setBackground(new Color(245, 245, 245));
        JScrollPane logScrollPane = new JScrollPane(logArea);

        frame.add(flightInfoPanel, BorderLayout.NORTH);
        frame.add(controlPanel, BorderLayout.CENTER);
        frame.add(logScrollPane, BorderLayout.SOUTH);

        startButton.addActionListener(e -> startThreads("IST-ANK"));
        startButton2.addActionListener(e -> startThreads("IZM-ANT"));
        startButtonNoLock.addActionListener(e -> startThreadsNoLock("IST-ANK"));
        startButton2NoLock.addActionListener(e -> startThreadsNoLock("IZM-ANT"));
        cancelReservationButton.addActionListener(e -> cancelReservation("IST-ANK"));

        frame.setVisible(true);
    }

    private static void styleButton(JButton button) {
        button.setFont(new Font("Arial", Font.BOLD, 12)); 
        button.setBackground(new Color(70, 130, 180));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(new RoundedBorder(20)); 
        button.setPreferredSize(new Dimension(150, 40)); 
    }

    private static void startThreads(String flightCode) {
        Flight flight = database.getFlight(flightCode);
        if (flight != null) {
            flightInfoLabel.setText("Flight: " + flight);
        }

        Random random = new Random();
        int seatNumber = random.nextInt(flight.getSeats().size());
        lastReservedSeat = seatNumber;

        List<Thread> threads = new ArrayList<>();
        threads.add(new WriterThreadSync(database, flightCode, Main::updateSeatStatus, seatNumber, false, "WriterThread-1"));
        threads.add(new WriterThreadSync(database, flightCode, Main::updateSeatStatus, seatNumber, false, "WriterThread-2"));
        threads.add(new WriterThreadSync(database, flightCode, Main::updateSeatStatus, seatNumber, false, "WriterThread-3"));
        threads.add(new ReaderThreadSync(database, flightCode, Main::updateSeatStatus, "ReaderThread-1"));
        threads.add(new ReaderThreadSync(database, flightCode, Main::updateSeatStatus, "ReaderThread-2"));
        threads.add(new ReaderThreadSync(database, flightCode, Main::updateSeatStatus, "ReaderThread-3"));

        Collections.shuffle(threads);

        ScheduledExecutorService executor = Executors.newScheduledThreadPool(6);
        for (int i = 0; i < threads.size(); i++) {
            executor.schedule(threads.get(i), i * 100, TimeUnit.MILLISECONDS);
        }
    }

    private static void startThreadsNoLock(String flightCode) {
        Flight flight = database.getFlight(flightCode);
        if (flight != null) {
            flightInfoLabel.setText("Flight: " + flight);
        }

        Random random = new Random();
        int seatNumber = random.nextInt(flight.getSeats().size());
        lastReservedSeat = seatNumber;

        List<Thread> threads = new ArrayList<>();
        threads.add(new WriterThreadAsync(database, flightCode, Main::updateSeatStatus, seatNumber, "WriterThreadNoLock-1"));
        threads.add(new WriterThreadAsync(database, flightCode, Main::updateSeatStatus, seatNumber, "WriterThreadNoLock-2"));
        threads.add(new WriterThreadAsync(database, flightCode, Main::updateSeatStatus, seatNumber, "WriterThreadNoLock-3"));
        threads.add(new ReaderThreadAsync(database, flightCode, Main::updateSeatStatus, "ReaderThreadNoLock-1"));
        threads.add(new ReaderThreadAsync(database, flightCode, Main::updateSeatStatus, "ReaderThreadNoLock-2"));
        threads.add(new ReaderThreadAsync(database, flightCode, Main::updateSeatStatus, "ReaderThreadNoLock-3"));

        Collections.shuffle(threads);

        ScheduledExecutorService executor = Executors.newScheduledThreadPool(6);
        for (int i = 0; i < threads.size(); i++) {
            executor.schedule(threads.get(i), i * 100, TimeUnit.MILLISECONDS);
        }
    }

    private static void cancelReservation(String flightCode) {
        Flight flight = database.getFlight(flightCode);
        if (flight != null && lastReservedSeat != -1 && !lastReservingThread.isEmpty()) {
            Thread cancelThread = new WriterThreadSync(database, flightCode, Main::updateSeatStatus, lastReservedSeat, true, lastReservingThread);
            ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
            executor.schedule(cancelThread, 0, TimeUnit.SECONDS);
            lastReservedSeat = -1;
            lastReservingThread = "";
        } else {
            logArea.append("No seat to cancel.\n");
        }
    }

    public static void updateLastReservedSeat(int seatNumber) {
        lastReservedSeat = seatNumber;
    }

    public static void updateLastReservingThread(String threadName) {
        lastReservingThread = threadName;
    }

    private static void updateSeatStatus(String flightCode, List<Boolean> seatStatus) {
        SwingUtilities.invokeLater(() -> {
            StringBuilder logBuilder = new StringBuilder();
            logBuilder.append("Flight: ").append(flightCode).append("\n");
            for (int i = 0; i < seatStatus.size(); i++) {
                if (seatStatus.get(i)) {
                    seatButtons[i].setBackground(new Color(255, 69, 0));
                } else {
                    seatButtons[i].setBackground(new Color(144, 238, 144));
                }
                logBuilder.append("Seat ").append(i + 1).append(": ")
                        .append(seatStatus.get(i) ? "Reserved" : "Available").append("\n");
            }
            logBuilder.append("\n");
            logArea.append(logBuilder.toString());
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }
}

class RoundedBorder implements Border {
    private int radius;

    RoundedBorder(int radius) {
        this.radius = radius;
    }

    public Insets getBorderInsets(Component c) {
        return new Insets(radius + 1, radius + 1, radius + 2, radius);
    }

    public boolean isBorderOpaque() {
        return true;
    }

    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        g.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
    }
}
