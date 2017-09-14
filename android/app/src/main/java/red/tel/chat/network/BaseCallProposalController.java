package red.tel.chat.network;

import android.os.Handler;
import android.os.Looper;

/**
 * Created by vmodev on 9/13/17.
 */

public class BaseCallProposalController extends
        NetworkSingleCallSessionController<NetworkCallProposal, NetworkCallProposalInfo> {
    private Handler handler;
    private Runnable finalizer;

    //tao moi cuoc goi
    @Override
    protected NetworkCallProposal create(NetworkCallProposalInfo info) {
        return new NetworkOutgoingCallProposal(info);
    }

    @Override
    protected String id(NetworkCallProposalInfo info) {
        return info.id;
    }

    //bat dau tao ket noi cuoc goi
    @Override
    public void start(NetworkCallProposalInfo callProposalInfo) {
        super.start(callProposalInfo);
        if (handler == null) {
            handler = new Handler(Looper.getMainLooper());
        }
        finalizer = new Runnable() {
            @Override
            public void run() {
                timeout(callProposalInfo);
            }
        };
        handler.postDelayed(finalizer, 10000);
    }

    //accept call in
    public void accept(NetworkCallProposalInfo info) {
        if (call != null) {
            call.accept(info);
            if (handler != null) {
                handler.removeCallbacks(finalizer);
                handler = null;
                finalizer = null;
            }
        }
        //call = null;
    }

    public void decline(NetworkCallProposalInfo info) {
        if (call != null) {
            call.decline();
        }
        call = null;
    }

    //auto cancels a 10 second call, if? No reply
    private void timeout(NetworkCallProposalInfo info) {
        stop(info);
    }
}
