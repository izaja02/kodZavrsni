package com.example.currentplacedetailsonmap;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;

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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

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


import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EventListener;
import java.util.List;


/**
 * An activity that displays a map showing the place at the device's current location.
 */
public class MapsActivity extends AppCompatActivity
        implements OnMapReadyCallback, BlankFragment.OnFragmentInteractionListener {


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
    private List<String> myList2 = new ArrayList<>();
    private List<String> myList3 = new ArrayList<>();
    private List<String> myList4 = new ArrayList<>();
    private List<String> myList5 = new ArrayList<>();
    private List<String> myList6 = new ArrayList<>();

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        context = getApplicationContext();// za checkGps



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


        mLocationRequest=new LocationRequest();
        mLocationRequest.setInterval(3000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationCallback= new LocationCallback(){


            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

                //spremamo lokaciju
                if(mLocationPermissionGranted){
                Location location= locationResult.getLastLocation();

                SaveUserDatabase(location);


                }




            }
        };

        mFusedLocationProviderClient.requestLocationUpdates(mLocationRequest, locationCallback, null );




        // Build the map.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mGps = (ImageView) findViewById(R.id.ic_gps);


        //----BlankFragment-----
        fragmentContainer = (FrameLayout) findViewById(R.id.fragment_container);

        button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                openFragment();
            }
        });


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

        myList= new ArrayList<>();
        myList2= new ArrayList<>();
        myList3= new ArrayList<>();
        myList4= new ArrayList<>();
        myList5= new ArrayList<>();
        myList6= new ArrayList<>();

        //-----Autocomplete-------
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

                mSearchText= place.getName();



                geoLocate();
            }

            @Override
            public void onError(@NonNull Status status) {
                Log.i(TAG, "An error occurred: " + status);

            }
        });


    } //kraj OnCreate()




    public void openFragment() {
        BlankFragment fragment = BlankFragment.newInstance(null);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(R.anim.enter_to_up, R.anim.exit_to_down, R.anim.enter_to_up, R.anim.exit_to_down);
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



   public void geoLocate() { //iz onCreate autofragment smo izvukli ime odabrane lokacije i sada trazimo koordinate i spremamo u database



        Log.d(TAG, "geoLocate: geolocating");

        Geocoder geocoder = new Geocoder(MapsActivity.this);
        List<Address> list = new ArrayList<>();
        try {
            list = geocoder.getFromLocationName(mSearchText, 1);
        } catch (IOException e) {
            Log.e(TAG, "geoLocate: IOException: " + e.getMessage());
        }

        if (list.size() > 0) {
            Address address = list.get(0);


            destinationLocation= new LatLng(address.getLatitude(),address.getLongitude());
            FindClosestDestinationBusStop(destinationLocation);




            Log.d(TAG, "geoLocate: found a location: " + address.toString());



            mMap.clear();

            moveCamera(new LatLng(address.getLatitude(), address.getLongitude()), DEFAULT_ZOOM,
                    address.getAddressLine(0));


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
        Log.d(TAG, "moveCamera: moving the camera to: lat: " + latLng.latitude + ", lng: " + latLng.longitude);
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
                }
            }
        }
        updateLocationUI();
    }


    private void SaveBusStopDatabase(){

        DatabaseReference stop = FirebaseDatabase.getInstance().getReference("BusStop");
        GeoFire geoFire = new GeoFire(stop);


        myList.add("8");
        myList.add("9");
        myList.add("13");
        geoFire.setLocation("1", new GeoLocation(43.514282,16.443449));
        stop.child("1").child("Buses").setValue(myList);
        myList.clear();

        myList.add("7");
        myList.add("9");
        myList.add("1");
        geoFire.setLocation("2", new GeoLocation(43.511341,16.454626));
        stop.child("2").child("Buses").setValue(myList);
        myList.clear();


        myList.add("5");
        myList.add("3");
        myList.add("11");
        geoFire.setLocation("3", new GeoLocation(43.5105,16.480027));
        stop.child("3").child("Buses").setValue(myList);
        myList.clear();


        myList.add("8");
        myList.add("16");
        myList.add("9");
        geoFire.setLocation("4", new GeoLocation(43.505303,16.457243));
        stop.child("4").child("Buses").setValue(myList);
        myList.clear();


        myList.add("7");
        myList.add("5");
        myList.add("27");
        geoFire.setLocation("5", new GeoLocation(43.518971,16.476350));
        stop.child("4").child("Buses").setValue(myList);
        myList.clear();







    }
    public List<String> getBusList(String key){
        final List<String> myListy = new ArrayList<>();

        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("BusStop").child(key).child("Buses");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    myListy.clear();

                    for (DataSnapshot dss: snapshot.getChildren()){
                        String busList= dss.getValue(String.class);
                        myListy.add(busList);

                    }

                    for(int i = 0; i < myListy.size(); i++) {
                        System.out.println(myListy.get(i));}
                }

            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        return myListy;


    }
    private LatLng userLatLng;

    private void SaveUserDatabase(final Location location){

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference savedId = FirebaseDatabase.getInstance().getReference("Users").child(userId);
       GeoFire geoFire = new GeoFire(savedId);
       geoFire.setLocation("lokacija", new GeoLocation(location.getLatitude(), location.getLongitude()));


        //uzimamo koordinate korisnika
       geoFire.getLocation("lokacija", new com.firebase.geofire.LocationCallback() {
           @Override
           public void onLocationResult(String key, GeoLocation mlocation) {
               if (mlocation != null) {

                    userLatLng = new LatLng(mlocation.latitude, mlocation.longitude);
                   System.out.println(String.format("The location for key %s is [%f,%f]", key, mlocation.latitude, mlocation.longitude));




               } else {
                   System.out.println(String.format("There is no location for key %s in GeoFire", key));
               }
           }

           @Override
           public void onCancelled(DatabaseError databaseError) {

           }
       });


    }
    private double radius1 = 0.1;
    private double radius2 = 0.1;
    private boolean stopFound=false;
    private String stopNumber;
    private boolean stopUserFound=false;
    private String stopUserNumber;






    private void FindClosestDestinationBusStop(LatLng latLng) {
        myList4.clear();

        DatabaseReference find = FirebaseDatabase.getInstance().getReference().child("BusStop");
        GeoFire geoFire = new GeoFire(find);

        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(latLng.latitude, latLng.longitude), radius1);
        geoQuery.removeAllListeners();

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {


               myList4.add(key);


                if (myList4.size()>1){ return;}



                    else {
                    myList2= getBusList(key);
                   stopFound = true;
                   stopNumber = myList4.get(0);


                   String title = "odredisna stanica je" + stopNumber;


                   LatLng latLng1 = new LatLng(location.latitude, location.longitude);

                   MarkerOptions options = new MarkerOptions()
                           .position(latLng1)
                           .title(title);
                   mMap.addMarker(options);


                   System.out.println(String.format("broj stanice je %s", stopNumber));

               }}





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



                }

                FindClosestUSERBusStop();

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });


    }



    private void FindClosestUSERBusStop() {
        myList5.clear();

        DatabaseReference find = FirebaseDatabase.getInstance().getReference().child("BusStop");
        GeoFire geoFire = new GeoFire(find);
        //latLng.latitude, latLng.longitude

        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(43.515449, 16.457858), radius2);
        geoQuery.removeAllListeners();

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
               myList5.add(key);

                if (myList5.size()>1){ return;}

                else {
                    myList3 = getBusList(key);
                    for(int i = 0; i < myList3.size(); i++) {
                        System.out.println(myList3.get(i));
                    }

                    for(int i = 0; i < myList2.size(); i++) {
                        System.out.println(myList2.get(i));
                    }


                    for (int i = 0; i < myList2.size(); i++) {
                        for (int j = 0; j < myList3.size(); j++) {

                            System.out.println(myList2.get(i));
                            System.out.println(myList3.get(j));



                            if (myList3.get(j).equals(myList2.get(i))){
                            myList6.add(myList3.get(j));
                            }


                        }


                    }
                    System.out.println("myList6 elementi: ");

                    for(int i = 0; i < myList6.size(); i++) {
                        System.out.println(myList6.get(i));
                    }


                    if (myList6.size() > 0) {
                        stopUserFound = true;
                        stopUserNumber = key;

                        String title = "vama najbliža stanica je: " + stopUserNumber;


                        LatLng latLng1 = new LatLng(location.latitude, location.longitude);

                        MarkerOptions options = new MarkerOptions()
                                .position(latLng1)
                                .title(title);
                        mMap.addMarker(options);


                        System.out.println(String.format("broj stanice je %s", stopUserNumber));
                        myList6.clear();

                    }
                    else {

                        return;}


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

                    radius2 = radius2 + 0.1;
                    FindClosestUSERBusStop();


                }

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });


    }





    /**
     * Prompts the user to select the current place from a list of likely places, and shows the
     * current place on the map - provided the user has granted location permission.
     */
   /* private void showCurrentPlace() {
        if (mMap == null) {
            return;
        }

        if (mLocationPermissionGranted) {
            // Use fields to define the data types to return.
            List<Place.Field> placeFields = Arrays.asList(Place.Field.NAME, Place.Field.ADDRESS,
                    Place.Field.LAT_LNG);

            // Use the builder to create a FindCurrentPlaceRequest.
            FindCurrentPlaceRequest request =
                    FindCurrentPlaceRequest.newInstance(placeFields);

            // Get the likely places - that is, the businesses and other points of interest that
            // are the best match for the device's current location.


            @SuppressWarnings("MissingPermission") final
            Task<FindCurrentPlaceResponse> placeResult =
                    mPlacesClient.findCurrentPlace(request);
            placeResult.addOnCompleteListener (new OnCompleteListener<FindCurrentPlaceResponse>() {
                @Override
                public void onComplete(@NonNull Task<FindCurrentPlaceResponse> task) {
                    if (task.isSuccessful() && task.getResult() != null) {
                        FindCurrentPlaceResponse likelyPlaces = task.getResult();

                        // Set the count, handling cases where less than 5 entries are returned.
                        int count;
                        if (likelyPlaces.getPlaceLikelihoods().size() < M_MAX_ENTRIES) {
                            count = likelyPlaces.getPlaceLikelihoods().size();
                        } else {
                            count = M_MAX_ENTRIES;
                        }

                        int i = 0;
                        mLikelyPlaceNames = new String[count];
                        mLikelyPlaceAddresses = new String[count];
                        mLikelyPlaceAttributions = new List[count];
                        mLikelyPlaceLatLngs = new LatLng[count];

                        for (PlaceLikelihood placeLikelihood : likelyPlaces.getPlaceLikelihoods()) {
                            // Build a list of likely places to show the user.
                            mLikelyPlaceNames[i] = placeLikelihood.getPlace().getName();
                            mLikelyPlaceAddresses[i] = placeLikelihood.getPlace().getAddress();
                            mLikelyPlaceAttributions[i] = placeLikelihood.getPlace()
                                    .getAttributions();
                            mLikelyPlaceLatLngs[i] = placeLikelihood.getPlace().getLatLng();

                            i++;
                            if (i > (count - 1)) {
                                break;
                            }
                        }

                        // Show a dialog offering the user the list of likely places, and add a
                        // marker at the selected place.
                        MapsActivityCurrentPlace.this.openPlacesDialog();
                    }
                    else {
                        Log.e(TAG, "Exception: %s", task.getException());
                    }
                }
            });
        } else {
            // The user has not granted permission.
            Log.i(TAG, "The user did not grant location permission.");

            // Add a default marker, because the user hasn't selected a place.
            mMap.addMarker(new MarkerOptions()
                    .title(getString(R.string.default_info_title))
                    .position(mDefaultLocation)
                    .snippet(getString(R.string.default_info_snippet)));

            // Prompt the user for permission.
            getLocationPermission();
        }
    }

    /**
     * Displays a form allowing the user to select a place from a list of likely places.
     */
   /* private void openPlacesDialog() {
        // Ask the user to choose the place where they are now.
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // The "which" argument contains the position of the selected item.
                LatLng markerLatLng = mLikelyPlaceLatLngs[which];
                String markerSnippet = mLikelyPlaceAddresses[which];
                if (mLikelyPlaceAttributions[which] != null) {
                    markerSnippet = markerSnippet + "\n" + mLikelyPlaceAttributions[which];
                }

                // Add a marker for the selected place, with an info window
                // showing information about that place.
                mMap.addMarker(new MarkerOptions()
                        .title(mLikelyPlaceNames[which])
                        .position(markerLatLng)
                        .snippet(markerSnippet));

                // Position the map's camera at the location of the marker.
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(markerLatLng,
                        DEFAULT_ZOOM));
            }
        };

        // Display the dialog.
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.pick_place)
                .setItems(mLikelyPlaceNames, listener)
                .show();
    }

    /**
     * Updates the map's UI settings based on whether the user has granted location permission.
     */
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
}

//-------------------------------------------------------------------------------------------------------------------







