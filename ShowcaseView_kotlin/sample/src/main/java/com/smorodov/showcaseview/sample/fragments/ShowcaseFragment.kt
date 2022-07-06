package com.smorodov.showcaseview.sample.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.smorodov.showcaseview.ShowcaseView
import com.smorodov.showcaseview.ShowcaseView.ConfigOptions
import com.smorodov.showcaseview.sample.R

class ShowcaseFragment : Fragment() {
    var sv: ShowcaseView? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val layout = inflater.inflate(R.layout.fragment_layout, container)
        val button = layout.findViewById<Button>(R.id.buttonFragments)
        button.setOnClickListener { v: View? ->
            Toast.makeText(
                activity,
                R.string.it_does_work,
                Toast.LENGTH_LONG
            ).show()
        }
        return layout
    }

    override fun onResume() {
        super.onResume()
        //setContentView() needs to be called in the Activity first.
        //That's why it has to be in onActivityCreated().
        val co = ConfigOptions()
        co.hideOnClickOutside = true
        sv = ShowcaseView.insertShowcaseView(
            R.id.buttonFragments,
            requireActivity(), R.string.showcase_fragment_title,
            R.string.showcase_fragment_message, co
        )
    }
}