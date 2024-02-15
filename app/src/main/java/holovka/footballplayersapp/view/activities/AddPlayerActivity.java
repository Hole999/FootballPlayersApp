package holovka.footballplayersapp.view.activities;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;
import holovka.footballplayersapp.R;
import holovka.footballplayersapp.model.entities.FootballPlayer;
import holovka.footballplayersapp.viewmodel.FootballPlayerViewModel;

public class AddPlayerActivity extends AppCompatActivity {

    private EditText editTextName, editTextClub;
    private Button buttonSelectImage;
    private String imageUriString = "";
    private FootballPlayerViewModel viewModel;

    private ImageView imageViewPlayer;

    private Spinner spinnerPosition;
    private Button buttonSelectDate;
    private String selectedDate = null;

    private static final int PERMISSION_REQUEST_STORAGE = 2;
    private static final int REQUEST_IMAGE_CAPTURE = 3;
    private static final int PERMISSION_REQUEST_CAMERA = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_player);

        buttonSelectImage = findViewById(R.id.button_select_image);
        editTextName = findViewById(R.id.editTextPlayerName);
        editTextClub = findViewById(R.id.editTextPlayerClub);
        spinnerPosition = findViewById(R.id.spinnerPosition);
        buttonSelectDate = findViewById(R.id.button_select_date);
        buttonSelectDate.setOnClickListener(v -> showDatePickerDialog());

        viewModel = new ViewModelProvider(this).get(FootballPlayerViewModel.class);
        imageViewPlayer = findViewById(R.id.imageViewPlayer);

        Button buttonSave = findViewById(R.id.button_save);
        buttonSave.setOnClickListener(v -> savePlayer());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        buttonSelectImage.setOnClickListener(v -> openImageSelectionOptions());
    }

    private void openImageSelectionOptions() {
        if (hasCameraPermission()) {
            openCameraForImageCapture();
        } else {
            requestCameraPermission();
        }
    }

    private boolean hasCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CAMERA);
    }

    private void openCameraForImageCapture() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } else {
            Toast.makeText(this, "No camera app found", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleImageCapture(Intent data) {
        if (data != null && data.getExtras() != null) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            if (imageBitmap != null) {
                Uri imageUri = saveImageToGallery(imageBitmap);
                if (imageUri != null) {
                    imageUriString = imageUri.toString();
                    imageViewPlayer.setImageBitmap(imageBitmap);
                } else {
                    Toast.makeText(this, "Failed to save the captured image", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Failed to retrieve the captured image", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "No image captured", Toast.LENGTH_SHORT).show();
        }
    }

    private Uri saveImageToGallery(Bitmap bitmap) {
        ContentResolver resolver = getContentResolver();
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, "FootballPlayerImage");
        contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        Uri imageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        Uri savedImageUri = resolver.insert(imageUri, contentValues);

        try {
            if (savedImageUri != null) {
                OutputStream outputStream = resolver.openOutputStream(savedImageUri);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                outputStream.close();
                return savedImageUri;
            } else {
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
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

    private void showDatePickerDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    selectedDate = year + "-" + (month + 1) + "-" + dayOfMonth;
                    buttonSelectDate.setText(selectedDate);
                },
                Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CAMERA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCameraForImageCapture();
            } else {
                Toast.makeText(this, "Permission denied to access the camera", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void savePlayer() {
        String name = editTextName.getText().toString().trim();
        String club = editTextClub.getText().toString().trim();
        String position = spinnerPosition.getSelectedItem().toString().trim();
        if (name.isEmpty() || club.isEmpty() || imageUriString.isEmpty() ||selectedDate == null || selectedDate.equals("Select Join Date")) {
            Toast.makeText(this, "Player name, club, photo and join date are required fields!!.", Toast.LENGTH_SHORT).show();
            return;
        }

        FootballPlayer player = new FootballPlayer();
        player.nameSurname = name;
        player.club = club;
        player.position = position;
        player.image = imageUriString;
        player.joinDate = selectedDate;

        viewModel.insert(player);

        finish();
    }
}
