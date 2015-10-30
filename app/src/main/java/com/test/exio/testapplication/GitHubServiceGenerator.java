package com.test.exio.testapplication;

import android.content.Context;
import android.util.Base64;

import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;

import retrofit.GsonConverterFactory;
import retrofit.Retrofit;

/**
 * Создано: exio Дата: 26.10.2015.
 */
public class GitHubServiceGenerator {
        private static OkHttpClient httpClient = new OkHttpClient();
        private static Retrofit.Builder builder =
                new Retrofit.Builder()
                        .baseUrl(Constants.GIT_API_URL)
                        .addConverterFactory(GsonConverterFactory.create());

        public static <S> S createService(final Context context, Class<S> serviceClass) {
                httpClient.interceptors().add(new Interceptor() {
                    @Override
                    public Response intercept(Interceptor.Chain chain) throws IOException {
                        Request original = chain.request();
                        Request.Builder requestBuilder = original.newBuilder();

                        String creds = Credentials.getBasicAuthority(context);
                        if(creds!=null) {
                            final String basic =
                                    "Basic " + Base64.encodeToString(creds.getBytes(), Base64.NO_WRAP);

                            requestBuilder
                                    .header("Authorization", basic)
                                    .method(original.method(), original.body());
                        }

                        Request request = requestBuilder.build();
                        return chain.proceed(request);
                    }
                });

            Retrofit retrofit = builder.client(httpClient).build();
            return retrofit.create(serviceClass);
        }
}
