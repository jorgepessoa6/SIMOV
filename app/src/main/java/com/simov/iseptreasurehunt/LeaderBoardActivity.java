package com.simov.iseptreasurehunt;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.WindowManager;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.simov.iseptreasurehunt.adapters.LeaderBoardAdapter;
import com.simov.iseptreasurehunt.models.LeaderBoardItem;

public class LeaderBoardActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    FirebaseFirestore fStore;
    LeaderBoardAdapter leaderBoardAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppThemeList);
        setContentView(R.layout.activity_leader_board);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Leaderboard");
        }

        fStore = FirebaseFirestore.getInstance();

        recyclerView = (RecyclerView) findViewById(R.id.leaderboard);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        FirestoreRecyclerOptions<LeaderBoardItem> options =
                new FirestoreRecyclerOptions.Builder<LeaderBoardItem>()
                        .setQuery(fStore.collection("leaderboard").orderBy("Points", Query.Direction.DESCENDING), LeaderBoardItem.class)
                        .build();

        leaderBoardAdapter = new LeaderBoardAdapter(options);
        recyclerView.setAdapter(leaderBoardAdapter);

    }

    @Override
    protected void onStart() {
        super.onStart();
        leaderBoardAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        leaderBoardAdapter.stopListening();
    }
}