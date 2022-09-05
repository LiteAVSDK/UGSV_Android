package com.tencent.qcloud.xiaoshipin.userinfo;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.ToastUtils;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.tencent.qcloud.ugckit.utils.TCUserMgr;
import com.tencent.qcloud.xiaoshipin.R;
import com.tencent.qcloud.xiaoshipin.constant.AvatarConstant;

import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

public class ModifyUserAvatarDialog extends BottomSheetDialog {
    private Context mContext;
    private ModifySuccessListener mListener;
    private RecyclerView mRvAvatar;
    private String mSelectAvatarUrl;

    public ModifyUserAvatarDialog(Context context, ModifySuccessListener listener) {
        super(context, R.style.LoginBottomDialog);
        if (TCUserMgr.getInstance().getUserId() == null) {
            dismiss();
            return;
        }
        mContext = context;
        mListener = listener;
        setContentView(R.layout.login_view_modify_user_avatar_dialog);
        mRvAvatar = findViewById(R.id.rv_avatar);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(context, 4);
        mRvAvatar.setLayoutManager(gridLayoutManager);
        String[] avatarArr = AvatarConstant.USER_AVATAR_ARRAY;
        List<String> avatarList = Arrays.asList(avatarArr);
        AvatarListAdapter adapter = new AvatarListAdapter(context, avatarList,
                new AvatarListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(String avatarUrl) {
                mSelectAvatarUrl = avatarUrl;
            }
        });
        mRvAvatar.setAdapter(adapter);
        findViewById(R.id.confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setProfile(mSelectAvatarUrl);
            }
        });
    }

    private void setProfile(final String avatarUrl) {
        if (TextUtils.isEmpty(avatarUrl) || TCUserMgr.getInstance().getUserId() == null) {
            return;
        }
        TCUserMgr.getInstance().setHeadPic(avatarUrl, new TCUserMgr.Callback() {
            @Override
            public void onSuccess(JSONObject data) {
                ToastUtils.showLong(mContext.getString(R.string.login_toast_success_to_set_username));
                mListener.onSuccess();
                dismiss();
            }

            @Override
            public void onFailure(int code, String msg) {
                ToastUtils.showLong(mContext.getString(R.string.login_toast_failed_to_set, msg));
            }
        });
    }

    public interface ModifySuccessListener {
        void onSuccess();
    }
}