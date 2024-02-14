package holovka.footballplayersapp.model.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "football_players")
public class FootballPlayer {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String nameSurname;
    public String position;
    public String club;
    public String image;
    public String joinDate;

    @Override
    public String toString() {
        return nameSurname + " - " + position;
    }

}
