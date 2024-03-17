package com.example.findany;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Window;
import android.view.WindowManager;
//Dialogs
public class DialogUtils {
    public static Dialog createLoadingDialog(Context context) {
        Dialog loadingDialog = new Dialog(context);
        loadingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        loadingDialog.setContentView(R.layout.loading_dialog);
        loadingDialog.setCancelable(true);

        Window window = loadingDialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(window.getAttributes());
            lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            window.setAttributes(lp);
        }

        return loadingDialog;
    }

    public static void showLoadingDialog(Dialog dialog) {
        if (dialog != null && !dialog.isShowing()) {
            dialog.show();
        }
    }
    public static void hideLoadingDialog(Dialog dialog) {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

}

