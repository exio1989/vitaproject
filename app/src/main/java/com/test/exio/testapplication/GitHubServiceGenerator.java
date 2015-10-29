package com.test.exio.testapplication;

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

        public static <S> S createService(Class<S> serviceClass) {
            return createService(serviceClass, null, null);
        }

        public static <S> S createService(Class<S> serviceClass, String username, String password) {
            if (username != null && password != null) {
                String credentials = username + ":" + password;
                final String basic =
                        "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);

                httpClient.interceptors().add(new Interceptor() {
                    @Override
                    public Response intercept(Interceptor.Chain chain) throws IOException {
                        Request original = chain.request();

                        Request.Builder requestBuilder = original.newBuilder()
                                .header("Authorization", basic)
                                .method(original.method(), original.body());

                        Request request = requestBuilder.build();
                        return chain.proceed(request);
                    }
                });
            }

            Retrofit retrofit = builder.client(httpClient).build();
            return retrofit.create(serviceClass);
        }
}
