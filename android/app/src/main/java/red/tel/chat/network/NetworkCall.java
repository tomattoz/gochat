package red.tel.chat.network;


import android.util.Log;

import java.util.UUID;

import red.tel.chat.Model;
import red.tel.chat.Types;
import red.tel.chat.office365.Constants;

public class NetworkCall implements Types.SessionProtocol {
    private static final String TAG = NetworkCall.class.getSimpleName();
    private NetworkCallInfo networkCallInfo;

    public NetworkCall(NetworkCallInfo networkCallInfo) {
        this.networkCallInfo = networkCallInfo;
    }

    //bat dau gui du lieu cuoc goi
    @Override
    public void start() {
        Log.d(TAG, "start: ");
        startCapture(getNetworkCallInfo().from(), getNetworkCallInfo().to());
    }

    //ket thuc gui du lieu cuoc goi
    @Override
    public void stop() {
        Log.d(TAG, "stop: ");

    }
    public String counterpart() {
        return getNetworkCallInfo().from();
    }

    // TODO: 9/11/17
    public NetworkCallInfo startCapture(String from, String to) {
        return new NetworkCallInfo(getNetworkCallInfo().proposal, null, null);
    }

    /*private NetworkAudio.NetworkAudioSessionInfo startAudioCapture(IO.IOID id) {
        NetworkAudio.NetworkAudioSessionInfo audioSessionInfo = new NetworkAudio.NetworkAudioSessionInfo()
    }*/

    ///////////////////////
    public NetworkCallProposalInfo callAudioAsync(String to) {
        return callAsync(to, true, false);
    }

    public NetworkCallProposalInfo callVideoAsync(String to) {
        return callAsync(to, true, true);
    }

    private NetworkCallProposalInfo callAsync(String to, boolean audio, boolean video) {
        String id = UUID.fromString(Constants.CLIENT_ID).toString();
        NetworkCallProposalInfo info = new NetworkCallProposalInfo(id,
                Model.shared().getUsername(),
                to,
                audio,
                video);
        OutgoingCallProposalController.getInstance().start(info);
        return info;
    }

    public NetworkCall() {
    }

    private static volatile NetworkCall ourInstance = null;

    public static NetworkCall getInstance() {
        if (ourInstance == null) {
            synchronized (NetworkCall.class) {
                if (ourInstance == null) {
                    ourInstance = new NetworkCall();
                }
            }
        }
        return ourInstance;
    }

    public NetworkCallInfo getNetworkCallInfo() {
        return networkCallInfo;
    }

    public void setNetworkCallInfo(NetworkCallInfo networkCallInfo) {
        this.networkCallInfo = networkCallInfo;
    }
}
