package me.pgb.a2021_04_26_room;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import me.pgb.a2021_04_26_room.db.DatabaseOperations;
import me.pgb.a2021_04_26_room.db.Stock;

public class StockUpdate extends AppCompatActivity {
    private static final String TAG = "_StockUpdate_";

    private Button updateStock;
    private EditText stockName;
    private EditText priceName;
    private Button goBack;
    private PortfolioViewModel portfolioViewModel;
    private LiveData<List<Stock>> allStocks;

    private Observable<Stock> observable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_update);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        stockName = findViewById(R.id.name_view);
        priceName = findViewById(R.id.price_view);
        updateStock = findViewById(R.id.update_button);
        goBack = findViewById(R.id.go_back);

        portfolioViewModel = new ViewModelProvider(this).get(PortfolioViewModel.class);
        allStocks = portfolioViewModel.getAllStocks();

        portfolioViewModel.getAllStocks().observe(this,
                new Observer<List<Stock>>() {
                    @Override
                    public void onChanged(List<Stock> stocks) {
                        for (Stock stock : stocks) {
                            if (!allStocks.getValue().contains(stock)) {
                                allStocks.getValue().add(stock);
                            }
                        }
                    }
        });

        updateStock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //if no stock name is inputed by the user do-nothing
                if (0 == stockName.getText().toString().length() ||priceName.getText().toString().length() == 0 ) {
                    Toast.makeText(getBaseContext(), "Please input valid stock or price" , Toast.LENGTH_SHORT).show();
                    return;
                }

                String name = stockName.getText().toString();
                double price = Double.parseDouble(priceName.getText().toString());

                //if stock in the Database
                for (Stock find_stock : allStocks.getValue()) {
                    if (name.equals(find_stock.name)) {
                        find_stock.price = price;
                        find_stock.databaseOperations = DatabaseOperations.UPDATE;
                        observable = io.reactivex.Observable.just(find_stock);
                        io.reactivex.Observer<Stock> observer = getStockObserver(find_stock);

                        observable
                                .observeOn(Schedulers.io())
                                .subscribe(observer);

                        Toast.makeText(getBaseContext(), "Stock Successfully updated!" , Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                Toast.makeText(getBaseContext(), "None-existent Stock" , Toast.LENGTH_SHORT).show();

            }
        });

        goBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity();
            }
        });

    }

    public void MainActivity(){
        Intent intent= new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    /*
     * Try in UI thread...:-(
     */
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
        new AlreadyInDatabase().show(getSupportFragmentManager(), TAG);
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
                switch (stock.databaseOperations) {
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