package model;

public enum RpsGameResult {

    WIN("побед"),
    LOSE("поражений"),
    DRAW("ничьих");
    private final String title;

    RpsGameResult(String title){
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
