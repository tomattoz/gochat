package red.tel.chat.ui.presenter;


import red.tel.chat.office365.model.ContactsModel;
import red.tel.chat.ui.views.BaseView;

public interface ContactsContract {
    interface ContactsView extends BaseView {
        void showListContact(ContactsModel contactsModel);

        void onError(Throwable e);
    }

    abstract class Presenter implements BasePresenter<ContactsContract.ContactsView> {
        public abstract void getListContacts(int nextPage);
    }
}
