package holovka.footballplayersapp.model.database;


import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

import holovka.footballplayersapp.model.entities.FootballPlayer;

@Dao
public interface FootballPlayerDao {
    @Insert
    void insert(FootballPlayer player);

    @Update
    void update(FootballPlayer player);

    @Delete
    void delete(FootballPlayer player);

    @Query("SELECT * FROM football_players")
    LiveData<List<FootballPlayer>> getAllPlayers();

    @Query("SELECT * FROM football_players WHERE id = :playerId")
    LiveData<FootballPlayer> getPlayerById(int playerId);
}
