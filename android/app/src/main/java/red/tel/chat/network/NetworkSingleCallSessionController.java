package red.tel.chat.network;

import java.util.Objects;

import red.tel.chat.Types;


public class NetworkSingleCallSessionController<T extends Types.SessionProtocol, I>
        extends NetworkCallSessionController<T, I> {
    public T call;
    public I callInfo;

    @Override
    public void start(I info) {
        super.start(info);
        stop();
        //tao moi cuoc goi
        call = create(info);
        callInfo = info;
        try {
            if (call != null) {
                call.start();//bat dau tao ket noi cuoc goi {@link NetworkOutgoingCallProposal.start()}
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop(I info) {
        super.stop(info);
        if (this.callInfo == null) {
            return;
        }
        I callInfo = this.callInfo;
        if (!Objects.equals(id(callInfo), id(info))) {
            return;
        }

        if (call != null) {
            call.stop();
        }
        this.call = null;
        this.callInfo = null;
    }

    private void stop() {
        if (callInfo == null) {
            return;
        }
        stop(callInfo);
    }

    @Override
    protected T call(I info) {
        I callInfo = this.callInfo;
        if (!Objects.equals(id(callInfo), id(info))) {
            return null;
        }
        return call;
    }
}