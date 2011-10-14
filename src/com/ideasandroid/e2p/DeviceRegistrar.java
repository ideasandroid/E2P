package com.ideasandroid.e2p;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.provider.Settings.Secure;
import android.util.Log;

/**
 * 注册/注销 E2P服务.
 */
public class DeviceRegistrar {
    public static final String STATUS_EXTRA = "Status";
    public static final int REGISTERED_STATUS = 1;
    public static final int AUTH_ERROR_STATUS = 2;
    public static final int UNREGISTERED_STATUS = 3;
    public static final int ERROR_STATUS = 4;

    private static final String TAG = "DeviceRegistrar";

    private static final String REGISTER_PATH = "/service/register";
    private static final String UNREGISTER_PATH = "/service/unregister";

    public static void registerWithServer(final Context context,
          final String deviceRegistrationID) {
        new Thread(new Runnable() {
            public void run() {
                Intent updateUIIntent = new Intent("com.google.ctp.UPDATE_UI");
                try {
                    HttpResponse res = makeRequest(context, deviceRegistrationID, REGISTER_PATH);
                    if (res.getStatusLine().getStatusCode() == 200) {
                        SharedPreferences settings = Prefs.get(context);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString("deviceRegistrationID", deviceRegistrationID);
                        Log.d("************:", deviceRegistrationID);
                        editor.commit();
                        updateUIIntent.putExtra(STATUS_EXTRA, REGISTERED_STATUS);
                    } else if (res.getStatusLine().getStatusCode() == 400) {
                        updateUIIntent.putExtra(STATUS_EXTRA, AUTH_ERROR_STATUS);
                    } else {
                        Log.w(TAG, "Registration error " +
                                String.valueOf(res.getStatusLine().getStatusCode()));
                        updateUIIntent.putExtra(STATUS_EXTRA, ERROR_STATUS);
                    }
                    context.sendBroadcast(updateUIIntent);
                } catch (SAEClient.PendingAuthException pae) {
                    Intent intent = new Intent(SetupActivity.AUTH_PERMISSION_ACTION);
                    intent.putExtra("AccountManagerBundle", pae.getAccountManagerBundle());
                    context.sendBroadcast(intent);
                } catch (Exception e) {
                    Log.w(TAG, "Registration error " + e.getMessage());
                    e.printStackTrace();
                    updateUIIntent.putExtra(STATUS_EXTRA, ERROR_STATUS);
                    context.sendBroadcast(updateUIIntent);
                }
            }
        }).start();
    }

    public static void unregisterWithServer(final Context context,
            final String deviceRegistrationID) {
        new Thread(new Runnable() {
            public void run() {
                Intent updateUIIntent = new Intent("com.google.ctp.UPDATE_UI");
                try {
                    HttpResponse res = makeRequest(context, deviceRegistrationID, UNREGISTER_PATH);
                    if (res.getStatusLine().getStatusCode() != 200) {
                        Log.w(TAG, "Unregistration error " +
                                String.valueOf(res.getStatusLine().getStatusCode()));
                    }
                } catch (Exception e) {
                    Log.w(TAG, "Unregistration error " + e.getMessage());
                } finally {
                    SharedPreferences settings = Prefs.get(context);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.remove("deviceRegistrationID");
                    editor.remove("accountName");
                    editor.commit();
                    updateUIIntent.putExtra(STATUS_EXTRA, UNREGISTERED_STATUS);
                }

                // Update dialog activity
                context.sendBroadcast(updateUIIntent);
            }
        }).start();
    }

    private static HttpResponse makeRequest(Context context, String deviceRegistrationID,
            String urlPath) throws Exception {
        SharedPreferences settings = Prefs.get(context);
        String accountName = settings.getString("accountName", null);
        String username = settings.getString("username", null);
        String password = settings.getString("password", null);

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("devregid", deviceRegistrationID));

        String deviceId = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
        if (deviceId != null) {
            params.add(new BasicNameValuePair("deviceId", deviceId));
        }
        if (username != null) {
            params.add(new BasicNameValuePair("username", username));
        }
        if (password != null) {
            params.add(new BasicNameValuePair("password", password));
        }

        // TODO: Allow device name to be configured
        params.add(new BasicNameValuePair("deviceName", isTablet(context) ? "Tablet" : "Phone"));

        SAEClient client = new SAEClient(context, accountName);
        return client.makeRequest(urlPath, params);
    }

    static boolean isTablet (Context context) {
        // TODO: This hacky stuff goes away when we allow users to target devices
        int xlargeBit = 4; // Configuration.SCREENLAYOUT_SIZE_XLARGE;  // upgrade to HC SDK to get this
        Configuration config = context.getResources().getConfiguration();
        return (config.screenLayout & xlargeBit) == xlargeBit;
    }
}
