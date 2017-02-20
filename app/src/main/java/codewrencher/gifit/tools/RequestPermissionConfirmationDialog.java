package codewrencher.gifit.tools;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;

/**
 * Created by Gene on 10/30/2016.
 * _____          _   __          __                  _
 * / ____|        | |  \ \        / /                 | |
 * | |     ___   __| | __\ \  /\  / / __ ___ _ __   ___| |__   ___ _ __
 * | |    / _ \ / _` |/ _ \ \/  \/ / '__/ _ \ '_ \ / __| '_ \ / _ \ '__|
 * | |___| (_) | (_| |  __/\  /\  /| | |  __/ | | | (__| | | |  __/ |
 * \_____\___/ \__,_|\___| \/  \/ |_|  \___|_| |_|\___|_| |_|\___|_|
 *
 * Copyright 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Shows OK/Cancel confirmation dialog about camera permission.
 */
public class RequestPermissionConfirmationDialog extends DialogFragment {

    private static final String TAG = "RequestPermissionConfirmationDialog";

    private static final String ARG_MESSAGE = "message";
    private static final String ARG_PERMISSION = "permission";
    private static final String ARG_PERMISSION_INDEX = "permission_index";

    /**
     * Creates and returns a new class instance
     * @param message: Message you want to pass along to the dialog
     * @param permission: Which permission to request on confir,
     * @param permission_index: What is the current permission's index in PermissionChecker
     * @return
     */
    public static RequestPermissionConfirmationDialog newInstance(String message, String permission, int permission_index) {
        RequestPermissionConfirmationDialog dialog = new RequestPermissionConfirmationDialog();

        Bundle args = new Bundle();
        args.putString(ARG_MESSAGE, message);
        args.putString(ARG_PERMISSION, permission);
        args.putInt(ARG_PERMISSION_INDEX, permission_index);
        dialog.setArguments(args);

        return dialog;
    }

    /**
     * On class instance created callback
     * @param savedInstanceState: Stores saved parameters
     * @return
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Fragment parent = getParentFragment();
        return new AlertDialog.Builder(getActivity())
                .setMessage(getArguments().getString(ARG_MESSAGE))
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(parent.getActivity(),
                                new String[]{getArguments().getString(ARG_PERMISSION)},
                                getArguments().getInt(ARG_PERMISSION_INDEX));
                    }
                })
                .setNegativeButton(android.R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Activity activity = parent.getActivity();
                                if (activity != null) {
                                    activity.finish();
                                }
                            }
                        })
                .create();
    }
}