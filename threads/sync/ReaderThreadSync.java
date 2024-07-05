package threads.sync;

import database.ReservationDatabase;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.BiConsumer;
import model.Flight;

public class ReaderThreadSync extends Thread {
    private final ReservationDatabase database;
    private final String flightCode;
    private final BiConsumer<String, List<Boolean>> seatStatusUpdater;
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_BLUE = "\u001B[34m";

    public ReaderThreadSync(ReservationDatabase database, String flightCode, BiConsumer<String, List<Boolean>> seatStatusUpdater, String threadName) {
        super(threadName);
        this.database = database;
        this.flightCode = flightCode;
        this.seatStatusUpdater = seatStatusUpdater;
    }

    @Override
    public void run() {
        Flight flight = database.getFlight(flightCode);
        if (flight != null) {
            ReentrantReadWriteLock.ReadLock readLock = flight.getLock().readLock();
            readLock.lock();
            try {
                LocalTime now = LocalTime.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
                System.out.println(ANSI_BLUE + now.format(formatter) + " - " + getName() + " looks for available seats. State of the seats are:" + ANSI_RESET);
                List<Boolean> seatStatus = flight.getSeats().stream().map(seat -> seat.isReserved()).toList();
                seatStatusUpdater.accept(flightCode, seatStatus);
                for (int i = 0; i < flight.getSeats().size(); i++) {
                    System.out.print("Seat No " + (i + 1) + " : " + (seatStatus.get(i) ? "1" : "0") + " ");
                }
                System.out.println("\n-------------------------------------------");
            } finally {
                readLock.unlock();
            }
        }
    }
}
