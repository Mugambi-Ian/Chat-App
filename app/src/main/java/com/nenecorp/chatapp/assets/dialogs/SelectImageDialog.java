package com.nenecorp.chatapp.assets.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.nenecorp.chatapp.R;

public class SelectImageDialog extends Dialog {
    private ImageInterface resultListener;


    public SelectImageDialog(@NonNull Context context, int themeResId, ImageInterface resultListener) {
        super(context, themeResId);
        setCancelable(true);
        this.resultListener = resultListener;
        setOnCancelListener(new OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                dialog.dismiss();
            }
        });
        setCanceledOnTouchOutside(false);
        setContentView(R.layout.dialog_select_image);
        init();
    }

    @Override
    public void setOnCancelListener(@Nullable OnCancelListener listener) {
        super.setOnCancelListener(listener);
        dismiss();
    }

    private void init() {
        findViewById(R.id.DSI_btnC).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (resultListener != null) {
                            resultListener.result(true);
                        }
                        dismiss();
                    }
                }
        );
        findViewById(R.id.DSI_btnG).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (resultListener != null) {
                    resultListener.result(false);
                }
                dismiss();
            }
        });
        show();
    }

    public interface ImageInterface {
        void result(boolean c);
    }
}
