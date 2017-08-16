package red.tel.chat;
import com.jakewharton.rxrelay2.PublishRelay;
import com.jakewharton.rxrelay2.Relay;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

public class RxBus {
    private static volatile RxBus ourInstance = null;
    private final Relay<Object> relay;

    private RxBus() {
        relay = PublishRelay.create().toSerialized();
    }

    public static RxBus getInstance() {
        if (ourInstance == null) {
            synchronized (RxBus.class) {
                if (ourInstance == null) {
                    ourInstance = new RxBus();
                }
            }
        }
        return ourInstance;
    }

    /**
     * Sent event with RxJava
     *
     * @param event
     */
    public synchronized void sendEvent(Object event) {
        relay.accept(event);
    }

    /**
     * Receive event
     *
     * @param clazz
     * @param consumer
     * @param <T>
     * @return object event
     */
    public <T> Disposable receiveEvent(final Class<T> clazz, Consumer<T> consumer) {
        return receive(clazz).subscribe(consumer);
    }

    public <T> Observable<T> receive(final Class<T> clazz) {
        return receive().ofType(clazz);
    }

    public synchronized Observable<Object> receive() {
        return relay;
    }
}
