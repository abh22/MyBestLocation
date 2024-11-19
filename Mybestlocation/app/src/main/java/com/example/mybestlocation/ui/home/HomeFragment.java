package com.example.mybestlocation.ui.home;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.mybestlocation.Config;
import com.example.mybestlocation.JSONParser;
import com.example.mybestlocation.databinding.FragmentHomeBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        binding.btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Download download = new Download();
                    download.execute();            }
        });


        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    class Download extends AsyncTask {
        // code de second thread
        @Override
        protected Object doInBackground(Object[] objects) {
            JSONParser parser = new JSONParser();
            JSONObject response=parser.makeRequest(Config.url_GETALL);

            try {
                int success= response.getInt("success");
                 Log.e("response","=="+success);
                 if(success==1){
                     JSONArray position=response.getJSONArray("positions");
                     Log.e("response","data = "+position);
                 }


            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
            return null;
        }
    }
}