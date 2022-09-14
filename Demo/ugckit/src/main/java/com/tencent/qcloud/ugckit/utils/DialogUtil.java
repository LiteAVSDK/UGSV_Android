package com.tencent.qcloud.ugckit.utils;

import android.app.Dialog;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.tencent.qcloud.ugckit.R;


/**
 * 对话框工具类
 */
public class DialogUtil {

    public static void showDialog(@NonNull Context context, String title, String content, @Nullable final View.OnClickListener listener) {
        final Dialog dialog = new Dialog(context, R.style.UGCKitConfirmDialogStyle);
        final View v = LayoutInflater.from(context).inflate(R.layout.ugckit_dialog_ugc_tip, null);
        dialog.setContentView(v);
        TextView tvTitle = (TextView) dialog.findViewById(R.id.tv_title);
        TextView tvContent = (TextView) dialog.findViewById(R.id.tv_msg);
        Button btnOk = (Button) dialog.findViewById(R.id.btn_ok);
        tvTitle.setText(title);
        tvContent.setText(content);
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                if (listener != null) {
                    listener.onClick(view);
                }
            }
        });
        dialog.show();
    }
}
