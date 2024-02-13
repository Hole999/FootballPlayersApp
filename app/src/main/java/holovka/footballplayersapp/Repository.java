package holovka.footballplayersapp;


import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;

public class Repository {
    private final FootballPlayerDao footballPlayerDao;

    public Repository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        footballPlayerDao = db.footballPlayerDao();
    }

    public void update(FootballPlayer player) {
        AppDatabase.databaseWriteExecutor.execute(() -> footballPlayerDao.update(player));
    }

    public void delete(FootballPlayer player) {
        AppDatabase.databaseWriteExecutor.execute(() -> footballPlayerDao.delete(player));
    }
    public void insert(FootballPlayer player) {
        AppDatabase.databaseWriteExecutor.execute(() -> footballPlayerDao.insert(player));
    }
    public LiveData<FootballPlayer> getPlayerById(int playerId) {
        return footballPlayerDao.getPlayerById(playerId);
    }

    public LiveData<List<FootballPlayer>> getAllPlayers() {
        return footballPlayerDao.getAllPlayers();
    }
}
