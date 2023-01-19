package com.simov.iseptreasurehunt;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
import android.os.Vibrator;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;
import com.simov.iseptreasurehunt.models.GameDetail;
import com.simov.iseptreasurehunt.models.LeaderBoardItem;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {


    private static final LatLng b1 = new LatLng(41.179601, -8.605439);
    private static final LatLng b2 = new LatLng(41.178782, -8.606082);
    private static final LatLng b3 = new LatLng(41.176934, -8.607954);
    private static final LatLng b4 = new LatLng(41.177339, -8.609225);
    private static final LatLng b5 = new LatLng(41.179565, -8.609310);
    private static final int INITIAL_STROKE_WIDTH_PX = 5;

    private GoogleMap mMap;
    private MapView mapView;
    private View backgroundLayout;
    private FloatingActionButton qrcodescanner;
    private GameDetail gameDetail;
    FirebaseAuth mAuth;
    FirebaseFirestore fStore;
    FirebaseUser mUser;

    CollectionReference leaderboardRef;
    DocumentReference userRef;
    DocumentReference gameRef;
    CollectionReference gameUsers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        mapView = findViewById(R.id.mapView);
        backgroundLayout = findViewById(R.id.view_fade);
        qrcodescanner = findViewById(R.id.opencamera);
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        fStore = FirebaseFirestore.getInstance();

        leaderboardRef = fStore.collection("leaderboard");
        gameUsers = fStore.collection("gameusers");
        userRef = fStore.collection("users").document(mUser.getUid());

        fStore.collection("gamedetails").whereEqualTo("GameActive", true).limit(1).get()
                .addOnSuccessListener(documentSnapshots -> {
                    if (documentSnapshots.isEmpty()) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(MapActivity.this);
                        builder.setMessage("No Active Games Available!");
                        builder.setPositiveButton("Ok", (dialog, which) -> dialog.dismiss()).show();
                        return;
                    } else {
                        // Convert the whole Query Snapshot to a list
                        // of objects directly! No need to fetch each
                        // document.
                        List<GameDetail> types = documentSnapshots.toObjects(GameDetail.class);

                        // Add all to your list
                        gameDetail = types.get(0);
                        for (DocumentSnapshot document : documentSnapshots.getDocuments()) {
                            gameRef = document.getReference();
                        }
                        checkGameParticipants();
                    }
                });

        if (checkAndRequestPermissions()) {
            mapView.getMapAsync(this);
            mapView.onCreate(savedInstanceState);
        }

        qrcodescanner.setOnClickListener(view -> ScanQRCode());

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

        mMap.addPolyline(new PolylineOptions()
                .add(b1, b2, b3, b4, b5, b1)
                .width(INITIAL_STROKE_WIDTH_PX)
                .color(Color.BLUE)
                .geodesic(true)
                .clickable(false));

        LatLng isep = new LatLng(41.177963, -8.608283);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(isep));

        googleMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                        this, R.raw.map_layout));
        mMap.getUiSettings().setZoomControlsEnabled(false);
        mMap.getUiSettings().setZoomGesturesEnabled(false);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setScrollGesturesEnabled(true);
        mMap.getUiSettings().setScrollGesturesEnabledDuringRotateOrZoom(false);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        mMap.setMyLocationEnabled(true);
        mMap.animateCamera(CameraUpdateFactory.zoomTo(18f));
        // Add a marker to the map and move the camera

    }

    @Override
    protected void onResume() {
        super.onResume();

        // Check the distance between the device and the marker every second
        final Handler handler = new Handler();
        final int delay = 1000; // milliseconds

        handler.postDelayed(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            public void run() {
                checkDistanceToMarker();
                handler.postDelayed(this, delay);
            }
        }, delay);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outstate, @NonNull PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outstate, outPersistentState);
        mapView.onSaveInstanceState(outstate);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void checkDistanceToMarker() {
        checkAndRequestPermissions();

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Permissions not granted, return null
            return;
        }
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        long[] pattern = {0, 1000, 500, 1000};
        int repeat = -1;

        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                Location l = new Location("ponto");
                l.setLongitude(gameDetail.getMarker().getLongitude());
                l.setLatitude(gameDetail.getMarker().getLatitude());

                double distance = location.distanceTo(l);
                if (distance < 5 && distance != 0) {
                    backgroundLayout.setBackgroundColor(Color.parseColor("#FF0000"));
                    backgroundLayout.setAlpha(1/(float)distance);
                } else {
                    backgroundLayout.setBackgroundColor(Color.parseColor("#0000FF"));
                    backgroundLayout.setAlpha((float) (0.1 + (distance - 5) * 0.01));
                }
                if(distance < 2){
                    vibrator.vibrate(pattern, repeat);
                }else{
                    vibrator.cancel();
                }
                mapView.setAlpha(1f);
                mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(),location.getLongitude())));
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };

        // Register the listener with the Location Manager to receive location updates
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

    }

    public boolean checkAndRequestPermissions() {
        int internet = ContextCompat.checkSelfPermission(this,
                Manifest.permission.INTERNET);
        int loc = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION);
        int loc2 = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        List<String> listPermissionsNeeded = new ArrayList<>();

        if (internet != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.INTERNET);
        }
        if (loc != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }
        if (loc2 != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray
                    (new String[listPermissionsNeeded.size()]), 1);
            return false;
        }
        return true;
    }


    public void checkGameParticipants(){
        gameUsers.whereEqualTo("GameId", gameRef).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                QuerySnapshot snapshot = task.getResult();
                if (snapshot.isEmpty()) {
                    validateGameEntrance();
                } else {
                    int participants = snapshot.getDocuments().size();
                    if(participants >= gameDetail.getNumberofplayers()) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(MapActivity.this);
                        builder.setMessage("Event is already full!");
                        builder.setPositiveButton("Ok", (dialog, which) -> {
                            dialog.dismiss();
                            sendUserToMenuActivity();
                        }).show();
                    }else{
                        validateGameEntrance();
                    }
                }
            }
        });
    }

    public void validateGameEntrance(){
        gameUsers.whereEqualTo("PlayerId", userRef).whereEqualTo("GameId", gameRef).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                QuerySnapshot snapshot = task.getResult();
                if (snapshot.isEmpty()) {
                    Map<String, Object> guitem = new HashMap<>();
                    guitem.put("PlayerId", userRef);
                    guitem.put("GameId", gameRef);

                    // Add a new document with a generated ID
                    fStore.collection("gameusers")
                            .add(guitem)
                            .addOnSuccessListener(documentReference -> {
                            })
                            .addOnFailureListener(e -> {
                            });
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MapActivity.this);
                    builder.setMessage("Can't participate again!");
                    builder.setPositiveButton("Ok", (dialog, which) ->{ dialog.dismiss();sendUserToMenuActivity();}).show();
                }
            }
        });
    }

    public void updateUserLeaderBoardDocReference(int points){
        leaderboardRef.whereEqualTo("PlayerId", userRef).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                QuerySnapshot snapshot = task.getResult();
                if (snapshot.isEmpty()) {
                    Map<String, Object> lditem = new HashMap<>();
                    lditem.put("PlayerId", userRef);
                    lditem.put("Points", points);

                    // Add a new document with a generated ID
                    fStore.collection("leaderboard")
                            .add(lditem)
                            .addOnSuccessListener(documentReference -> {
                            })
                            .addOnFailureListener(e -> {
                            });
                } else {
                    for (DocumentSnapshot document : snapshot.getDocuments()) {
                        DocumentReference docRef1 = document.getReference();
                        docRef1.update("Points", FieldValue.increment(points));
                    }
                }
            }
        });
    }

    private void ScanQRCode() {
        ScanOptions options = new ScanOptions();
        options.setPrompt("Volume up to flash on");
        options.setBeepEnabled(false);
        options.setCaptureActivity(QrCodeScanner.class);
        barLauncher.launch(options);
    }

    ActivityResultLauncher<ScanOptions> barLauncher = registerForActivityResult(new ScanContract(), result -> {
        if(result.getContents() != null){
            String code = gameDetail.getQrCode();
            if(result.getContents().equals(code)) {
                Timestamp timestamp = new Timestamp(new Date());
                long milliseconds = timestamp.toDate().getTime() - gameDetail.getStartDate().toDate().getTime();
                long minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds);
                int points = 1000 - ((int) minutes);
                if(points < 0)
                    points = 100;
                int finalPoints = points;

                updateUserLeaderBoardDocReference(finalPoints);

                AlertDialog.Builder builder = new AlertDialog.Builder(MapActivity.this);
                builder.setMessage("Congratulations!\n You won " + points + " points");
                builder.setPositiveButton("Ok", (dialog, which) ->{ dialog.dismiss();sendUserToMenuActivity();}).show();

            }else{
                AlertDialog.Builder builder = new AlertDialog.Builder(MapActivity.this);
                builder.setMessage("Invalid QRCode");
                builder.setPositiveButton("Ok", (dialog, which) -> dialog.dismiss()).show();
            }
        }
    });

    private void sendUserToMenuActivity() {
        Intent intent = new Intent(MapActivity.this, MenuActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}