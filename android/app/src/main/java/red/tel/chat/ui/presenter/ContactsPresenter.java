package red.tel.chat.ui.presenter;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;
import red.tel.chat.office365.model.ContactsModel;
import red.tel.chat.office365.services.ContactsService;

/**
 * Created by hoanghiep on 8/17/17.
 */

public class ContactsPresenter extends ContactsContract.Presenter {
    private ContactsContract.ContactsView view;
    private CompositeDisposable compositeDisposable;
    @Override
    public void attachView(ContactsContract.ContactsView view) {
        this.view = view;
        compositeDisposable = new CompositeDisposable();
    }

    @Override
    public void detachView() {
        this.view = null;
        if (compositeDisposable != null) {
            compositeDisposable.dispose();
        }
    }

    @Override
    public void getListContacts(int nextPage) {
        new ContactsService().getContacts(nextPage)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ContactsModel>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        compositeDisposable.add(d);
                    }

                    @Override
                    public void onNext(ContactsModel contactsModel) {
                        if (view != null) {
                            view.showListContact(contactsModel);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (view != null) {
                            view.onError(e);
                        }
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }
}
