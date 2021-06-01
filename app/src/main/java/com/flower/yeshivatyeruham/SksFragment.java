package com.flower.yeshivatyeruham;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;


import com.bumptech.glide.Glide;

import static com.flower.yeshivatyeruham.DataClass.WIFINAME;
import static com.flower.yeshivatyeruham.DataClass.studentRoot;
import static com.flower.yeshivatyeruham.DataClass.sksRoot;

/**
 * fragment holds the sks buttons - sks, student, downloads, favorites
 */
public class SksFragment extends Fragment {
    private OnFragmentInteractionListener mListener;

    ImageButton sksBtn, netBtn, downloadsBtn, favoritesBtn;
    View rootView;
    Context ctx;

    public SksFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ctx = getContext();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_sks, container, false);

        sksBtn = (ImageButton) rootView.findViewById(R.id.sks_btn);
        netBtn = (ImageButton) rootView.findViewById(R.id.net_btn);
        downloadsBtn = (ImageButton) rootView.findViewById(R.id.downloads_btn);
        favoritesBtn = (ImageButton) rootView.findViewById(R.id.favorites_btn);

        Glide.with(this)
                .load(R.drawable.sks_icon)
                .into(sksBtn);
        Glide.with(this)
                .load(R.drawable.downloads_icon)
                .into(downloadsBtn);
        Glide.with(this)
                .load(R.drawable.net_icon)
                .into(netBtn);
        Glide.with(this)
                .load(R.drawable.favorites_icon)
                .into(favoritesBtn);

        sksBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isOnline()){
                    Intent sksIntent = new Intent(getContext(), SksActivity.class);
                    sksIntent.putExtra("rootPath", sksRoot);
                    startActivity(sksIntent);
                }
            }
        });


        netBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isWIFIAvailable()){
                    Intent mp3Intent = new Intent(getContext(), SksActivity.class);
                    mp3Intent.putExtra("rootPath", studentRoot);
                    startActivity(mp3Intent);
                }
            }
        });

        downloadsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent downloadsIntent = new Intent(getContext(), DownloadsActivity.class);
                startActivity(downloadsIntent);
            }
        });


        favoritesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent favoritesIntent = new Intent(getContext(), FavoritesActivity.class);
                startActivity(favoritesIntent);
            }
        });

        // Inflate the layout for this fragment
        return rootView;
    }

    public boolean isOnline() {
        WifiManager connManager = (WifiManager) ctx.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        //NetworkInfo myWiFi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        WifiInfo mWiFi = connManager.getConnectionInfo();
        if (mWiFi != null && mWiFi.getSSID() != null &&
                (mWiFi.getSSID().contains(WIFINAME.split(",")[0]) ||
                        mWiFi.getSSID().contains(WIFINAME.split(",")[1]))) {
            return true;
        } else {
            ConnectivityManager connectivityManager
                    = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            if(activeNetworkInfo != null && activeNetworkInfo.isConnected())
                return true;
            else {
                Toast.makeText(getActivity(), "לא ניתן להציג, אין חיבור לרשת", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
    }

    public Boolean isWIFIAvailable() {

        ConnectivityManager connManager = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (networkInfo.isConnected()) {
            WifiManager wifiManager = (WifiManager) ctx.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            wifiInfo.getSSID();
            String name = networkInfo.getExtraInfo();
            String ssid = "\"" + wifiInfo.getSSID() + "\"";

            return((ssid.contains(WIFINAME.split(",")[0]) || wifiInfo.getSSID().contains(WIFINAME.split(",")[1])));
        }
        Toast.makeText(getActivity(), "לא ניתן להציג, אין חיבור לרשת הישיבה", Toast.LENGTH_SHORT).show();
        return false;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
