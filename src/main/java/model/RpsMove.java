package model;

public enum RpsMove {
    ROCK("камень"),
    SCISSORS("ножницы"),
    PAPER("бумага");
    private final String title;

    RpsMove(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}

