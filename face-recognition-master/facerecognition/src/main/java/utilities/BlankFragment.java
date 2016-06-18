package utilities;


        import android.app.Activity;
        import android.content.Context;
        import android.os.Bundle;
        import android.app.Fragment;
        import android.support.v4.app.FragmentActivity;
        import android.view.LayoutInflater;
        import android.view.View;
        import android.view.ViewGroup;

        import com.google.android.gms.maps.CameraUpdateFactory;
        import com.google.android.gms.maps.SupportMapFragment;
        import com.google.android.gms.maps.model.LatLng;
        import com.google.android.gms.maps.model.MarkerOptions;

        import org.opencv.javacv.facerecognition.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class BlankFragment extends Fragment {


    public BlankFragment() {
        // Required empty public constructor
    }

    View rootView;
    private FragmentActivity myContext;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_blank, container, false);

        SupportMapFragment mapFragment = (SupportMapFragment) myContext.getSupportFragmentManager()
                .findFragmentById(R.id.map);

        LatLng sydney = new LatLng(-34, 151);
        mapFragment.getMap().addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mapFragment.getMap().moveCamera(CameraUpdateFactory.newLatLng(sydney));

        return  rootView;


    }

    @Override
    public void onAttach(Activity activity) {
        myContext=(FragmentActivity) activity;
        super.onAttach(activity);
    }
}
