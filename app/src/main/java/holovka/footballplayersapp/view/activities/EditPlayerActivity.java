package holovka.footballplayersapp.view.activities;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.bumptech.glide.Glide;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import holovka.footballplayersapp.R;
import holovka.footballplayersapp.model.entities.FootballPlayer;
import holovka.footballplayersapp.viewmodel.FootballPlayerViewModel;

public class EditPlayerActivity extends AppCompatActivity {

    private EditText editTextName, editTextClub;
    private Spinner spinnerPosition;
    private ImageView imageViewPlayer;
    private Button buttonUpdate, buttonDelete, buttonChangeImage, buttonSelectDate;
    private FootballPlayerViewModel viewModel;
    private FootballPlayer currentPlayer;
    private ActivityResultLauncher<String> pickImageLauncher;

    private static final int REQUEST_IMAGE_CAPTURE = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_player);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        editTextName = findViewById(R.id.editTextPlayerName);
        editTextClub = findViewById(R.id.editTextPlayerClub);
        spinnerPosition = findViewById(R.id.spinnerPosition);
        imageViewPlayer = findViewById(R.id.imageViewPlayer);
        buttonUpdate = findViewById(R.id.button_update);
        buttonDelete = findViewById(R.id.button_delete);
        buttonChangeImage = findViewById(R.id.button_change_image);
        buttonSelectDate = findViewById(R.id.button_select_date);

        viewModel = new ViewModelProvider(this).get(FootballPlayerViewModel.class);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.positions, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPosition.setAdapter(adapter);

        setupPickImageLauncher();
        setupButtonListeners();

        int playerId = getIntent().getIntExtra("playerId", -1);
        if (playerId != -1) {
            viewModel.getPlayerById(playerId).observe(this, player -> {
                if (player != null) {
                    currentPlayer = player;
                    updateUIWithPlayerInfo(player);
                }
            });
        }
    }

    private void setupPickImageLauncher() {
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        Glide.with(EditPlayerActivity.this).load(uri).into(imageViewPlayer);
                        currentPlayer.image = uri.toString();
                    }
                }
        );
    }

    private void setupButtonListeners() {
        buttonChangeImage.setOnClickListener(v -> openImageSelectionOptions());
        buttonUpdate.setOnClickListener(v -> updatePlayer());
        buttonDelete.setOnClickListener(v -> deletePlayer());
        buttonSelectDate.setOnClickListener(v -> showDatePickerDialog());
    }

    private void updateUIWithPlayerInfo(FootballPlayer player) {
        editTextName.setText(player.nameSurname);
        editTextClub.setText(player.club);
        Glide.with(this).load(Uri.parse(player.image)).into(imageViewPlayer);
        String joinDate = player.joinDate;
        buttonSelectDate.setText(joinDate);

        setSpinnerToPosition(player.position);
    }

    private void showDatePickerDialog() {
        Calendar cal = Calendar.getInstance();
        DatePickerDialog.OnDateSetListener dateSetListener = (view, year, month, dayOfMonth) -> {
            cal.set(Calendar.YEAR, year);
            cal.set(Calendar.MONTH, month);
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            String selectedDate = sdf.format(cal.getTime());
            buttonSelectDate.setText(selectedDate);
            currentPlayer.joinDate = selectedDate;
        };
        new DatePickerDialog(EditPlayerActivity.this, dateSetListener, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void openImageSelectionOptions() {
        openCameraForImageCapture();
    }

    private void updatePlayer() {
        currentPlayer.nameSurname = editTextName.getText().toString().trim();
        currentPlayer.club = editTextClub.getText().toString().trim();

        String selectedPosition = spinnerPosition.getSelectedItem().toString();
        currentPlayer.position = selectedPosition;

        Log.d("UpdatePlayer", "Selected position: " + selectedPosition);

        viewModel.update(currentPlayer);

        Toast.makeText(EditPlayerActivity.this, "Player updated successfully", Toast.LENGTH_SHORT).show();

        finish();
    }

    private void deletePlayer() {
        viewModel.delete(currentPlayer);
        Toast.makeText(EditPlayerActivity.this, "Player deleted successfully", Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void openCameraForImageCapture() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } else {
            Toast.makeText(this, "No camera app found", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                handleImageCapture(data);
            }
        }
    }

    private void handleImageCapture(Intent data) {
        if (data != null && data.getExtras() != null) {
            Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");
            if (imageBitmap != null) {
                Uri imageUri = saveImageToGallery(imageBitmap);
                if (imageUri != null) {
                    currentPlayer.image = imageUri.toString();
                    Glide.with(this).load(imageUri).into(imageViewPlayer);
                } else {
                    Toast.makeText(this, "Failed to save the captured image", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Failed to retrieve the captured image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setSpinnerToPosition(String position) {
        ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>) spinnerPosition.getAdapter();
        for (int i = 0; i < adapter.getCount(); i++) {
            if (position.equals(adapter.getItem(i).toString())) {
                spinnerPosition.setSelection(i);
                break;
            }
        }
    }


    private Uri saveImageToGallery(Bitmap imageBitmap) {
        ContentResolver resolver = getContentResolver();

        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, "FootballPlayerImage");
        contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");

        Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

        try {
            OutputStream outputStream = resolver.openOutputStream(imageUri);
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.close();
            return imageUri;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
