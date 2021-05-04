package me.pgb.a2021_04_26_room;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import io.reactivex.Observable;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import me.pgb.a2021_04_26_room.db.DatabaseOperations;
import me.pgb.a2021_04_26_room.db.Stock;

import android.util.Log;
import android.view.View;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "_MainActivity_";
    private Button addStockButton;
    private Button deleteStockButton;
    private Button updateStockButton;
    private EditText nameEditText;
    private EditText priceEditText;
    private PortfolioViewModel portfolioViewModel;
    private LiveData<List<Stock>> allStocks;

    private Observable<Stock> observable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        addStockButton = findViewById(R.id.insert_stock_button);
        deleteStockButton = findViewById(R.id.delete_stock_button);
        nameEditText = findViewById(R.id.name_text_view);
        priceEditText = findViewById(R.id.price_text_view);
        updateStockButton = findViewById(R.id.update_stock_button);

        portfolioViewModel = new ViewModelProvider(this).get(PortfolioViewModel.class);
        allStocks = portfolioViewModel.getAllStocks();

        portfolioViewModel.getAllStocks().observe(this,
                new Observer<List<Stock>>() {
                    @Override
                    public void onChanged(List<Stock> stocks) {
                        for (Stock stock : stocks) {
                            if (!allStocks.getValue().contains(stock)){
                                allStocks.getValue().add(stock);
                            }
                        }
                    }
                });

        addStockButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //if no stock name is inputed by the user do-nothing
                if (0 == nameEditText.getText().toString().length() ||priceEditText.getText().toString().length() == 0 ) {
                    Toast.makeText(getBaseContext(), "Please input valid stock or price" , Toast.LENGTH_SHORT).show();
                    return;
                }

                double price = Double.parseDouble(priceEditText.getText().toString());

                String name = nameEditText.getText().toString();
                price = Double.parseDouble(priceEditText.getText().toString());
                Stock stock = new Stock(name, price);

                if (isStockInDatabase_faster(stock.name)) {
                    inDataBaseAlert();
                    return;
                }

                stock.databaseOperations = DatabaseOperations.INSERT;
                observable = io.reactivex.Observable.just(stock);
                io.reactivex.Observer<Stock> observer = getStockObserver(stock);

                Toast.makeText(getBaseContext(), "Stock Successfully added!" , Toast.LENGTH_SHORT).show();

                observable
                        .observeOn(Schedulers.io())
                        .subscribe(observer);

            }
        });

        deleteStockButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = nameEditText.getText().toString();

                Stock stock = new Stock(name, 0.0);;

                //if stock in the Database then delete
                for (Stock find_stock : allStocks.getValue()) {
                    if (name.equals(find_stock.name)) {
                            stock = find_stock;
                            break;
                    }
                }

                if (stock.price == 0.0){
                    Toast.makeText(getBaseContext(), "Non-Existent Stock" , Toast.LENGTH_SHORT).show();
                    return;
                }

                stock.databaseOperations = DatabaseOperations.DELETE;
                observable = io.reactivex.Observable.just(stock);
                io.reactivex.Observer<Stock> observer = getStockObserver(stock);

                Toast.makeText(getBaseContext(), "Stock Successfully deleted!" , Toast.LENGTH_SHORT).show();

                observable
                        .observeOn(Schedulers.io())
                        .subscribe(observer);
            }
        });

        updateStockButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UpdateStock();
            }
        });

    }

    public void UpdateStock(){
        Intent intent= new Intent(this, StockUpdate.class);
        startActivity(intent);
    }

    private boolean isStockInDatabase(String name) {
        Stock stock = portfolioViewModel.getPortfolioDatabase().stockDao().isStockInDatabase(name);
        if (null == stock) {
            return false;
        } else {
            return true;
        }
    }

    private boolean isStockInDatabase_faster(String name) {
        boolean inDB = false;
        for (Stock stock : allStocks.getValue()) {
            if (name.equals(stock.name)) {
                inDB = true;
                break;
            }
        }

        return inDB;
    }

    /*
     * https://developer.android.com/guide/topics/ui/dialogs
     */
    private void inDataBaseAlert() {
        new AlreadyInDatabase().show(getSupportFragmentManager(),TAG);
    }

    private void listAll() {
        Log.i(TAG, "allStocks size: " + allStocks.getValue().size());
        for (Stock stock : allStocks.getValue()) {
            Log.i(TAG, "Stock: " + stock.name);
        }
    }

    private io.reactivex.Observer<Stock> getStockObserver(Stock stock) { // OBSERVER
        return new io.reactivex.Observer<Stock>() {
            @Override
            public void onSubscribe(Disposable d) {
                Log.d(TAG, "onSubscribe");
            }

            @Override
            public void onNext(@NonNull Stock stock) {
                switch(stock.databaseOperations) {
                    case INSERT:
                        if (!isStockInDatabase(stock.name)) {
                            portfolioViewModel.getPortfolioDatabase().stockDao().insert(stock);
                        }
                        break;
                    case DELETE:
                        portfolioViewModel.getPortfolioDatabase().stockDao().delete(stock);
                        break;
                    case UPDATE:
                        portfolioViewModel.getPortfolioDatabase().stockDao().update(stock);
                        break;
                    default:
                        Log.i(TAG, "Default");
                }
            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, "onError: " + e.getMessage());
            }

            @Override
            public void onComplete() {
                Log.d(TAG, "All items are emitted!");
            }
        };
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}