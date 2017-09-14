package red.tel.chat.network;


public class NetworkCallSessionController<T, I> {
    protected T create(I info) {
        return null;
    }

    protected String id(I info) {
        return "";
    }

    protected T call(I info) {
        return null;
    }

    public void start(I info) {
    }

    public void stop(I info) {
    }
}
