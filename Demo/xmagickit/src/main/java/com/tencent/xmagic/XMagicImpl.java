package com.tencent.xmagic;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Looper;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.tencent.xmagic.module.XmagicResParser;
import com.tencent.xmagic.panel.XmagicPanelDataManager;
import com.tencent.xmagic.panel.XmagicPanelView;
import com.tencent.xmagic.telicense.TELicenseCheck;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class XMagicImpl implements SensorEventListener {
    private static final String TAG = "XMagicImpl";
    private static String xmagicAuthKey = "";
    private static String xmagicAuthLicenceUrl = "";

    private XmagicApi mXmagicApi;
    //判断当前手机旋转方向，用于手机在不同的旋转角度下都能正常的识别人脸
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;

    private boolean isOpenXmagic = true;

    private XmagicPanelView xmagicPanelView = null;

    private boolean isOpenPhotoAlbum = false;

    public static Context applicationContext = null;


    private boolean isOpenBeauty = true;

    /**
     * 由于外部有一个隐私政策问题，导致此处需要进行特殊处理
     */
    private boolean userBeauty = false;

    private boolean mMute = false;
    private Fragment mHostFragment;

    /**
     * 使用美颜之前必须先调用此方法
     *
     * @param context
     */
    public static void init(Context context) {
        applicationContext = context.getApplicationContext();
        XmagicResParser.setResPath(new File(applicationContext.getFilesDir(), "xmagic").getAbsolutePath());
    }

    public static void setXmagicAuthKeyAndUrl(String authLicenceUrl, String authKey) {
        XMagicImpl.xmagicAuthKey = authKey;
        XMagicImpl.xmagicAuthLicenceUrl = authLicenceUrl;
    }

    /**
     * 进行美颜授权检验,注：调用此方法之前要保证ini方法已被调用
     *
     * @param listener
     */
    public static void checkAuth(final TELicenseCheck.TELicenseCheckListener listener) {                         //
        if (applicationContext == null) {
            throw new RuntimeException("please init XMagicImpl init()");
        }
        if (listener == null) {
            TELicenseCheck.getInstance().setTELicense(applicationContext, xmagicAuthLicenceUrl, xmagicAuthKey, null);
        } else {
            TELicenseCheck.TELicenseCheckListener l = new TELicenseCheck.TELicenseCheckListener() {
                @Override
                public void onLicenseCheckFinish(final int i, final String s) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            if (listener != null) {
                                listener.onLicenseCheckFinish(i, s);
                            }
                        }
                    });
                }
            };
            TELicenseCheck.getInstance().setTELicense(applicationContext, xmagicAuthLicenceUrl, xmagicAuthKey,l);
        }
    }

    /**
     * 创建美颜对象
     *
     * @param activity
     * @param panelLayout
     */
    public XMagicImpl(final Activity activity, RelativeLayout panelLayout) {
        getUserBeauty();
        XmagicApi.OnXmagicPropertyErrorListener listener = new XmagicApi.OnXmagicPropertyErrorListener() {
            @Override
            public void onXmagicPropertyError(final String errorMsg, final int code) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (xmagicPanelView != null) {
                            xmagicPanelView.showErrorMsg(code + ":" + errorMsg);
                        } else {
                            Toast.makeText(activity.getApplicationContext(),
                                    code + ":" + errorMsg, Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        };
        mXmagicApi = XmagicApiWrapper.createXmagicApi(activity.getApplicationContext(), true, listener);
        initPropertyUiPanel(activity, panelLayout);
        mSensorManager = (SensorManager) activity.getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    private void initPropertyUiPanel(final Activity activity, RelativeLayout panelLayout) {
        if (xmagicPanelView != null) {
            return;
        }
        xmagicPanelView = new XmagicPanelView(panelLayout.getContext());
        xmagicPanelView.setOnUserUpdatePropertyListener(new XmagicPanelView.PanelViewCallBack() {
            @Override
            public void onUserUpdateProperty(XmagicProperty<?> xmagicProperty) {
                updateProperty(xmagicProperty);
            }

            @Override
            public void onClickCustomSegItem() {
                isOpenPhotoAlbum = true;
                if (mHostFragment != null) {
                    XmagicPanelView.openPhotoAlbum(mHostFragment);
                } else {
                    XmagicPanelView.openPhotoAlbum(activity);
                }
            }

            @Override
            public void onRevertBtnClick() {
                if (mXmagicApi != null) {
                    mXmagicApi.updateProperties(XmagicPanelDataManager.getInstance().getRevertXmagicData());
                }
                if (xmagicPanelView != null) {
                    xmagicPanelView.revertMenuList();
                }
            }

            @Override
            public void onBeautySwitchCheckedChanged(boolean isChecked) {
                isOpenXmagic = isChecked;
                if (mXmagicApi != null) {
                    if (isOpenXmagic) {
                        mXmagicApi.onResume();
                    } else {
                        mXmagicApi.onPause();
                    }
                }
            }

            @Override
            public void onBeautyCloseOrOpen(boolean isOpen) {
                isOpenBeauty = isOpen;
                List<XmagicProperty<?>> updateList = null;
                if (!isOpen) {
                    updateList = XmagicPanelDataManager.getInstance().getCloseBeautyItems();
                } else {
                    updateList = XmagicPanelDataManager.getInstance().getOpenBeautyItems();
                }
                if (mXmagicApi != null && updateList != null && updateList.size() > 0) {
                    mXmagicApi.updateProperties(updateList);
                }
            }

        });
        panelLayout.removeAllViews();
        int width = ViewGroup.LayoutParams.MATCH_PARENT;
        int height = ViewGroup.LayoutParams.WRAP_CONTENT;
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, height);
        panelLayout.addView(xmagicPanelView, params);
    }

    /**
     * 处理 视频/摄像头 每一帧数据
     *
     * @param textureId
     * @param width
     * @param height
     * @return
     */
    public int process(int textureId, int width, int height) {
        if (userBeauty && mXmagicApi != null && isOpenXmagic) {
            return mXmagicApi.process(textureId, width, height);
        }
        return textureId;
    }


    private void updateProperty(XmagicProperty<?> xmagicProperty) {
        if (mXmagicApi != null && xmagicProperty != null) {
            List<XmagicProperty<?>> xmagicPropertyList = new ArrayList<>();
            xmagicPropertyList.add(xmagicProperty);
            mXmagicApi.isDeviceSupport(xmagicPropertyList);
            for (XmagicProperty<?> property : xmagicPropertyList) {
                if (property != null && property.isSupport) {
                    mXmagicApi.updateProperty(xmagicProperty);
                } else {
                    String tip = "the device is not support this motion";
                    Toast.makeText(applicationContext, tip, Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    /**
     * 用于恢复美颜效果使用
     */
    public void onResume() {
        if (mXmagicApi == null) {
            mXmagicApi = XmagicApiWrapper.createXmagicApi(applicationContext, false, null);
            mXmagicApi.updateProperties(XmagicPanelDataManager.getInstance().getUsedProperty());
            List<XmagicProperty<?>> updateList = null;
            if (isOpenBeauty) {
                updateList = XmagicPanelDataManager.getInstance().getOpenBeautyItems();
            } else {
                updateList = XmagicPanelDataManager.getInstance().getCloseBeautyItems();
            }
            if (mXmagicApi != null && updateList != null && updateList.size() > 0) {
                mXmagicApi.updateProperties(updateList);
            }
            mXmagicApi.setAudioMute(mMute);
        } else {
            mXmagicApi.onResume();
        }
        //注册传感器
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    /**
     * 用于暂停美颜效果
     */
    public void onPause() {
        if (mXmagicApi != null) {
            mXmagicApi.onPause();
        }
        //取消传感器
        mSensorManager.unregisterListener(this);
    }

    /**
     * 当页面效果时调用，销毁美颜
     * 需要在gl线程创建
     */
    public void onDestroy() {
        if (mXmagicApi != null) {
            mXmagicApi.onDestroy();
            mXmagicApi = null;
        }
    }



    public void setAudioMute(boolean isMute) {
        mMute = isMute;
        if (mXmagicApi != null) {
            mXmagicApi.setAudioMute(isMute);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (mXmagicApi != null) {
            mXmagicApi.sensorChanged(event, mAccelerometer);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (isOpenPhotoAlbum && xmagicPanelView != null) {
            isOpenPhotoAlbum = false;
            xmagicPanelView.onActivityResult(requestCode, resultCode, data);
        }
    }


    public enum XmagicState {
        IDLE, STARTED, STOPPED
    }


    public void setBeautyStateOpen() {
        Editor editor = applicationContext.getSharedPreferences("xmagic_settings", Context.MODE_PRIVATE).edit();
        editor.putBoolean("xmagic_state", true).commit();
        userBeauty = true;
    }

    private void getUserBeauty() {
        SharedPreferences sp = applicationContext.getSharedPreferences("xmagic_settings", Context.MODE_PRIVATE);
        userBeauty = sp.getBoolean("xmagic_state", false);
    }


    public void setHostFragment(Fragment mHostFragment) {
        this.mHostFragment = mHostFragment;
    }
}
