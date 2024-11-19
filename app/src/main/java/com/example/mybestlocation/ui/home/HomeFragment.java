package com.example.mybestlocation.ui.home;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.mybestlocation.Config;
import com.example.mybestlocation.JSONParser;
import com.example.mybestlocation.Position;
import com.example.mybestlocation.R;
import com.example.mybestlocation.databinding.FragmentHomeBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class HomeFragment extends Fragment {
    private static final int CONTEXT_MENU_OPEN = 1;
    private static final int CONTEXT_MENU_DELETE = 2;

    ArrayList<Position> data = new ArrayList<>();
    ArrayAdapter<Position> adapter;
    ListView listView;
    private FragmentHomeBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        binding.downloadBtn.setOnClickListener(v -> {
            Download d = new Download();
            d.execute();
        });

        listView = binding.listLocations;
        registerForContextMenu(listView);

        listView.setOnItemClickListener((parent, view, position, id) -> view.showContextMenu());

        return root;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(Menu.NONE, CONTEXT_MENU_OPEN, Menu.NONE, "Open in Map");
        menu.add(Menu.NONE, CONTEXT_MENU_DELETE, Menu.NONE, "Delete Position");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        final int position = info.position;

        switch (item.getItemId()) {
            case CONTEXT_MENU_OPEN:
                Bundle bundle = new Bundle();
                bundle.putDouble("latitude", Double.parseDouble(data.get(position).getLatitude()));
                bundle.putDouble("longitude", Double.parseDouble(data.get(position).getLongitude()));
                Navigation.findNavController(requireView()).navigate(R.id.mapFragment, bundle);
                return true;

            case CONTEXT_MENU_DELETE:
                new AlertDialog.Builder(requireContext())
                        .setTitle("Delete Position")
                        .setMessage("Are you sure you want to delete this position?")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            Position positionToDelete = data.get(position);
                            DeletePosition deleteTask = new DeletePosition(positionToDelete);
                            deleteTask.execute();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
                return true;
        }
        return super.onContextItemSelected(item);
    }

    class DeletePosition extends AsyncTask<Void, Void, Boolean> {
        private Position position;

        public DeletePosition(Position position) {
            this.position = position;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                JSONParser jsonParser = new JSONParser();

                // Create parameters for deletion
                HashMap<String, String> params = new HashMap<>();
                params.put("idposition", String.valueOf(position.getId()));

                // Assuming you have a URL for position deletion in your Config class
                JSONObject response = jsonParser.makeHttpRequest(Config.url_delete, "GET", params);

                // Check if deletion was successful
                return response.getInt("success") == 1;
            } catch (JSONException e) {
                Log.e("DeletePosition", "Error deleting position", e);
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                // Remove from local list and update adapter
                data.remove(position);
                adapter.notifyDataSetChanged();
                Toast.makeText(requireContext(), "Position deleted successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), "Failed to delete position", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Existing Download class remains the same as in previous example
    class Download extends AsyncTask<Void, Void, Void> {
        AlertDialog alert;

        @Override
        protected void onPreExecute() {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Download");
            builder.setMessage("Downloading...");
            alert = builder.create();
            alert.show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                Thread.sleep(2000);
                JSONParser parser = new JSONParser();
                JSONObject response = parser.makeRequest(Config.url_getAll);

                int success = response.getInt("success");
                if (success == 1) {
                    // Clear existing data before downloading
                    data.clear();
                    JSONArray positions = response.getJSONArray("positions");
                    for (int i = 0; i < positions.length(); i++) {
                        JSONObject obj = positions.getJSONObject(i);
                        Position p = new Position(
                                obj.getInt("idposition"),
                                obj.getString("pseudo"),
                                obj.getString("longitude"),
                                obj.getString("latitude"),
                                obj.getString("numero")
                        );
                        data.add(p);
                    }
                }
            } catch (JSONException | InterruptedException e) {
                Log.e("Download", "Error downloading positions", e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            alert.dismiss();
            adapter = new ArrayAdapter<>(requireActivity(),
                    android.R.layout.simple_list_item_1, data);
            listView.setAdapter(adapter);
        }
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}