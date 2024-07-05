package model;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Flight {
    private final String from;
    private final String to;
    private final List<Seat> seats;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock(true);

    public Flight(String from, String to, int numSeats) {
        this.from = from;
        this.to = to;
        this.seats = new ArrayList<>(numSeats);
        for (int i = 0; i < numSeats; i++) {
            seats.add(new Seat());
        }
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public List<Seat> getSeats() {
        return seats;
    }

    public ReentrantReadWriteLock getLock() {
        return lock;
    }

    @Override
    public String toString() {
        return "Flight from " + from + " to " + to;
    }
}
