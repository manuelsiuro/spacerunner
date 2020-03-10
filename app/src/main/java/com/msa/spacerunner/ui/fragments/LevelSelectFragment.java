package com.msa.spacerunner.ui.fragments;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.msa.spacerunner.GameBoard;
import com.msa.spacerunner.GameConstants;
import com.msa.spacerunner.R;
import com.msa.spacerunner.ui.activities.GameActivity;
import com.msa.spacerunner.ui.activities.MainActivity;
import com.msa.spacerunner.ui.adapters.LevelsAdapter;

public class LevelSelectFragment extends Fragment implements LevelsAdapter.OnGameBoardListener {

    private Context context;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_level_select, container, false);

        context = view.getContext();

        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(getActivity());
        mLinearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        RecyclerView recyclerView = view.findViewById(R.id.list_level);
        recyclerView.setLayoutManager(mLinearLayoutManager);

        LevelsAdapter adapter = new LevelsAdapter(this);
        recyclerView.setAdapter(adapter);

        view.findViewById(R.id.btnBack).setOnClickListener(v -> LevelSelectFragment.this.getFragmentManager().popBackStack());

        return view;
    }

    @Override
    public void onGameBoardSelected(GameBoard gameBoard) {

        Intent intent = new Intent(context, GameActivity.class);
        intent.putExtra(GameConstants.GAME_NAME, gameBoard.boardName);
        intent.putExtra(GameConstants.GAME_SOUND_OFF, MainActivity.soundOff);
        intent.putExtra(GameConstants.GAME_MUSIC_OFF, MainActivity.musicOff);
        startActivity(intent);
    }
}
