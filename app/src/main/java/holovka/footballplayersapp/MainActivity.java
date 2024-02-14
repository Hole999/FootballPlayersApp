package holovka.footballplayersapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

public class MainActivity extends AppCompatActivity {

    private FootballPlayerViewModel footballPlayerViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ListView listView = findViewById(R.id.list_view_players);

        footballPlayerViewModel = new ViewModelProvider(this).get(FootballPlayerViewModel.class);

        footballPlayerViewModel.getAllPlayers().observe(this, footballPlayers -> {
            PlayerAdapter adapter = new PlayerAdapter(MainActivity.this, footballPlayers);
            listView.setAdapter(adapter);
        });


        listView.setOnItemClickListener((parent, view, position, id) -> {
            FootballPlayer selectedPlayer = (FootballPlayer) parent.getItemAtPosition(position);
            Intent intent = new Intent(MainActivity.this, EditPlayerActivity.class);
            intent.putExtra("playerId", selectedPlayer.id);
            intent.putExtra("joinDate", selectedPlayer.joinDate);
            startActivity(intent);
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_add) {
            Intent intent = new Intent(MainActivity.this, AddPlayerActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
