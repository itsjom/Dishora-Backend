package com.example.dishora.defaultUI.inboxTab;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.dishora.R;
import com.example.dishora.defaultUI.inboxTab.chatTab.ChatTabFragment;

public class InboxFragment extends Fragment {

    Button btnChat, btnTrash;

    public InboxFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_inbox, container, false);

        btnChat = view.findViewById(R.id.chatButton);
        btnTrash = view.findViewById(R.id.trashButton);

        // ✅ Show ChatFragment by default
        if (savedInstanceState == null) {
            getChildFragmentManager()
                    .beginTransaction()
                    .replace(R.id.tabContentContainer, new ChatTabFragment())
                    .commit();
        }

        // 👇 Set up tab click listeners
        btnChat.setOnClickListener(v -> {
            getChildFragmentManager()
                    .beginTransaction()
                    .replace(R.id.tabContentContainer, new ChatTabFragment())
                    .commit();
        });

//        btnTrash.setOnClickListener(v -> {
//            getChildFragmentManager()
//                    .beginTransaction()
//                    .replace(R.id.tabContentContainer, new TrashFragment())
//                    .commit();
//        });


        return view;
    }
}