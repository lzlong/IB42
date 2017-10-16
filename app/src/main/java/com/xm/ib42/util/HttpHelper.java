package com.xm.ib42.util;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.ProtocolException;
import org.apache.http.client.RedirectHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.json.JSONObject;

import java.net.URI;
import java.security.KeyStore;
import java.util.List;

/**
 * Created by long on 17-1-10.
 */

@SuppressWarnings({"deprecation", "SynchronizeOnNonFinalField", "IndexOfReplaceableByContains", "WeakerAccess"})
public class HttpHelper {
    private static final String TAG = com.xm.ib42.util.HttpHelper.class.getSimpleName();
    private boolean mRedirect;
    private DefaultHttpClient mClient;
    private Object mSyncObject = new Object();

    public HttpHelper() {

    }

    public HttpHelper(boolean redirect) {
        mRedirect = redirect;
    }

    public boolean connect() {
        synchronized (mSyncObject) {
            boolean ret = false;
            mClient = getHttpClient();
            if (mClient != null) {
                if (mRedirect)
                    mClient.setRedirectHandler(mRedirectHandler);
                ret = true;
            }
            return ret;
        }
    }

    public HttpResponse doGet(String url) {
        return doGet(url, null);
    }

    public HttpResponse doGet(String url, List<BasicNameValuePair> dataList) {
        synchronized (mSyncObject) {
            try {
                if (dataList != null) {
                    for (int i = 0; i < dataList.size(); i++) {
                        NameValuePair pair = dataList.get(i);
                        if (i == 0 && url.indexOf("?") < 0) {
                            url += "?" + pair.getName() + "=" + pair.getValue();
                        } else {
                            url += "&" + pair.getName() + "=" + pair.getValue();
                        }
                    }
                }
                HttpGet httpGet = new HttpGet(url);
                return mClient.execute(httpGet);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    public HttpResponse doPost(String url) {
        return doPost(url, (String) null);
    }

    public HttpResponse doPost(String url, List<NameValuePair> dataList) {
        synchronized (mSyncObject) {
            try {
                HttpPost httpPost = new HttpPost(url);
                HttpEntity entity = new UrlEncodedFormEntity(dataList, "UTF-8");
                httpPost.setEntity(entity);
                return mClient.execute(httpPost);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public HttpResponse doPost(String url, List<BasicNameValuePair> dataList, String cookie, String contentType) {
        synchronized (mSyncObject) {
            try {
                HttpPost httpPost = new HttpPost(url);
                httpPost.setHeader(HTTP.CONTENT_TYPE, contentType);
//                httpPost.addHeader("Cookie", cookie);
//                httpPost.setHeader("token", AppApplication.deviceId);
                HttpEntity entity = new UrlEncodedFormEntity(dataList, "UTF-8");
                httpPost.setEntity(entity);
                return mClient.execute(httpPost);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public HttpResponse doPost(String url, List<BasicNameValuePair> dataList, String cookie) {
        synchronized (mSyncObject) {
            try {
                HttpPost httpPost = new HttpPost(url);
                httpPost.addHeader("Cookie", cookie);
                HttpEntity entity = new UrlEncodedFormEntity(dataList, "UTF-8");
                httpPost.setEntity(entity);
                return mClient.execute(httpPost);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public HttpResponse doPost(String url, String content) {
        synchronized (mSyncObject) {
            try {
                HttpPost httpPost = new HttpPost(url);
                httpPost.setHeader("Charset", "UTF-8");
                httpPost.setHeader("Content-Type", "text/xml;charset=UTF-8");
                httpPost.setHeader("Accept-Encoding", "gzip,deflate");
                httpPost.setHeader("SOAPAction", "");
                httpPost.setEntity(new StringEntity(content, "UTF-8"));
                return mClient.execute(httpPost);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    public HttpResponse doPost(String url, JSONObject json) {
        synchronized (mSyncObject) {
            try {
                HttpPost httpPost = new HttpPost(url);
                StringEntity s = new StringEntity(json.toString(), "UTF-8");
                s.setContentType("application/json");
                httpPost.setEntity(s);
                return mClient.execute(httpPost);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public void disconnect() {
        synchronized (mSyncObject) {
            if (mClient != null) {
                mClient.getConnectionManager().shutdown();
            }
        }
    }

    private DefaultHttpClient getHttpClient() {
        try {
            KeyStore trustStore = KeyStore.getInstance(KeyStore
                    .getDefaultType());
            trustStore.load(null, null);
            SSLSocketFactory sf = new SSLSocketFactoryEx(trustStore);
            sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);  //允许所有主机的验证
            HttpParams params = new BasicHttpParams();
            HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
            HttpProtocolParams.setContentCharset(params,
                    HTTP.DEFAULT_CONTENT_CHARSET);
            HttpProtocolParams.setUseExpectContinue(params, true);

            // 设置连接管理器的超时
            ConnManagerParams.setTimeout(params, 10000);
            // 设置连接超时
            HttpConnectionParams.setConnectionTimeout(params, 10000);
            // 设置socket超时
            HttpConnectionParams.setSoTimeout(params, 10000);

            // 设置http https支持
            SchemeRegistry schReg = new SchemeRegistry();
            schReg.register(new Scheme("http", PlainSocketFactory
                    .getSocketFactory(), 80));
            schReg.register(new Scheme("https", sf, 443));

            ClientConnectionManager conManager = new ThreadSafeClientConnManager(
                    params, schReg);

            return new DefaultHttpClient(conManager, params);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private RedirectHandler mRedirectHandler = new RedirectHandler() {
        @Override
        public boolean isRedirectRequested(HttpResponse httpResponse, HttpContext httpContext) {
            return false;
        }

        @Override
        public URI getLocationURI(HttpResponse httpResponse, HttpContext httpContext) throws ProtocolException {
            return null;
        }
    };
}
