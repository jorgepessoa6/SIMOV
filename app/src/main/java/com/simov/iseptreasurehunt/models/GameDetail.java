package com.simov.iseptreasurehunt.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.GeoPoint;

public class GameDetail {

    boolean GameActive;
    GeoPoint marker;
    String name;
    int numberofplayers;
    String qrCode;
    Timestamp startDate;

    public GameDetail() {
    }

    public GameDetail(boolean gameActive, GeoPoint marker, String name, int numberofplayers, String qrCode, Timestamp startDate) {
        GameActive = gameActive;
        this.marker = marker;
        this.name = name;
        this.numberofplayers = numberofplayers;
        this.qrCode = qrCode;
        this.startDate = startDate;
    }

    public boolean isGameActive() {
        return GameActive;
    }

    public void setGameActive(boolean gameActive) {
        GameActive = gameActive;
    }

    public GeoPoint getMarker() {
        return marker;
    }

    public void setMarker(GeoPoint marker) {
        this.marker = marker;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getNumberofplayers() {
        return numberofplayers;
    }

    public void setNumberofplayers(int numberofplayers) {
        this.numberofplayers = numberofplayers;
    }

    public String getQrCode() {
        return qrCode;
    }

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }

    public Timestamp getStartDate() {
        return startDate;
    }

    public void setStartDate(Timestamp startDate) {
        this.startDate = startDate;
    }

    @Override
    public String toString() {
        return "GameDetail{" +
                "GameActive=" + GameActive +
                ", marker=" + marker +
                ", name='" + name + '\'' +
                ", numberofplayers=" + numberofplayers +
                ", qrCode='" + qrCode + '\'' +
                ", startDate=" + startDate +
                '}';
    }
}
