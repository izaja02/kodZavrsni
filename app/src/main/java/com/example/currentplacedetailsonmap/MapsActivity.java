package com.example.currentplacedetailsonmap;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import android.os.RemoteException;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

/*import com.estimote.coresdk.common.requirements.SystemRequirementsChecker;
import com.estimote.coresdk.observation.region.beacon.BeaconRegion;
import com.estimote.coresdk.recognition.packets.Beacon;
import com.estimote.coresdk.service.BeaconManager;*/

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.Region;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;
import java.util.UUID;




/**
 * An activity that displays a map showing the place at the device's current location.
 */
public class MapsActivity extends AppCompatActivity
        implements OnMapReadyCallback, BlankFragment.OnFragmentInteractionListener, BeaconConsumer, MonitorNotifier, LocationListener {


    //fragment
    private FrameLayout fragmentContainer;
    private EditText editText;
    private Button button;


    Context context;// checkGps

    LocationManager locationManager;
    boolean GpsStatus;


    private static final String TAG = MapsActivity.class.getSimpleName();
    private GoogleMap mMap;
    private CameraPosition mCameraPosition;


    // The entry point to the Places API.
    private PlacesClient mPlacesClient;

    // The entry point to the Fused Location Provider.
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private LocationRequest mLocationRequest;
    private LocationCallback locationCallback;
    private LatLng destinationLocation;

    private List<String> myList = new ArrayList<>();
    private List<String> destinationStopBuses = new ArrayList<>();
    private List<String> userClosestStationBuses = new ArrayList<>();
    private List<String> allClosestStations = new ArrayList<>();
    private List<String> queryResults = new ArrayList<>();
    private List<String> commonBusResults = new ArrayList<>();

    // A default location (Sydney, Australia) and default zoom to use when location permission is
    // not granted.
    private final LatLng mDefaultLocation = new LatLng(-33.8523341, 151.2106085);
    private static final int DEFAULT_ZOOM = 18;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean mLocationPermissionGranted;


    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private Location mLastKnownLocation;
    private LatLng destinationLatLng;

    // Keys for storing activity state.
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";

    // Used for selecting the current place.
    private static final int M_MAX_ENTRIES = 5;
    private String[] mLikelyPlaceNames;
    private String[] mLikelyPlaceAddresses;
    private List[] mLikelyPlaceAttributions;
    private LatLng[] mLikelyPlaceLatLngs;

    //widgets

    private String mSearchText;
    private ImageView mGps;

    FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    private BeaconManager beaconManager;
    private  String uuidNum="b9407f30-f5f8-466e-aff9-25556b57fe6d";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        context = getApplicationContext();// za checkGps


        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));
        beaconManager.bind((BeaconConsumer) this);


        // Retrieve location and camera position from saved instance state.
        if (savedInstanceState != null) {
            mLastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }

        // Retrieve the content view that renders the map.
        setContentView(R.layout.activity_maps);


        // Construct a PlacesClient
        Places.initialize(getApplicationContext(), getString(R.string.google_maps_key));
        mPlacesClient = Places.createClient(this);


        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        //za update lokacije


        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(3000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationCallback = new LocationCallback() {


            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

                //spremamo lokaciju
                if (mLocationPermissionGranted) {
                    Location location = locationResult.getLastLocation();

                    SaveUserDatabase(location);


                }


            }
        };

        mFusedLocationProviderClient.requestLocationUpdates(mLocationRequest, locationCallback,
                null);


        // Build the map.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mGps = (ImageView) findViewById(R.id.ic_gps);


        //----BlankFragment-----
      //  fragmentContainer = (FrameLayout) findViewById(R.id.fragment_container);

      //  button = (Button) findViewById(R.id.button);
     //   button.setOnClickListener(new View.OnClickListener() {
     //       @Override
     //       public void onClick(View v) {

     //           openFragment();
           // }
      //  });


        CheckGpsStatus();

        //spremili smo u bazu stanice

        SaveBusStopDatabase();

        mGps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: clicked gps icon");
                getDeviceLocation();

            }
        });

        myList = new ArrayList<>();
        destinationStopBuses = new ArrayList<>();
        userClosestStationBuses = new ArrayList<>();
        allClosestStations = new ArrayList<>();
        queryResults = new ArrayList<>();
        commonBusResults = new ArrayList<>();

        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        // Specify the types of place data to return.
        autocompleteFragment.setTypeFilter(TypeFilter.ESTABLISHMENT);


        autocompleteFragment.setLocationBias(RectangularBounds.newInstance(

                new LatLng(43.4434, 16.6929),
                new LatLng(43.5423, 16.4920)));


        autocompleteFragment.setCountry("HR");

        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME));
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                Log.i(TAG, "Place:" + place.getName() + "," + place.getId());

                mSearchText = place.getName();


                geoLocate();
            }

            @Override
            public void onError(@NonNull Status status) {
                Log.i(TAG, "An error occurred: " + status);

            }
        });


    this.updateSpeed(null);





} //kraj OnCreate()


    public void openFragment() {
        BlankFragment fragment = BlankFragment.newInstance(null);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(R.anim.enter_to_up, R.anim.exit_to_down,
                R.anim.enter_to_up, R.anim.exit_to_down);
        transaction.addToBackStack(null);
        transaction.add(R.id.fragment_container, fragment, "BLANK_FRAGMENT").commit();
    }

    @Override
    public void onFragmentInteraction() {

        onBackPressed();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.current_place_menu, menu);


        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sign_out:
                signOut();

        }

        return super.onOptionsItemSelected(item);
    }

    private void signOut() {

        FirebaseAuth.getInstance().signOut();
        Intent intoLog = new Intent(MapsActivity.this, Prijava.class);
        startActivity(intoLog);

    }


    public void geoLocate() { //iz onCreate autofragment smo izvukli ime odabrane lokacije i sada
        Log.d(TAG, "geoLocate: geolocating");
        mMap.clear();
        //stopScanningBeacon();


        Geocoder geocoder = new Geocoder(MapsActivity.this);
        List<Address> list = new ArrayList<>();
        try {
            list = geocoder.getFromLocationName(mSearchText, 1);
        } catch (IOException e) {
            Log.e(TAG, "geoLocate: IOException: " + e.getMessage());
        }

        if (list.size() > 0) {
            Address address = list.get(0);


            destinationLocation = new LatLng(address.getLatitude(), address.getLongitude());
            FindClosestDestinationBusStop(destinationLocation);
            //FindClosestUSERBusStop();
            moveCamera(new LatLng(address.getLatitude(), address.getLongitude()), DEFAULT_ZOOM,
                    address.getAddressLine(0));


            Log.d(TAG, "geoLocate: found a location: " + address.toString());


            //  moveCamera(new LatLng(address.getLatitude(), address.getLongitude()), DEFAULT_ZOOM,
            //        address.getAddressLine(0));


        }


    }


    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;

        // Use a custom info window adapter to handle multiple lines of text in the
        // info window contents.
        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            @Override
            // Return null here, so that getInfoContents() is called next.
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                // Inflate the layouts for the info window, title and snippet.
                View infoWindow = getLayoutInflater().inflate(R.layout.custom_info_contents,
                        (FrameLayout) findViewById(R.id.map), false);

                TextView title = infoWindow.findViewById(R.id.title);
                title.setText(marker.getTitle());

                TextView snippet = infoWindow.findViewById(R.id.snippet);
                snippet.setText(marker.getSnippet());

                return infoWindow;
            }
        });

        // Prompt the user for permission.
        getLocationPermission();

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();

        // Get the current location of the device and set the position of the map.
        getDeviceLocation();


    }

    public void CheckGpsStatus() {
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        assert locationManager != null;
        GpsStatus = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (GpsStatus == true) {

            return;

        } else {

            GpsDialog exampleDialog = new GpsDialog();
            exampleDialog.show(getSupportFragmentManager(), "Gps dialog");

        }
    }


    /**
     * Gets the current location of the device, and positions the map's camera.
     */
    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (mLocationPermissionGranted) {
                Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            mLastKnownLocation = task.getResult();

                            if (mLastKnownLocation != null) {
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                        new LatLng(mLastKnownLocation.getLatitude(),
                                                mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                            }
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                            mMap.moveCamera(CameraUpdateFactory
                                    .newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                            mMap.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void moveCamera(LatLng latLng, float zoom, String title) {
        Log.d(TAG,
                "moveCamera: moving the camera to: lat: " + latLng.latitude + ", lng: " + latLng.longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));

        mMap.clear();

        if (!title.equals("My Location")) {
            MarkerOptions options = new MarkerOptions()
                    .position(latLng)
                    .title(title);
            mMap.addMarker(options);
        }

        hideSoftKeyboard();

    }


    /**
     * Prompts the user for permission to use the device location.
     */
    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    /**
     * Handles the result of the request for location permissions.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                    doStuff();
                }
            }
        }
        updateLocationUI();
    }


    private void SaveBusStopDatabase() {

        DatabaseReference stop = FirebaseDatabase.getInstance().getReference("BusStop");
        GeoFire geoFire = new GeoFire(stop);


        myList.add("8");
        myList.add("9");
        myList.add("13");
        geoFire.setLocation("1", new GeoLocation(43.514282, 16.443449));
        stop.child("1").child("Buses").setValue(myList);
        myList.clear();

        myList.add("7");
        myList.add("9");
        myList.add("1");
        geoFire.setLocation("2", new GeoLocation(43.511341, 16.454626));
        stop.child("2").child("Buses").setValue(myList);
        myList.clear();


        myList.add("5");
        myList.add("3");
        myList.add("11");
        geoFire.setLocation("3", new GeoLocation(43.5105, 16.480027));
        stop.child("3").child("Buses").setValue(myList);
        myList.clear();


        myList.add("8");
        myList.add("16");
        myList.add("9");
        geoFire.setLocation("4", new GeoLocation(43.505303, 16.457243));
        stop.child("4").child("Buses").setValue(myList);
        myList.clear();


        myList.add("7");
        myList.add("5");
        myList.add("27");
        geoFire.setLocation("5", new GeoLocation(43.518971, 16.476350));
        stop.child("5").child("Buses").setValue(myList);
        myList.clear();


    }

    public List<String> getBusList(String key) {
        final List<String> myListy = new ArrayList<String>();

        DatabaseReference reference =
                FirebaseDatabase.getInstance().getReference("BusStop").child(key).child("Buses");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists()) {

                    myListy.clear();


                    for (DataSnapshot dss : snapshot.getChildren()) {
                        String busList = dss.getValue(String.class);
                        Log.i(TAG, "<<<<<<<<<<<<<<<<<<Usli smo u DATASNAPSHOT, FindUSERBusStop");
                        myListy.add(busList);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        return myListy;

    }

    private LatLng userLatLng;

    private void SaveUserDatabase(final Location location) {

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference savedId =
                FirebaseDatabase.getInstance().getReference("Users").child(userId);
        GeoFire geoFire = new GeoFire(savedId);
        geoFire.setLocation("lokacija", new GeoLocation(location.getLatitude(),
                location.getLongitude()));


        //uzimamo koordinate korisnika
        geoFire.getLocation("lokacija", new com.firebase.geofire.LocationCallback() {
            @Override
            public void onLocationResult(String key, GeoLocation mlocation) {
                if (mlocation != null) {

                    userLatLng = new LatLng(mlocation.latitude, mlocation.longitude);
                    System.out.println(String.format("The location for key %s is [%f,%f]", key,
                            mlocation.latitude, mlocation.longitude));


                } else {
                    System.out.println(String.format("There is no location for key %s in GeoFire"
                            , key));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    private double radius1 = 0.1;
    private double radius2 = 0.01;
    private boolean stopFound = false;
    private String stopNumber;
    private boolean stopUserFound = false;
    private String stopUserNumber;
    private boolean isFinished = false;


    private void FindClosestDestinationBusStop(LatLng latLng) {

        destinationStopBuses.clear();

        DatabaseReference find = FirebaseDatabase.getInstance().getReference().child("BusStop");
        GeoFire geoFire = new GeoFire(find);

        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(latLng.latitude,
                latLng.longitude), radius1);
        geoQuery.removeAllListeners();


        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {

                Log.i(TAG, "<<<<<<<<<<<<<<<<<<Usli smo u onKeyEntered, FindClosestDestinationBusStop");


                // destinationStopBuses = getBusList(key);


                DatabaseReference reference =
                        FirebaseDatabase.getInstance().getReference("BusStop").child(key).child("Buses");
                reference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if (snapshot.exists()) {

                            destinationStopBuses.clear();


                            for (DataSnapshot dss : snapshot.getChildren()) {
                                String busList = dss.getValue(String.class);
                                Log.i(TAG, "<<<<<<<<<<<<<<<<<<Usli smo u DATASNAPSHOT, FindClosestDestinationBusStop");
                                destinationStopBuses.add(busList);


                            }

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


                stopFound = true;
                stopNumber = key;


                String title = "odredisna stanica je" + stopNumber;


                LatLng latLng1 = new LatLng(location.latitude, location.longitude);

                MarkerOptions options = new MarkerOptions()
                        .position(latLng1)
                        .title(title);
                mMap.addMarker(options);


                System.out.println(String.format("broj stanice je %s", stopNumber));


            }


            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                if (!stopFound) {

                    radius1 = radius1 + 0.2;
                    FindClosestDestinationBusStop(destinationLocation);


                } else {

                   // startToScanBeacon();
                    stopFound = false;

                    radius1 = 0.1;
                    Log.i(TAG, "<<<<<<<<<<<<<<<<<<Usli smo u GeoQueryReady, FindClosestDestinationBusStop");
                    FindClosestUSERBusStop();


                }


            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });


    }


    private void FindClosestUSERBusStop() {

        userClosestStationBuses.clear();
        commonBusResults.clear();

        DatabaseReference find1 = FirebaseDatabase.getInstance().getReference().child("BusStop");
        GeoFire geoFire = new GeoFire(find1);
        ;


        GeoQuery geoQuery1 = geoFire.queryAtLocation(new GeoLocation(43.518547, 16.457858), radius2);
        geoQuery1.removeAllListeners();

        geoQuery1.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(final String key, final GeoLocation location) {

                Log.i(TAG, "<<<<<<<<<<<<<<<<<<Usli smo u onKeyEntered, FindUSERBusStop");


                final DatabaseReference reference1 =
                        FirebaseDatabase.getInstance().getReference("BusStop").child(key).child("Buses");
                // reference1.removeEventListener(this);
                reference1.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if (snapshot.exists()) {

                            userClosestStationBuses.clear();


                            for (DataSnapshot dss : snapshot.getChildren()) {
                                String busList = dss.getValue(String.class);
                                Log.i(TAG, "<<<<<<<<<<<<<<<<<<Usli smo u DATASNAPSHOT, FindUSERBusStop");
                                userClosestStationBuses.add(busList);


                            }
                            isFinished = true;
                            if (isFinished = true) {
                                reference1.removeEventListener(this);
                            }

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                for (int i = 0; i < destinationStopBuses.size(); i++) {
                    for (int j = 0; j < userClosestStationBuses.size(); j++) {

                        Log.i(TAG, "<<<<<<<<<<<<<<<<<<TESTIRAMO, FindUSERBusStop");


                        if (userClosestStationBuses.get(j).equals(destinationStopBuses.get(i))) {
                            commonBusResults.add(userClosestStationBuses.get(j));
                        }


                    }


                }
                if (commonBusResults.size() > 0) {
                    stopUserFound = true;
                    stopUserNumber = key;

                    String title = "vama najbliža stanica je: " + stopUserNumber;


                    LatLng latLng1 = new LatLng(location.latitude, location.longitude);

                    MarkerOptions options = new MarkerOptions()
                            .position(latLng1)
                            .title(title);
                    mMap.addMarker(options);


                    System.out.println(String.format("broj stanice je %s",
                            stopUserNumber));
                    commonBusResults.clear();


                } else if (commonBusResults.size() == 0) {
                    Log.i(TAG, "<<<<<<<<<<<<<<<<<<OVA STANICA NEMA ZAJEDNIČKIH BUSEVA, FindUSERBusStop");
                }


            }


            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                if (!stopUserFound) {

                    Log.i(TAG, "<<<<<<<<<<<<<<<<<<Usli smo uGeoQueryReady ALI MORAMO OPET, FindUSERBusStop");

                    radius2 = radius2 + 0.01;
                    FindClosestUSERBusStop();
                } else {


                    stopUserFound = false;
                    radius2 = 0.01;
                    Log.i(TAG, "<<<<<<<<<<<<<<<<<<Usli smo uGeoQueryReady, FindUSERBusStop");

                }


            }


            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });


    }









    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                // init();
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mLastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void hideSoftKeyboard() {
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }




    private void stopScanningBeacon() {

            deregisterBeaconToBeMonitored(UuidProvider.beaconBusStopToBeMonitored());
            BeaconManager.getInstanceForApplication(this).removeMonitorNotifier(this);
            BeaconManager.getInstanceForApplication(this).unbind(this);

    }

    private void startToScanBeacon() {
        BeaconManager beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.addMonitorNotifier(this);
        beaconManager.bind(this);

    }


    @Override
    public void onBeaconServiceConnect() {

        Log.i(TAG, "<<<<<<<<<<<<<<<<<<SCAN IS READY");

        scanBusStopBeacon();

    }

    private void scanBusStopBeacon() {
        registerBeaconToBeMonitored(UuidProvider.beaconBusStopToBeMonitored());
    }
    private void scanBusesBeacon() {
        registerBeaconToBeMonitored(UuidProvider.beaconBusesToBeMonitored());
    }
    //private String stop_uuidNum= "b9407f30-f5f8-466e-aff9-25556b57fe6d:1:"+stopNumber;
    //private String bus_uuidNum= "b9407f30-f5f8-466e-aff9-25556b57fe6d:2:"+destinationStopBuses.get(0);


    @Override
    public void didEnterRegion(Region region) {

        String stop_uuidNum= "b9407f30-f5f8-466e-aff9-25556b57fe6d:1:"+stopUserNumber;
       String bus_uuidNum= "b9407f30-f5f8-466e-aff9-25556b57fe6d:2:"+destinationStopBuses.get(0);


       if(region.getUniqueId().equals("b9407f30-f5f8-466e-aff9-25556b57fe6d:1:"+stopUserNumber)){
           Toast.makeText(MapsActivity.this,"Blizu ste polazišne stanice stanice: "+ region.getId3().toString()+"Molimo vas, pratite autobuse", Toast.LENGTH_LONG).show();
           deregisterBeaconToBeMonitored(UuidProvider.beaconBusStopToBeMonitored());
           scanBusesBeacon();
       }
       for(int i=0; i<commonBusResults.size(); i++){
       if (region.getUniqueId().equals("b9407f30-f5f8-466e-aff9-25556b57fe6d:2:"+commonBusResults.get(i))){

           Toast.makeText(MapsActivity.this,"U blizini je autobus koji vodi ka odredištu: "+ region.getId3().toString(), Toast.LENGTH_LONG).show();
           if (nCurrentSpeed>=30){
               deregisterBeaconToBeMonitored(UuidProvider.beaconBusesToBeMonitored());
               registerBeaconToBeMonitored(UuidProvider.beaconBusesToBeMonitored());
               scanBusStopBeacon();
               Toast.makeText(MapsActivity.this,"Prolazite pored stanice broj "+ region.getId3().toString(), Toast.LENGTH_LONG).show();
               if(region.getUniqueId().equals("b9407f30-f5f8-466e-aff9-25556b57fe6d:1:"+stopNumber)){
                   Toast.makeText(MapsActivity.this,"u blizini ste odredišne stanice! Pripremite se za izlazak." , Toast.LENGTH_LONG).show();
                   mMap.clear();
                   return;




               }

           }


    }}}

    @Override
    public void didExitRegion(Region region) {
        Toast.makeText(MapsActivity.this,"IZAŠLI STE IZ REGIJE"+ region.getUniqueId(), Toast.LENGTH_LONG).show();

    }

    @Override
    public void didDetermineStateForRegion(int state, Region region) {

    }





    private void registerBeaconToBeMonitored(List<String> beacons) {
        try {
            BeaconManager beaconManager = BeaconManager.getInstanceForApplication(this);
            for (String beacon : beacons) {


                Toast.makeText(MapsActivity.this,"PRATIMO BEACON "+  beacon, Toast.LENGTH_LONG).show();

                beaconManager.startMonitoringBeaconsInRegion(UuidMapper.constructRegion(beacon));}

        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }

    }

    private void deregisterBeaconToBeMonitored(List<String> beacons) {
        try {
            BeaconManager beaconManager = BeaconManager.getInstanceForApplication(this);
            for (String beacon : beacons) {

                Toast.makeText(MapsActivity.this,"PRESTALI SMO PRATITI BEACON"+  beacon, Toast.LENGTH_LONG).show();

                beaconManager.stopMonitoringBeaconsInRegion(UuidMapper.constructRegion(beacon));
            }
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }

    }
@SuppressLint("MissingPermission")
private void doStuff() {


    LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
    if (locationManager != null) {
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
    }

}
   private float nCurrentSpeed=0;
private void updateSpeed(CLocation location){

       // float nCurrentSpeed=0;
        if(location!=null){

            location.setUseMetricUnits(this.useMetricUnits());
            nCurrentSpeed=location.getSpeed();

            Formatter fmt= new Formatter(new StringBuilder());
            fmt.format(Locale.US, "%S.lf", nCurrentSpeed);
            String strCurrentSpeed=fmt.toString();
            strCurrentSpeed=strCurrentSpeed.replace(" ","0") + "km/h";

            Toast.makeText(MapsActivity.this,"************************* BRZINA JE:"+ strCurrentSpeed, Toast.LENGTH_LONG).show();

           // Log.i(TAG, "************************* BRZINA JE:"+ strCurrentSpeed);


        }



}
private boolean useMetricUnits(){

        return true;
}

    @Override
    public void onLocationChanged(Location location) {
        if(location!=null){

            CLocation myLocation= new CLocation(location, this.useMetricUnits());
            this.updateSpeed(myLocation);
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
//-------------------------------------------------------------------------------------------------------------------







