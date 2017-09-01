package red.tel.chat;


import android.util.Log;

import java.io.IOException;

import red.tel.chat.generated_protobuf.Voip;
import red.tel.chat.network.NetworkBase;
import red.tel.chat.network.NetworkCall;

public class VoidBackend {
    private static final String TAG = VoidBackend.class.getSimpleName();
    private static NetworkBase.NetworkInput audio;
    private static NetworkBase.NetworkInput video;

    private static volatile VoidBackend ourInstance = null;

    public static VoidBackend getInstance() {
        if (ourInstance == null) {
            synchronized (VoidBackend.class) {
                if (ourInstance == null) {
                    ourInstance = new VoidBackend();
                }
            }
        }
        return ourInstance;
    }

    void onReceiveFromPeer(byte[] binary, String peerId) {
        try {
            Voip voip = Voip.ADAPTER.decode(binary);
            Log.d(TAG, "incoming " + voip.which + " from " + peerId);

            switch (voip.which) {
                case TEXT:
                    Model.shared().incomingFromPeer(voip, peerId);
                    break;
                case AV:
                    break;
                case CALL_PROPOSAL:
                    getsCallProposal(voip);
                    break;
                default:
                    Log.e(TAG, "no handler for " + voip.which);
                    break;
            }
        } catch (Exception exception) {
            Log.e(TAG, exception.getLocalizedMessage());
        }
    }

    private void getsCallProposal(Voip voip) {
        NetworkCall.NetworkCallProposalController.getInstance().start(callProposalInfo(voip));
    }

    private NetworkCall.NetworkCallProposalInfo callProposalInfo(Voip voip) {
        return new NetworkCall.NetworkCallProposalInfo(voip.call.key,
                voip.call.from,
                voip.call.to,
                voip.call.audio,
                voip.call.video);
    }
}
