package holovka.footballplayersapp;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import com.bumptech.glide.Glide;
import java.util.Calendar;

public class AddPlayerActivity extends AppCompatActivity {

    private EditText editTextName, editTextClub;
    private Button buttonSelectImage;
    private String imageUriString = "";
    private FootballPlayerViewModel viewModel;

    private ImageView imageViewPlayer;

    private Spinner spinnerPosition;
    private Button buttonSelectDate;
    private String selectedDate = "";


    private static final int PERMISSION_REQUEST_STORAGE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_player);

        editTextName = findViewById(R.id.editTextPlayerName);
        editTextClub = findViewById(R.id.editTextPlayerClub);
        buttonSelectImage = findViewById(R.id.button_select_image);
        spinnerPosition = findViewById(R.id.spinnerPosition);
        buttonSelectDate = findViewById(R.id.button_select_date);
        buttonSelectDate.setOnClickListener(v -> showDatePickerDialog());


        viewModel = new ViewModelProvider(this).get(FootballPlayerViewModel.class);
        imageViewPlayer = findViewById(R.id.imageViewPlayer);

        buttonSelectImage.setOnClickListener(v -> requestStoragePermission());
        Button buttonSave = findViewById(R.id.button_save);
        buttonSave.setOnClickListener(v -> {
            savePlayer();
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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

    private void requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, PERMISSION_REQUEST_STORAGE);
            } else {
                openGallery();
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_STORAGE);
            } else {
                openGallery();
            }
        }
    }


    private void openGallery() {
        pickImageLauncher.launch("image/*");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                Toast.makeText(this, "Permission denied to access your images", Toast.LENGTH_SHORT).show();
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




    private ActivityResultLauncher<String> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            new ActivityResultCallback<Uri>() {
                @Override
                public void onActivityResult(Uri result) {
                    if (result != null) {
                        imageUriString = result.toString();
                        Log.d("ActivityResult", "imageUriString: " + imageUriString);
                        Glide.with(AddPlayerActivity.this)
                                .load(result)
                                .into(imageViewPlayer);
                    } else {
                        Log.e("ActivityResult", "Selected image URI is null");
                    }
                }
            });



    private void savePlayer() {
        String name = editTextName.getText().toString().trim();
        String club = editTextClub.getText().toString().trim();
        String position = spinnerPosition.getSelectedItem().toString().trim();

        if (name.isEmpty() || club.isEmpty() || imageUriString.isEmpty()) {
            Toast.makeText(this, "All fields are required.", Toast.LENGTH_SHORT).show();
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
