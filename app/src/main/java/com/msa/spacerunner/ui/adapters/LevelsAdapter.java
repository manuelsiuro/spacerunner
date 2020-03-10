package com.msa.spacerunner.ui.adapters;

import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.msa.spacerunner.GameBoard;
import com.msa.spacerunner.GameData;
import com.msa.spacerunner.R;

import java.util.ArrayList;

public class LevelsAdapter extends RecyclerView.Adapter<LevelsAdapter.LevelsViewHolder> {

    public interface OnGameBoardListener {
        void onGameBoardSelected(GameBoard gameBoard);
    }

    private OnGameBoardListener onGameBoardListener;
    private ArrayList<String> names;

    public LevelsAdapter(OnGameBoardListener listener) {
        GameData.generateAllLevels();
        names = GameData.getGameNames();
        onGameBoardListener = listener;
    }

    static class LevelsViewHolder extends RecyclerView.ViewHolder {

        View itemLevel;
        TextView levelName;
        TextView levelDifficulty;

        LevelsViewHolder(View view) {
            super(view);
            itemLevel = view.findViewById(R.id.item_level);
            levelName = view.findViewById(R.id.level_name);
            levelDifficulty = view.findViewById(R.id.level_difficulty);
        }
    }

    @NonNull
    @Override
    public LevelsAdapter.LevelsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_level, parent, false);
        return new LevelsViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull LevelsAdapter.LevelsViewHolder holder, int i) {
        GameBoard gameBoard = (GameBoard) getItem(i);
        if(gameBoard == null) {
            return;
        }

        holder.levelName.setText(gameBoard.boardName);
        holder.levelDifficulty.setText(String.valueOf(gameBoard.difficulty));
        switch (gameBoard.difficulty) {
            case EASY:
                holder.itemLevel.setBackgroundResource(R.drawable.item_background_easy);
                holder.levelDifficulty.setTextColor(ContextCompat.getColor(holder.levelName.getContext(), R.color.md_green_500));
                break;
            case MODERATE:
                holder.itemLevel.setBackgroundResource(R.drawable.item_background_moderate);
                holder.levelDifficulty.setTextColor(ContextCompat.getColor(holder.levelName.getContext(), R.color.md_yellow_500));
                break;
            case HARD:
                holder.itemLevel.setBackgroundResource(R.drawable.item_background_hard);
                holder.levelDifficulty.setTextColor(ContextCompat.getColor(holder.levelName.getContext(), R.color.md_orange_500));
                break;
            case EXTREME:
                holder.itemLevel.setBackgroundResource(R.drawable.item_background_extreme);
                holder.levelDifficulty.setTextColor(ContextCompat.getColor(holder.levelName.getContext(), R.color.md_red_500));
                break;
        }

        holder.itemLevel.setOnClickListener(v -> onGameBoardListener.onGameBoardSelected(gameBoard));

    }

    private Object getItem(int i) {
        return names != null ? GameData.getGameBoard(names.get(i)) : null;
    }

    @Override
    public int getItemCount() {
        return names.size();
    }
}
