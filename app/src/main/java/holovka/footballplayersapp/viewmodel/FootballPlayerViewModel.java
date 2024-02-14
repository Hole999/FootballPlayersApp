package holovka.footballplayersapp.viewmodel;


import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import java.util.List;

import holovka.footballplayersapp.model.entities.FootballPlayer;
import holovka.footballplayersapp.repository.Repository;

public class FootballPlayerViewModel extends AndroidViewModel {

    private final Repository repository;
    private LiveData<List<FootballPlayer>> allPlayers;

    public FootballPlayerViewModel(@NonNull Application application) {
        super(application);
        repository = new Repository(application);
        allPlayers = repository.getAllPlayers();
    }

    public LiveData<List<FootballPlayer>> getAllPlayers() {
        return allPlayers;
    }


    public void insert(FootballPlayer player) {
        repository.insert(player);
    }

    public void update(FootballPlayer player) {
        repository.update(player);
    }

    public void delete(FootballPlayer player) {
        repository.delete(player);
    }

    public LiveData<FootballPlayer> getPlayerById(int playerId) {
        return repository.getPlayerById(playerId);
    }
}
