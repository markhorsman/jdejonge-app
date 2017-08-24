package com.example.markhorsman.insphireapi;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.markhorsman.insphireapi.api.Insphire;
import com.example.markhorsman.insphireapi.model.CustomerContact;
import com.example.markhorsman.insphireapi.model.Status;
import com.example.markhorsman.insphireapi.model.Stock;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONObject;
import java.net.HttpURLConnection;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    public static final String BASE_URL = "http://192.168.192.43:8080/";

    private final String username = "jdejong";
    private final String password = "insphire";
    private String base = username + ":" + password;
    private String authHeader = "Basic " + Base64.encodeToString(base.getBytes(), Base64.NO_WRAP);

    private TextView informationTextView;
    private EditText barcodeText;
    private EditText customerReferenceText;
    private Button findStockButton;
    private Button findCustomerButton;
    private Button fetchCustomerButton;
    private Button putInRentButton;
    private Button pullFromRentButton;

    private Gson gson;
    private Retrofit retrofit;
    private Insphire insphire;

    private CustomerContact currentCustomerContact;
    private Stock currentStock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        informationTextView = (TextView) findViewById(R.id.informationTextView);
        barcodeText = (EditText) findViewById(R.id.barcodeText);
        customerReferenceText = (EditText) findViewById(R.id.customerReferenceText);
        findStockButton = (Button) findViewById(R.id.findStockButton);
        findCustomerButton = (Button) findViewById(R.id.findCustomerButton);
        fetchCustomerButton = (Button) findViewById(R.id.fetchCustomerButton);
        putInRentButton = (Button) findViewById(R.id.putInRentButton);
        pullFromRentButton = (Button) findViewById(R.id.pullFromRentButton);

        gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
                .create();

        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        insphire = retrofit.create(Insphire.class);

        View.OnClickListener fetchCustomerButtonListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // reset fields and textview
                informationTextView.setText("");

                currentCustomerContact = null;
                currentStock = null;

                fetchCustomerButton.setVisibility(View.GONE);
                findStockButton.setVisibility(View.GONE);
                putInRentButton.setVisibility(View.GONE);
                pullFromRentButton.setVisibility(View.GONE);
                barcodeText.setVisibility(View.GONE);
                findCustomerButton.setVisibility(View.VISIBLE);
                customerReferenceText.setVisibility(View.VISIBLE);
            }
        };

        View.OnClickListener stockButtonListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Update informationTextView with the Stock item Desc field value

                // find Stock item using barcodeText value
                String barcode = barcodeText.getText().toString();

                // hide buttons
                findStockButton.setVisibility(View.GONE);
                putInRentButton.setVisibility(View.GONE);
                pullFromRentButton.setVisibility(View.GONE);
                getStockItem(barcode);
            }
        };

        View.OnClickListener findCustomerButtonListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Update informationTextView with the Stock item Desc field value

                // find Stock item using barcodeText value
                String reference = customerReferenceText.getText().toString();

                // hide button
                findCustomerButton.setVisibility(View.GONE);
                getCustomerContact(reference);
            }
        };

        View.OnClickListener putInRentButtonListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // hide button
                putInRentButton.setVisibility(View.GONE);
                findStockButton.setVisibility(View.GONE);
                updateStockStatus(currentStock.ITEMNO, Stock.STATUS_IN_RENT);
            }
        };

        final View.OnClickListener pullFromRentButtonListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // hide button
                pullFromRentButton.setVisibility(View.GONE);
                findStockButton.setVisibility(View.GONE);
                updateStockStatus(currentStock.ITEMNO, Stock.STATUS_AVAILABLE);
            }
        };

        fetchCustomerButton.setOnClickListener(fetchCustomerButtonListener);
        findStockButton.setOnClickListener(stockButtonListener);
        findCustomerButton.setOnClickListener(findCustomerButtonListener);
        putInRentButton.setOnClickListener(putInRentButtonListener);
        pullFromRentButton.setOnClickListener(pullFromRentButtonListener);

    }

    private void getStockItem(String barcode) {
        Log.d(TAG, "About to fetch Stock item!");

        Call<Stock> call = insphire.getStockItem(authHeader, barcode);
        call.enqueue(new Callback<Stock>() {
            @Override
            public void onResponse(Call<Stock> call, Response<Stock> response) {
                int statusCode = response.code();
                Stock stock = response.body();

                if (statusCode == HttpURLConnection.HTTP_OK) {
                    informationTextView.setText(stock.DESC1);
                    barcodeText.getText().clear();
                    currentStock = stock;

                    if (stock.STATUS == Stock.STATUS_AVAILABLE) {
                        // show put in rental button
                        putInRentButton.setVisibility(View.VISIBLE);
                    } else if (stock.STATUS == Stock.STATUS_IN_RENT) {
                        // show pull from rental button
                        pullFromRentButton.setVisibility(View.VISIBLE);
                    } else {
                        // stock item not available
                        Toast.makeText(MainActivity.this, "Product niet beschikbaar", Toast.LENGTH_LONG).show();
                    }
                } else {
                    showAPIErrorMessage(response);
                }

                findStockButton.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFailure(Call<Stock> call, Throwable t) {
                showAPIFailureMessage(t);
                findStockButton.setVisibility(View.VISIBLE);
                Log.d(TAG, t.getMessage());
            }
        });
    }

    private void getCustomerContact(String reference) {
        Log.d(TAG, "About to fetch Customer!");

        Call<CustomerContact> call = insphire.getCustomerContact(authHeader, reference);
        call.enqueue(new Callback<CustomerContact>() {
            @Override
            public void onResponse(Call<CustomerContact> call, Response<CustomerContact> response) {
                int statusCode = response.code();
                CustomerContact customer = response.body();

                if (statusCode == HttpURLConnection.HTTP_OK) {
                    currentCustomerContact = customer;

                    informationTextView.setText("Huidige klant: " + customer.NAME);
                    customerReferenceText.getText().clear();

                    customerReferenceText.setVisibility(View.GONE);
                    findStockButton.setVisibility(View.VISIBLE);
                    barcodeText.setVisibility(View.VISIBLE);
                } else {
                    showAPIErrorMessage(response);

                    findCustomerButton.setVisibility(View.VISIBLE);
                }

                fetchCustomerButton.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFailure(Call<CustomerContact> call, Throwable t) {
                showAPIFailureMessage(t);
                findCustomerButton.setVisibility(View.VISIBLE);
                Log.d(TAG, t.getMessage());
            }
        });
    }

    private void updateStockStatus(String itemno, final int status) {
        final int originalStockStatus = currentStock.STATUS;
        currentStock.STATUS = status;

        Call<Status> call = insphire.updateStockStatus(authHeader, itemno, currentStock);
        call.enqueue(new Callback<Status>() {
            @Override
            public void onResponse(Call<Status> call, Response<Status> response) {
                int statusCode = response.code();

                if (statusCode == HttpURLConnection.HTTP_OK) {
                    Boolean resultStatus = response.body().status;
                    if (resultStatus == true) {
                        if (status == Stock.STATUS_IN_RENT) {
                            pullFromRentButton.setVisibility(View.VISIBLE);
                        } else {
                            putInRentButton.setVisibility(View.VISIBLE);
                        }

                        Toast.makeText(MainActivity.this, "Artikel opgeslagen" , Toast.LENGTH_LONG).show();
                    } else {
                        currentStock.STATUS = originalStockStatus;
                    }
                } else {
                    currentStock.STATUS = originalStockStatus;
                    showAPIErrorMessage(response);
                }

                findStockButton.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFailure(Call<Status> call, Throwable t) {
                showAPIFailureMessage(t);
                Log.d(TAG, t.getMessage());

                findStockButton.setVisibility(View.VISIBLE);

                if (status == Stock.STATUS_IN_RENT) {
                    pullFromRentButton.setVisibility(View.VISIBLE);
                } else {
                    putInRentButton.setVisibility(View.VISIBLE);
                }

                currentStock.STATUS = originalStockStatus;
            }
        });
    }

    private void showAPIErrorMessage(Response response) {
        try {
            JSONObject jObjError = new JSONObject(response.errorBody().string());
            Toast.makeText(this, jObjError.getString("message"), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void showAPIFailureMessage(Throwable t) {
        Toast.makeText(this, t.getMessage(), Toast.LENGTH_LONG).show();
    }
}
