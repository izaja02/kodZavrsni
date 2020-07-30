package com.example.currentplacedetailsonmap;

import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

public class BluetoothDialog extends AppCompatDialogFragment {

    private  static  final int REQUEST_ENABLE_BT=0;
    Intent intent2;



    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Omogući BLUETOOTH u postavkama")
                .setMessage("Nije vam ukljućen  bluetooth, molimo Vas pritisnite OMOGUĆI kako bi to promijenili i kvalitetno nastavili s uporabom aplikacije  ")
                .setPositiveButton("omoguĆi", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        intent2=new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(intent2, REQUEST_ENABLE_BT);

                    }
                })
                .setNegativeButton("onemoguĆi", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        Toast.makeText(getContext(), "Bluetooth nije omogućen, ne možete kvalitetni koristiti aplikaciju", Toast.LENGTH_SHORT).show();
                        return;
                    }
                });


        return builder.create();
    }
}