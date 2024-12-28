package net.hearnsoft.animesceneloader.model;

public class Duration {
    private long from;
    private long to;

    public Duration() {}

    public Duration(long from, long to) {
        this.from = from;
        this.to = to;
    }


    public long getFrom() {
        return from;
    }

    public void setFrom(long from) {
        this.from = from;
    }

    public long getTo() {
        return to;
    }

    public void setTo(long to) {
        this.to = to;
    }

    public long getDuration() {
        return to - from;
    }
}
