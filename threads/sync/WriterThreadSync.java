package threads.sync;

import database.ReservationDatabase;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.BiConsumer;
import main.Main;
import model.Flight;
import model.Seat;

public class WriterThreadSync extends Thread {
    private final ReservationDatabase database;
    private final String flightCode;
    private final BiConsumer<String, List<Boolean>> seatStatusUpdater;
    private final int seatNumber;
    private final boolean cancel;
    private final String threadName;
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_RED = "\u001B[31m";

    public WriterThreadSync(ReservationDatabase database, String flightCode, BiConsumer<String, List<Boolean>> seatStatusUpdater, int seatNumber, boolean cancel, String threadName) {
        this.database = database;
        this.flightCode = flightCode;
        this.seatStatusUpdater = seatStatusUpdater;
        this.seatNumber = seatNumber;
        this.cancel = cancel;
        this.threadName = threadName;
    }

    @Override
    public void run() {
        Flight flight = database.getFlight(flightCode);
        if (flight != null) {
            ReentrantReadWriteLock.WriteLock writeLock = flight.getLock().writeLock();
            writeLock.lock();
            try {
                LocalTime now = LocalTime.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
                Seat seat = flight.getSeats().get(seatNumber);

                if (cancel) {
                    if (seat.isReserved() && seat.getReservingThread().equals(threadName)) {
                        seat.cancel(threadName);
                        System.out.println(ANSI_RED + now.format(formatter) + " - " + threadName + " cancelled reservation for seat " + (seatNumber + 1) + " on " + flight + ANSI_RESET);
                    } else {
                        System.out.println(ANSI_RED + now.format(formatter) + " - " + threadName + " found seat " + (seatNumber + 1) + " was not reserved by this thread on " + flight + ANSI_RESET);
                    }
                } else {
                    if (!seat.isReserved()) {
                        seat.reserve(threadName);
                        Main.updateLastReservedSeat(seatNumber);
                        Main.updateLastReservingThread(threadName);
                        System.out.println(ANSI_RED + now.format(formatter) + " - " + threadName + " reserved seat " + (seatNumber + 1) + " on " + flight + ANSI_RESET);
                    } else {
                        System.out.println(ANSI_RED + now.format(formatter) + " - " + threadName + " found seat " + (seatNumber + 1) + " already reserved on " + flight + ANSI_RESET);
                    }
                }
                List<Boolean> seatStatus = flight.getSeats().stream().map(Seat::isReserved).toList();
                seatStatusUpdater.accept(flightCode, seatStatus);
            } finally {
                writeLock.unlock();
            }
        }
    }
}
