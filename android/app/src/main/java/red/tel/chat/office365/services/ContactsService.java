package red.tel.chat.office365.services;

import com.ihsanbal.logging.Level;
import com.ihsanbal.logging.LoggingInterceptor;

import io.reactivex.Observable;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.internal.platform.Platform;
import red.tel.chat.BuildConfig;
import red.tel.chat.office365.AuthInterceptor;
import red.tel.chat.office365.Constants;
import red.tel.chat.office365.api.IContactsApi;
import red.tel.chat.office365.model.ContactsModel;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;


public class ContactsService {
    private OkHttpClient okHttpClient() {
        return new OkHttpClient.Builder()
                .addInterceptor(logInterceptor())
                .addInterceptor(new AuthInterceptor())
                .build();
    }

    private Interceptor logInterceptor() {
        return new LoggingInterceptor.Builder()
                .loggable(BuildConfig.DEBUG)
                .setLevel(Level.BASIC)
                .log(Platform.INFO)
                .request("Request")
                .response("Response")
                .addHeader("version", BuildConfig.VERSION_NAME)
                .build();
    }

    private IContactsApi buildApi() {
        return new Retrofit.Builder()
                .baseUrl(Constants.CONTACT_ENDPOINT)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient())
                .build()
                .create(IContactsApi.class);
    }

    public Observable<ContactsModel> getContacts(int skipPage) {
        return buildApi().getContacts(skipPage);
    }
}
