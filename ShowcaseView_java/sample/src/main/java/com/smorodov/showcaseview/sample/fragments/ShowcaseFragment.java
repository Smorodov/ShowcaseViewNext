package com.smorodov.showcaseview.sample.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.smorodov.showcaseview.ShowcaseView;
import com.smorodov.showcaseview.sample.R;

public class ShowcaseFragment extends Fragment {

    ShowcaseView sv;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_layout, container);

        Button button = layout.findViewById(R.id.buttonFragments);
        button.setOnClickListener(v -> Toast.makeText(getActivity(), R.string.it_does_work, Toast.LENGTH_LONG).show());

        return layout;
    }

    @Override
    public void onResume() {
        super.onResume();
        //setContentView() needs to be called in the Activity first.
        //That's why it has to be in onActivityCreated().
        ShowcaseView.ConfigOptions co = new ShowcaseView.ConfigOptions();
        co.hideOnClickOutside = true;
        sv = ShowcaseView.insertShowcaseView(R.id.buttonFragments,
                requireActivity(), R.string.showcase_fragment_title,
                R.string.showcase_fragment_message, co);
    }
}
