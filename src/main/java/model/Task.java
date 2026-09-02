package model;

public class Task {
    private final String text;
    private final boolean done;

    public Task(String text, boolean done){
        this.done = done;
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public boolean isDone() {
        return done;
    }
}
