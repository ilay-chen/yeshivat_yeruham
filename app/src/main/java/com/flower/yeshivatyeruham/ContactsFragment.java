package com.flower.yeshivatyeruham;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.File;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ContactsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ContactsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */

/**
 * main fragment. have the contacts buttons, and - donate, preference, update files buttons.
 */
public class ContactsFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    View rootView;
    ImageButton Search, Groups;
    LinearLayout messages, settings, donation, attList;
    ImageView setIco, msgIco;
    private OnFragmentInteractionListener mListener;

    public ContactsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ContactsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ContactsFragment newInstance(String param1, String param2) {
        ContactsFragment fragment = new ContactsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        rootView = inflater.inflate(R.layout.fragment_contacts, container, false);
        Search = (ImageButton) rootView.findViewById(R.id.search_button);
        Groups = (ImageButton) rootView.findViewById(R.id.groups_button);
        messages = (LinearLayout) rootView.findViewById(R.id.messages);
        donation = (LinearLayout) rootView.findViewById(R.id.donation);
        attList=(LinearLayout)rootView.findViewById(R.id.attRegistration);
        settings = (LinearLayout) rootView.findViewById(R.id.settings);
        setIco = (ImageView) rootView.findViewById(R.id.set_icon);
        msgIco = (ImageView) rootView.findViewById(R.id.messages_icon);
        Glide.with(this)
                .load(R.drawable.big_groups_button)
                .fitCenter()
                .dontTransform()
                .into(Groups);

        Glide.with(this)
                .load(R.drawable.big_search_button)
                .fitCenter()
                .dontTransform()
                .into(Search);

        Glide.with(this)
                .load(R.drawable.settings_icon)
                .into(setIco);
        Glide.with(this)
                .load(R.drawable.small_msg_icon)
                .into(msgIco);

        Search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkFileExist()) {
                    Intent search = new Intent(getActivity(), ContactsActivity.class);
                    startActivity(search);
                }
            }
        });


        Groups.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkFileExist()) {
                    Intent groups = new Intent(getActivity(), GroupsActivity.class);
                    startActivity(groups);
                }
            }
        });


        messages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), MessagesActivity.class);
                startActivity(intent);
            }
        });

        settings.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                DataClass dc = (DataClass)getActivity().getApplication();
                dc.updateFile(true, true);
//                ctx.startService(new Intent(ctx, WalkingIconService.class));
                return true;
            }
        });
        attList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), LibraryActivity.class);
                Toast.makeText(getContext(), "הקלד את החיפוש שלך", Toast.LENGTH_SHORT).show();
                startActivity(intent);
            }
        });
//        update.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                DataClass dc = (DataClass)getActivity().getApplication();
//                dc.updateFile(true);
//                //new DataClass().updateFile();
//            }
//        });

        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    Intent settings = new Intent(getActivity(), SettingsActivity.class);
                    startActivity(settings);
            }
        });

        donation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.yhy.co.il/content/view/629/179/lang,he/"));
                startActivity(browserIntent);
            }
        });

        // Inflate the layout for this fragment
        return rootView;
    }

    public Boolean checkFileExist()
    {
        File f = getContext().getFileStreamPath(DataClass.ContactPath);
        if(f == null || !f.exists()){
            Toast.makeText(getContext(), "חסרים קבצים, אנא פתח את האפליקציה כאשר המכשיר מחובר לרשת", Toast.LENGTH_LONG).show(); return false;}
        return true;
    }


    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
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

