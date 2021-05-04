package me.pgb.a2021_04_26_room;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;

/*
 * https://developer.android.com/guide/topics/ui/dialogs
 */

public class AlreadyInDatabase extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.dialog_already_in_DB)
                .setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Anything to do?
                    }
                });

        // Create the AlertDialog object and return it
        return builder.create();
    }
}
