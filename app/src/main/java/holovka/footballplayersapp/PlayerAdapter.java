package holovka.footballplayersapp;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

public class PlayerAdapter extends ArrayAdapter<FootballPlayer> {
    public PlayerAdapter(Context context, List<FootballPlayer> players) {
        super(context, 0, players);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_player, parent, false);
        }

        ImageView playerImage = convertView.findViewById(R.id.player_image);
        TextView playerName = convertView.findViewById(R.id.player_name);

        FootballPlayer player = getItem(position);

        playerName.setText(player.nameSurname);
        if (player.image != null && !player.image.isEmpty()) {
            Glide.with(getContext()).load(Uri.parse(player.image)).into(playerImage);
        }

        return convertView;
    }
}
