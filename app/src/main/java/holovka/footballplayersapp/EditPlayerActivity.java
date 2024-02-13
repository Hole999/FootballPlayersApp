package holovka.footballplayersapp;

import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;

public class EditPlayerActivity extends AppCompatActivity {

    private EditText editTextName, editTextClub;
    private Spinner spinnerPosition;
    private ImageView imageViewPlayer;
    private FootballPlayerViewModel viewModel;
    private FootballPlayer currentPlayer;

    private ActivityResultLauncher<String> pickImageLauncher;

    private Button buttonChangeImage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_player);


        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        editTextName = findViewById(R.id.editTextPlayerName);
        spinnerPosition = findViewById(R.id.spinnerPosition);
        editTextClub = findViewById(R.id.editTextPlayerClub);
        imageViewPlayer = findViewById(R.id.imageViewPlayer);
        Button buttonUpdate = findViewById(R.id.button_update);
        Button buttonDelete = findViewById(R.id.button_delete);
        buttonChangeImage = findViewById(R.id.button_change_image);
        buttonChangeImage.setOnClickListener(v -> pickImageLauncher.launch("image/*"));


        viewModel = new ViewModelProvider(this).get(FootballPlayerViewModel.class);


        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.positions, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPosition.setAdapter(adapter);


        final int playerId = getIntent().getIntExtra("playerId", -1);
        if (playerId == -1) {
            Toast.makeText(this, "Error: player not found.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        viewModel.getPlayerById(playerId).observe(this, player -> {
            if (player != null) {
                currentPlayer = player;
                editTextName.setText(player.nameSurname);
                int spinnerPositionIndex = adapter.getPosition(player.position);
                spinnerPosition.setSelection(spinnerPositionIndex);
                editTextClub.setText(player.club);

                if (player.image != null && !player.image.isEmpty()) {
                    Glide.with(this).load(Uri.parse(player.image)).into(imageViewPlayer);
                }
            }
        });


        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                new ActivityResultCallback<Uri>() {
                    @Override
                    public void onActivityResult(Uri uri) {
                        if (uri != null) {
                            imageViewPlayer.setImageURI(uri);
                            currentPlayer.image = uri.toString();
                        }
                    }
                }
        );

        buttonUpdate.setOnClickListener(v -> updatePlayer());
        buttonDelete.setOnClickListener(v -> deletePlayer());
        buttonChangeImage.setOnClickListener(v -> pickImageLauncher.launch("image/*"));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updatePlayer() {
        String name = editTextName.getText().toString().trim();
        String position = spinnerPosition.getSelectedItem().toString();
        String club = editTextClub.getText().toString().trim();

        if (name.isEmpty() || position.isEmpty() || club.isEmpty()) {
            Toast.makeText(this, "Please fill out all fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        currentPlayer.nameSurname = name;
        currentPlayer.position = position;
        currentPlayer.club = club;

        viewModel.update(currentPlayer);
        Toast.makeText(this, "Player updated successfully", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void deletePlayer() {
        if (currentPlayer != null) {
            viewModel.delete(currentPlayer);
            Toast.makeText(this, "Player deleted successfully", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Error: No player to delete.", Toast.LENGTH_SHORT).show();
        }
    }


}
