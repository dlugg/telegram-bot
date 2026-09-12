package model;

public enum RpsGameResult {

    WIN("победа"),
    LOSE("поражение"),
    DRAW("ничья");
    private final String title;

    RpsGameResult(String title){
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
