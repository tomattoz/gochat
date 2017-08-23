package red.tel.chat.office365.api;

import io.reactivex.Observable;
import red.tel.chat.office365.model.ContactsModel;
import retrofit2.http.GET;
import retrofit2.http.Query;


public interface IContactsApi {
    @GET("me/contacts")
    Observable<ContactsModel> getContacts(@Query("$top")int top, @Query("$skip") int skip);
}
