package threads.async;

import database.ReservationDatabase;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.BiConsumer;
import model.Flight;
import model.Seat;

public class WriterThreadAsync extends Thread {
    private final ReservationDatabase database;
    private final String flightCode;
    private final BiConsumer<String, List<Boolean>> seatStatusUpdater;
    private final int seatNumber;
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_RED = "\u001B[31m";

    public WriterThreadAsync(ReservationDatabase database, String flightCode, BiConsumer<String, List<Boolean>> seatStatusUpdater, int seatNumber, String threadName) {
        super(threadName);
        this.database = database;
        this.flightCode = flightCode;
        this.seatStatusUpdater = seatStatusUpdater;
        this.seatNumber = seatNumber;
    }

    @Override
    public void run() {
        Flight flight = database.getFlight(flightCode);
        if (flight != null) {
            LocalTime now = LocalTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
            Seat seat = flight.getSeats().get(seatNumber);

            seat.setReserved(true);
            System.out.println(ANSI_RED + now.format(formatter) + " - " + getName() + " reserved seat " + (seatNumber + 1) + " on " + flight + ANSI_RESET);
            List<Boolean> seatStatus = flight.getSeats().stream().map(Seat::isReserved).toList();
            seatStatusUpdater.accept(flightCode, seatStatus);
        }
    }
}
