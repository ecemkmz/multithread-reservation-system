package model;

import java.util.ArrayList;
import java.util.List;

public class Seat {
    private boolean reserved;
    private String reservingThread;

    public Seat() {
        this.reserved = false;
        this.reservingThread = "";
    }

    public boolean isReserved() {
        return reserved;
    }

    public void setReserved(boolean reserved) {
        this.reserved = reserved;
    }

    public String getReservingThread() {
        return reservingThread;
    }

    public void setReservingThread(String reservingThread) {
        this.reservingThread = reservingThread;
    }

    public void reserve(String threadId) {
        this.reserved = true;
        this.reservingThread = threadId;
    }

    public void cancel(String threadId) {
        if (this.reservingThread.equals(threadId)) {
            this.reserved = false;
            this.reservingThread = "";
        }
    }

    public static List<Boolean> getSeatStatus(List<Seat> seats) {
        List<Boolean> statusList = new ArrayList<>();
        for (Seat seat : seats) {
            statusList.add(seat.isReserved());
        }
        return statusList;
    }
}
