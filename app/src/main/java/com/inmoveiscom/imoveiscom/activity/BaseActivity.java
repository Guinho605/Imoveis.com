package com.inmoveiscom.imoveiscom.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.inmoveiscom.imoveiscom.R;

public class BaseActivity extends AppCompatActivity {

    private AlertDialog noInternetDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    protected void showNoInternetDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.no_internet_dialog, null);
        builder.setView(dialogView);
        builder.setCancelable(false);

        // Referência ao botão "Tentar Novamente"
        Button btnTryAgain = dialogView.findViewById(R.id.btnTryAgain);
        btnTryAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Verifique novamente a conexão com a internet após o botão "Tentar Novamente" ser clicado
                checkInternetConnection();
            }
        });

        noInternetDialog = builder.create();
        noInternetDialog.show();
    }

    protected void checkInternetConnection() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            // O dispositivo está conectado à internet, então feche o diálogo
            dismissNoInternetDialog();
        }
    }

    protected void dismissNoInternetDialog() {
        if (noInternetDialog != null && noInternetDialog.isShowing()) {
            noInternetDialog.dismiss();
        }
    }

    public void onNetworkChange(boolean isConnected) {
        // Implemente o comportamento desejado quando a conexão de rede muda
        if (isConnected) {
            dismissNoInternetDialog();
        }
    }
}