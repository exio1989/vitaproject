package com.test.exio.testapplication;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.squareup.okhttp.ResponseBody;


/**
 * Создано: exio1989 Дата: 27.10.2015.
 */
public class GitHubService{
    public static final String TAG="GitHubService";
    private static GitHub mGithub;

    public static GitHub getService(Context context) {
        if (mGithub == null) {
            mGithub =  GitHubServiceGenerator.createService(context,GitHub.class);
        }
        return mGithub;
    }

    public static void processServiceError(Context appContext,int code, ResponseBody body){
        try {
            String err="";
            switch (code) {
                case 403://403
                    err=appContext.getString(R.string.http_git_403);
                    break;
                case 401://401 unauthorized
                    err=appContext.getString(R.string.http_git_401);
                    Credentials.clearBasicAuthority(appContext);
                    break;
                default:
                    err=appContext.getString(R.string.http_git_unknown_error);
                    break;
            }

            Toast.makeText(appContext, err, Toast.LENGTH_LONG)
                    .show();
        } catch (Exception ex) {
            Log.d(TAG, ex.getMessage());
        }
    }
}