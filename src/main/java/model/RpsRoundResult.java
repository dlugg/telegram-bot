package model;

public class RpsRoundResult {
    private final RpsMove computerMove;
    private final RpsGameResult rpsGameResult;
    public RpsRoundResult(RpsMove computerMove, RpsGameResult rpsGameResult){
        this.computerMove = computerMove;
        this.rpsGameResult = rpsGameResult;
    }

    public RpsGameResult getRpsGameResult() {
        return rpsGameResult;
    }

    public RpsMove getComputerMove() {
        return computerMove;
    }
}
