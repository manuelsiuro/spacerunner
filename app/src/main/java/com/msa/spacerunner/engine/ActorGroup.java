package com.msa.spacerunner.engine;

public class ActorGroup {

    private GameActor[] _upperLayer;
    private GameActor[] _lowerLayer;
    private int position;

    public int getPosition() {
        return position;
    }

    public GameActor[] getUpperLayer() {
        return _upperLayer;
    }

    public GameActor[] getLowerLayer() {
        return _lowerLayer;
    }

    public ActorGroup(GameActor[] up, GameActor[] floor, int p) {
        this._upperLayer = up;
        this._lowerLayer = floor;
        position = p;
    }
}
