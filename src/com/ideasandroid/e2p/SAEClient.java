package com.ideasandroid.e2p;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

public class SAEClient {

	static final String BASE_URL = "http://e2pserver.sinaapp.com";
    private static final String AUTH_URL = BASE_URL + "/login.php";
    private static final String AUTH_TOKEN_TYPE = "ah";

    private final Context mContext;
    private final String mAccountName;

    private static final String TAG = "SAEClient";

    public SAEClient(Context context, String accountName) {
        this.mContext = context;
        this.mAccountName = accountName;
    }

    public HttpResponse makeRequest(String urlPath, List<NameValuePair> params) throws Exception {
        HttpResponse res = makeRequestNoRetry(urlPath, params, false);
        if (res.getStatusLine().getStatusCode() == 500) {
            res = makeRequestNoRetry(urlPath, params, true);
        }
        return res;
    }

    private HttpResponse makeRequestNoRetry(String urlPath, List<NameValuePair> params, boolean newToken)
            throws Exception {
        // Get auth token for account
        Account account = new Account(mAccountName, "com.google");
        String authToken = getAuthToken(mContext, account);

        if (newToken) {  // invalidate the cached token
            AccountManager accountManager = AccountManager.get(mContext);
            accountManager.invalidateAuthToken(account.type, authToken);
            authToken = getAuthToken(mContext, account);
        }

        // Get ACSID cookie
      //创建一个http客户端
        HttpClient client=new DefaultHttpClient();
        //创建一个POST请求
        HttpPost httpPost=new HttpPost(BASE_URL+urlPath);
        //组装数据放到HttpEntity中发送到服务器
        final List<BasicNameValuePair> dataList = new ArrayList<BasicNameValuePair>();
        dataList.add(new BasicNameValuePair("authToken", authToken));
        dataList.add(new BasicNameValuePair("mAccountName", mAccountName));
        params.addAll(dataList);
        HttpEntity entity = new UrlEncodedFormEntity(params, "UTF-8");
        httpPost.setEntity(entity);
        //向服务器发送POST请求并获取服务器返回的结果，可能是增加成功返回商品ID，或者失败等信息
        HttpResponse response=client.execute(httpPost);
        return response;
    }

    private String getAuthToken(Context context, Account account) throws PendingAuthException {
        String authToken = null;
        AccountManager accountManager = AccountManager.get(context);
        try {
            AccountManagerFuture<Bundle> future =
                    accountManager.getAuthToken (account, AUTH_TOKEN_TYPE, false, null, null);
            Bundle bundle = future.getResult();
            authToken = bundle.getString(AccountManager.KEY_AUTHTOKEN);
            if (authToken == null) {
                throw new PendingAuthException(bundle);
            }
        } catch (OperationCanceledException e) {
            Log.w(TAG, e.getMessage());
        } catch (AuthenticatorException e) {
            Log.w(TAG, e.getMessage());
        } catch (IOException e) {
            Log.w(TAG, e.getMessage());
        }
        return authToken;
    }

    public class PendingAuthException extends Exception {
        private static final long serialVersionUID = 1L;
        private final Bundle mAccountManagerBundle;
        public PendingAuthException(Bundle accountManagerBundle) {
            super();
            mAccountManagerBundle = accountManagerBundle;
        }

        public Bundle getAccountManagerBundle() {
            return mAccountManagerBundle;
        }
    }
}
