package com.simov.iseptreasurehunt;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

public class MenuActivity extends AppCompatActivity {

    CardView gameCard, leaderboardCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        //Define the Cards
        gameCard = findViewById(R.id.gameCard);
        leaderboardCard = findViewById(R.id.leaderboardCard);

        // Click Listener to the Cards
        gameCard.setOnClickListener(this::onClick);
        leaderboardCard.setOnClickListener(this::onClick);
    }

    @SuppressLint("NonConstantResourceId")
    public void onClick(View view) {
        Intent intent;

        switch (view.getId()) {
            case R.id.gameCard:
                intent = new Intent(this, MapActivity.class);
                startActivity(intent);
                break;
            case R.id.leaderboardCard:
                intent = new Intent(this, LeaderBoardActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }
    }
}