/*
 * Copyright (C) 2014-2015 The MoKee OpenSource Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.mokee.push;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Notification;
import android.app.Notification.BigTextStyle;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.mokee.utils.MoKeeUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.UserHandle;
import android.util.Log;
import cn.jpush.android.api.JPushInterface;

import com.android.settings.R;
import com.android.settings.mokee.stats.Utilities;

import com.mokee.helper.misc.Constants;
import com.mokee.helper.receiver.UpdateCheckReceiver;
import com.mokee.os.Build;

public class PushingMessageReceiver extends BroadcastReceiver {

    protected static final String TAG = PushingMessageReceiver.class.getSimpleName();

    private static final String MKPUSH_PREF = "mokee_push";
    private static final String PREF_NEWS = "pref_news";

    private SharedPreferences prefs;

    @Override
    public void onReceive(Context ctx, Intent intent) {
        Bundle bundle = intent.getExtras();
        Log.d(TAG, "[MyReceiver] onReceive - " + intent.getAction() + ", extras: " + printBundle(bundle));
        if (JPushInterface.ACTION_MESSAGE_RECEIVED.equals(intent.getAction())) {
            String message = bundle.getString(JPushInterface.EXTRA_MESSAGE);
            String customContentString = bundle.getString(JPushInterface.EXTRA_EXTRA);
            onMessage(ctx, message, customContentString);
            JPushInterface.reportNotificationOpened(ctx, bundle.getString(JPushInterface.EXTRA_MSG_ID));
        }
    }

    public void onMessage(Context ctx, String message, String customContentString) {
        if (customContentString != null & customContentString != "") {
            JSONObject customJson = null;
            try {
                customJson = new JSONObject(customContentString);
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
            prefs = ctx.getSharedPreferences(MKPUSH_PREF, 0);
            String device = PushingUtils.getStringFromJson("device", customJson);
            String modType = PushingUtils.getStringFromJson("type", customJson);
            String url = PushingUtils.getStringFromJson("url", customJson);
            String title = PushingUtils.getStringFromJson("title", customJson);
            String newVersion = PushingUtils.getStringFromJson("version", customJson);
            String HASHID = PushingUtils.getStringFromJson("hashid", customJson);
            String user = PushingUtils.getStringFromJson("user", customJson);
            String IMEI = PushingUtils.getStringFromJson("imei", customJson);
            int msg_id = PushingUtils.getIntFromJson("id", customJson);
            String mod_device = Build.PRODUCT.toLowerCase();
            String mod_version = Build.VERSION.toLowerCase();

            switch (msg_id) {
                case 0:
                case 1:
                    if (PushingUtils.allowPush(device, mod_device, 1) && PushingUtils.allowPush(modType, mod_version, 0)
                            || device.equals("all") && modType.equals("all")
                            || device.equals("all") && PushingUtils.allowPush(modType, mod_version, 0)
                            || PushingUtils.allowPush(device, mod_device, 1) && modType.equals("all")) {
                        switch (msg_id) {
                            case 0:
                                String mod_version_code = mod_version.split("-")[2];
                                int new_version_code = Integer.parseInt(newVersion);
                                if (mod_version_code.length() > 6) {
                                    mod_version_code = mod_version_code.substring(2, 8);
                                }
                                if (new_version_code > Integer.parseInt(mod_version_code)) {
                                    Intent intent = new Intent(UpdateCheckReceiver.ACTION_UPDATE_CHECK);
                                    ctx.sendBroadcastAsUser(intent, UserHandle.CURRENT);
                                }
                                break;
                            case 1:
                                if (MoKeeUtils.isSupportLanguage(true) && prefs.getBoolean(PREF_NEWS, true)) {
                                    promptUser(ctx, url, title, message, msg_id, R.drawable.ic_mokee_msg);
                                }
                                break;
                        }
                    }
                    break;
                case 2:
                    if (HASHID.equals(Build.getUniqueID(ctx))) {
                        promptUser(ctx, url, title, message, msg_id, R.drawable.ic_mokee_msg);
                    }
                    break;
                case 3:
                    if (IMEI.equals(Utilities.getIMEI(ctx))) {
                        promptUser(ctx, url, title, message, msg_id, R.drawable.ic_mokee_msg);
                    }
                    break;
                case 4:
                    if (user.equals(android.os.Build.USER)) {
                        promptUser(ctx, url, title, message, msg_id, R.drawable.ic_mokee_msg);
                    }
            }
        }
    }

    private void promptUser(Context context, String url, String title, String message, int id, int icon) {
        NotificationManager nm = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        Uri uri = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        PendingIntent pendintIntent = PendingIntent.getActivity(context, 0, intent, 0);

        BigTextStyle noti = new Notification.BigTextStyle(new Notification.Builder(context)
                .setSmallIcon(icon).setAutoCancel(true).setTicker(title)
                .setContentIntent(pendintIntent).setWhen(0).setContentTitle(title)
                .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_LIGHTS)
                .setContentText(message)).bigText(message);

        nm.notify(id, noti.build());
    }

    // 打印所有的 intent extra 数据
    private static String printBundle(Bundle bundle) {
        StringBuilder sb = new StringBuilder();
        for (String key : bundle.keySet()) {
            if (key.equals(JPushInterface.EXTRA_NOTIFICATION_ID)) {
                sb.append("\nkey:" + key + ", value:" + bundle.getInt(key));
            }else if(key.equals(JPushInterface.EXTRA_CONNECTION_CHANGE)){
                sb.append("\nkey:" + key + ", value:" + bundle.getBoolean(key));
            } 
            else {
                sb.append("\nkey:" + key + ", value:" + bundle.getString(key));
            }
        }
        return sb.toString();
    }
}