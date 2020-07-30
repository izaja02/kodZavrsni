package com.example.currentplacedetailsonmap;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

public class GpsDialog extends AppCompatDialogFragment {

    Intent intent1;


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Omogući GPS u postavkama")
                .setMessage("U postavkama Vam nije omogućen GPS, molimo Vas pritisnite OMOGUĆI kako bi to promijenili i kvalitetno nastavili s uporabom aplikacije  ")
                .setPositiveButton("omoguĆi", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        intent1 = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(intent1);

                    }
                })
                .setNegativeButton("onemoguĆi", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        Toast.makeText(getContext(),"Aplikacija ne vidi vašu lokaciju", Toast.LENGTH_SHORT).show();
                        return;
                    }
                });


        return builder.create();
    }

}
