package com.simov.iseptreasurehunt.models;

import com.google.firebase.firestore.DocumentReference;

public class LeaderBoardItem {

    DocumentReference PlayerId;
    int Points;

    LeaderBoardItem() {

    }

    public LeaderBoardItem(DocumentReference playerId, int points) {
        this.PlayerId = playerId;
        this.Points = points;
    }

    public DocumentReference getPlayerId() {
        return PlayerId;
    }

    public void setPlayerId(DocumentReference playerId) {
        this.PlayerId = playerId;
    }

    public int getPoints() {
        return Points;
    }

    public void setPoints(int points) {
        this.Points = points;
    }
}
