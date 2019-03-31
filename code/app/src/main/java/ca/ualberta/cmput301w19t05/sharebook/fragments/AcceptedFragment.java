package ca.ualberta.cmput301w19t05.sharebook.fragments;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import ca.ualberta.cmput301w19t05.sharebook.R;
import ca.ualberta.cmput301w19t05.sharebook.activities.BookDetailActivity;
import ca.ualberta.cmput301w19t05.sharebook.activities.MapsActivity;
import ca.ualberta.cmput301w19t05.sharebook.activities.UserProfile;
import ca.ualberta.cmput301w19t05.sharebook.models.Book;
import ca.ualberta.cmput301w19t05.sharebook.models.User;
import ca.ualberta.cmput301w19t05.sharebook.tools.FirebaseHandler;

public class AcceptedFragment extends Fragment {
    private static final String TAG = "acce[ted fragment";
    private FirebaseHandler firebaseHandler;
    private Book book;
    private LocationManager locationManager;
    private String locationProvider;
    private TextView positionView;
    private LatLng currentLocation;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_accepted, container, false);

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //LayoutInflater inflater=getLayoutInflater();
        //View v = inflater.inflate(R.layout.fragment_accepted, null);
        positionView = getActivity().findViewById(R.id.locationView);
        final Context context = this.getContext();
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        System.out.print("created");


        if (getArguments() != null) {
            book = getArguments().getParcelable("book");
        }

        firebaseHandler = new FirebaseHandler(getContext());
        final Context fragment = getContext();
        if (book.getOwner().getUserID().equals(firebaseHandler.getCurrentUser().getUserID())){
            positionView.setText("Click me to add location");
            positionView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(fragment, MapsActivity.class);
                    startActivityForResult(intent, 0x09);
                }
            });
            final TextView requesterName =getActivity().findViewById(R.id.requester);
            firebaseHandler.getMyRef().child("accepted").child(book.getBookId()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot it : dataSnapshot.getChildren()){
                        final User temp = it.getValue(User.class);
                        if (temp!= null){
                            requesterName.setVisibility(View.VISIBLE);
                            requesterName.setText("requester: "+temp.getUsername());
                            requesterName.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent(getContext(), UserProfile.class);
                                    intent.putExtra("owner", temp);
                                    startActivity(intent);
                                }
                            });
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
        else {
            positionView.setText("The owner has not reset location");
        }

        firebaseHandler.getMyRef().child("Location").child(book.getBookId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final Map<String, Double> temp = (Map<String, Double>) dataSnapshot.getValue();
                if (temp != null) {
                    positionView.setText("Latitude: " + temp.get("latitude") + "\nLongitude: " + temp.get("longitude"));
                    Geocoder geocoder = new Geocoder(getActivity());
                    try {
                        List<Address> addresses = geocoder.getFromLocation(temp.get("latitude"), temp.get("longitude"),1);
                        Address obj = addresses.get(0);
                        String res = obj.getAddressLine(0);
                        positionView.setText(res);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (!book.getOwner().getUserID().equals(firebaseHandler.getCurrentUser().getUserID())){
                        positionView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Uri gmmIntentUri = Uri.parse("google.navigation:q="+temp.get("latitude")+","+temp.get("longitude"));
                                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                                mapIntent.setPackage("com.google.android.apps.maps");
                                if (mapIntent.resolveActivity(getActivity().getPackageManager())!=null){
                                    startActivity(mapIntent);
                                }
                            }
                        });

                    }

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });






    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 0x10 && requestCode == 0x09) {
            if (data != null) {
                currentLocation = data.getParcelableExtra("location");
                firebaseHandler.addLocation(book,currentLocation);
            }
        }

    }

}
