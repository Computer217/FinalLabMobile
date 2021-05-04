package me.pgb.a2021_04_26_room;

import android.app.Application;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import me.pgb.a2021_04_26_room.db.PortfolioDatabase;
import me.pgb.a2021_04_26_room.db.Stock;

public class PortfolioViewModel extends AndroidViewModel {

    private LiveData<List<Stock>> allStocks;
    private static PortfolioDatabase portfolioDatabase;

    public PortfolioViewModel(@NonNull Application application) {
        super(application);

        portfolioDatabase = PortfolioDatabase.getInstance(application);
        allStocks = portfolioDatabase.stockDao().getAll();
    }

    public LiveData<List<Stock>> getAllStocks() {
        return allStocks;
    }

    public PortfolioDatabase getPortfolioDatabase(){
        return portfolioDatabase;
    }
}
