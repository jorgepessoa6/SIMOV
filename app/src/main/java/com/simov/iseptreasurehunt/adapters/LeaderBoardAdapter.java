package com.simov.iseptreasurehunt.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.simov.iseptreasurehunt.R;
import com.simov.iseptreasurehunt.models.LeaderBoardItem;

import java.util.Objects;
import java.util.concurrent.Executor;

public class LeaderBoardAdapter extends FirestoreRecyclerAdapter<LeaderBoardItem, LeaderBoardAdapter.LeaderBoardViewHolder> {

    String userName;

    public LeaderBoardAdapter(@NonNull FirestoreRecyclerOptions<LeaderBoardItem> options) {
        super(options);
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onBindViewHolder(@NonNull LeaderBoardViewHolder holder, int position, @NonNull LeaderBoardItem model) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Get the current user
        String currentUser = model.getPlayerId().getId();

        // Retrieve the current user's document from the 'users' collection
        db.collection("users")
                .document(Objects.requireNonNull(currentUser)).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                userName = document.getString("userName");
                                holder.playerName.setText(userName);
                            }
                        }
                    }
                });
        holder.points.setText(Integer.toString(model.getPoints()));
    }

    @NonNull
    @Override
    public LeaderBoardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.leader_board_item, parent, false);
        return new LeaderBoardViewHolder(view);
    }

    static class LeaderBoardViewHolder extends RecyclerView.ViewHolder {

        TextView playerName, points;

        public LeaderBoardViewHolder(@NonNull View itemView) {
            super(itemView);

            playerName = (TextView) itemView.findViewById(R.id.playerName);
            points = (TextView) itemView.findViewById(R.id.playerPoints);

        }
    }
}
