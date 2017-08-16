package red.tel.chat.office365;

import android.support.annotation.NonNull;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import red.tel.chat.Model;


public class AuthInterceptor implements Interceptor {
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request request = chain.request()
                .newBuilder()
                .addHeader("Authorization", "Bearer " + Model.shared().getAccessToken())
                .addHeader("Content-Type", "application/json")
                .build();
        return chain.proceed(request);
    }
}
