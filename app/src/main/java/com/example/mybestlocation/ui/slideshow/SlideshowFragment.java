package com.example.mybestlocation.ui.slideshow;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import com.example.mybestlocation.Config;
import com.example.mybestlocation.JSONParser;
import com.example.mybestlocation.R;
import com.example.mybestlocation.databinding.FragmentSlideshowBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class SlideshowFragment extends Fragment {
    private static final String TAG = "SlideshowFragment";
    private FragmentSlideshowBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSlideshowBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        Button add = binding.addBtn;
        // btn back
        root.findViewById(R.id.back_btn).setOnClickListener(view -> {
            // back to HomeFragment
            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_content_main);
            navController.navigate(R.id.nav_home);
        });
        //  map but
        binding.mapBtn.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_slideshowFragment_to_mapFragment));

        // Check if we got location data from map
        Bundle args = getArguments();
        if (args != null) {
            double latitude = args.getDouble("latitude", 0.0);
            double longitude = args.getDouble("longitude", 0.0);
            if (latitude != 0.0 && longitude != 0.0) {
                binding.textLatitude.setText(String.format("%.6f", latitude));
                binding.textLongitude.setText(String.format("%.6f", longitude));

                // You might want to auto-generate a number or get it from the user
                if (binding.textNumero.getText().toString().isEmpty()) {
                    binding.textNumero.setText(String.valueOf(System.currentTimeMillis() % 10000));
                }

                // Set a default pseudo if empty
                if (binding.textPseudo.getText().toString().isEmpty()) {
                    binding.textPseudo.setText("User_" + (System.currentTimeMillis() % 1000));
                }
            }
        }

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Trim the input values to remove whitespace
                String longitude = binding.textLongitude.getText().toString().trim();
                String latitude = binding.textLatitude.getText().toString().trim();
                String numero = binding.textNumero.getText().toString().trim();
                String pseudo = binding.textPseudo.getText().toString().trim();

                // Log
                Log.d(TAG, "Longitude: " + longitude);
                Log.d(TAG, "Latitude: " + latitude);
                Log.d(TAG, "Numero: " + numero);
                Log.d(TAG, "Pseudo: " + pseudo);

                // Check if any of the fields are empty after trimming
                if (longitude.isEmpty() || latitude.isEmpty() || numero.isEmpty() || pseudo.isEmpty()) {
                    Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                //  validation for numeric values
                try {
                    double longVal = Double.parseDouble(longitude);
                    double latVal = Double.parseDouble(latitude);

                    //  coordinate validation
                    if (longVal < -180 || longVal > 180 || latVal < -90 || latVal > 90) {
                        Toast.makeText(getContext(), "Invalid coordinates", Toast.LENGTH_SHORT).show();
                        return;
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(getContext(), "Invalid coordinate format", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Prepare the data to be uploaded
                HashMap<String, String> params = new HashMap<>();
                params.put("longitude", longitude);
                params.put("latitude", latitude);
                params.put("numero", numero);
                params.put("pseudo", pseudo);

                Upload u = new Upload(params);
                u.execute();
            }
        });
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    AlertDialog alert;

    class Upload extends AsyncTask<Void, Void, JSONObject> {
        HashMap<String, String> params;

        public Upload(HashMap<String, String> params) {
            this.params = params;
        }

        @Override
        protected void onPreExecute() {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Upload");
            builder.setMessage("Uploading...");
            alert = builder.create();
            alert.show();
        }

        @Override
        protected JSONObject doInBackground(Void... voids) {
            try {
                Thread.sleep(500);
                JSONParser parser = new JSONParser();
                // Log the URL and parameters being sent
                Log.d(TAG, "Sending request to URL: " + Config.url_add);
                Log.d(TAG, "Parameters: " + params.toString());
                JSONObject response= parser.makeHttpRequest(Config.url_add, "POST", params);
                Log.d(TAG, "Server response: " + (response != null ? response.toString() : "null"));
                return response;
            } catch (InterruptedException e) {
                Log.e(TAG, "Upload interrupted", e);
                return null;
            } catch (Exception e) {
                Log.e(TAG, "Error during upload", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(JSONObject response) {
            super.onPostExecute(response);
            alert.dismiss();

            if (response != null) {
                try {
                    int success = response.getInt("success");
                    String message = response.optString("message", "Unknown response");
                    Log.d(TAG, "Upload response: success=" + success + ", message=" + message);

                    if (success == 1) {
                        Toast.makeText(getContext(), "Upload successful", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Upload failed: " + message, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "Error parsing response", e);
                    Toast.makeText(getContext(), "Error processing server response", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(), "Upload failed", Toast.LENGTH_SHORT).show();
            }
        }
    }
}