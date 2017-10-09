package red.tel.chat.network;


import android.util.Log;

import java.util.UUID;

import red.tel.chat.Model;
import red.tel.chat.Network;
import red.tel.chat.Types;
import red.tel.chat.io.IO;
import red.tel.chat.office365.Constants;

public abstract class NetworkCall implements Types.SessionProtocol {
    private static final String TAG = NetworkCall.class.getSimpleName();
    private NetworkCallInfo networkCallInfo;
    private IO.IOOutputContext audioOutputContext;
    private IO.IOOutputContext outputContext;
    private IO.IOSessionProtocol audioInputSession;
    private IO.IOSessionProtocol videoInputSession;

    public NetworkCall(NetworkCallInfo networkCallInfo) {
        this.networkCallInfo = networkCallInfo;
    }

    //bat dau gui du lieu cuoc goi
    @Override
    public void start() {
        Log.d(TAG, "start: ");
        outputContext = new IO.IOOutputContext();
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

    public NetworkAudio.NetworkAudioSessionInfo startAudioCapture(IO.IOID id) {
        return null;
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

    public NetworkCall() {}

    public NetworkCallInfo getNetworkCallInfo() {
        return networkCallInfo;
    }

    public void setNetworkCallInfo(NetworkCallInfo networkCallInfo) {
        this.networkCallInfo = networkCallInfo;
    }

    public IO.IODataProtocol startOutput(NetworkAudio.NetworkAudioSessionInfo info) {
        audioOutputContext = audioOutput(info, outputContext);
        return audioOutputContext.data;
    }

    public IO.IODataProtocol startOutput(NetworkVideo.NetworkVideoSessionInfo info) {
        return null;
    }

    private IO.IOOutputContext audioOutput(NetworkAudio.NetworkAudioSessionInfo audioSessionInfo, IO.IOOutputContext context) {
        return null;
    }
}
