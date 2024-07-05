package database;

import java.util.HashMap;
import java.util.Map;
import model.Flight;

public class ReservationDatabase {
    private final Map<String, Flight> flights = new HashMap<>();

    public void addFlight(String flightCode, Flight flight) {
        flights.put(flightCode, flight);
    }

    public Flight getFlight(String flightCode) {
        return flights.get(flightCode);
    }
}
