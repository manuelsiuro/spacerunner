package com.msa.spacerunner;

import com.msa.spacerunner.engine.ActorGroup;
import com.msa.spacerunner.engine.GameActor;

import java.util.ArrayList;

public class GameBoard {

    public enum GameDifficulty {
        EASY,
        MODERATE,
        HARD,
        EXTREME
    }

    public final static int BOARD_WIDTH = 5;

    private ArrayList<ActorGroup> _board;
    private int numOfCoins;
    public String boardName;
    public GameDifficulty difficulty;

    public GameBoard(String name, GameDifficulty diff) {
        boardName = name;
        difficulty = diff;
        _board = new ArrayList<>();
    }

    public GameBoard(GameBoard copy) {
        this.boardName = copy.boardName;
        this.difficulty = copy.difficulty;
        this._board = new ArrayList<ActorGroup>();
        for (ActorGroup ag : copy.getBoard()) {
            GameActor[] upper = new GameActor[BOARD_WIDTH];
            GameActor[] lower = new GameActor[BOARD_WIDTH];
            for (int i = 0; i < BOARD_WIDTH; i++) {
                upper[i] = new GameActor(ag.getUpperLayer()[i].getType(), ag.getUpperLayer()[i].boundingBox3D);
                lower[i] = new GameActor(ag.getLowerLayer()[i].getType(), ag.getLowerLayer()[i].boundingBox3D);
            }
            this._board.add(new ActorGroup(upper, lower, 0));
        }
    }

    public ArrayList<ActorGroup> getBoard() {
        return _board;
    }

    public ActorGroup addActorGroup(GameActor[] up, GameActor[] floor) {
        ActorGroup n = new ActorGroup(up, floor, _board.size() + 1);
        _board.add(n);
        return n;
    }

    public int getSize() {
        return _board.size();
    }

    public int getNumOfCoins() {
        return numOfCoins;
    }

    public void setNumOfCoins(int numOfCoins) {
        this.numOfCoins = numOfCoins;
    }
}
