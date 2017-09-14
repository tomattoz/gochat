package red.tel.chat.network;


import android.util.Log;

public class NetworkCallProposal implements NetworkCallProposalProtocol {
    private static final String TAG = NetworkCallProposal.class.getSimpleName();
    NetworkCallProposalInfo callProposalInfo;


    public NetworkCallProposal(NetworkCallProposalInfo callProposalInfo) {
        this.callProposalInfo = callProposalInfo;
    }

    /**
     * tao mot cuoc goi moi
     */
    @Override
    public void start() {}

    /**
     * dung cuoc goi di
     */
    @Override
    public void stop() {}

    /**
     * @method  dong y cuoc goi den
     * @param info
     */
    @Override
    public void accept(NetworkCallProposalInfo info) {
       NetworkCall.getInstance().setNetworkCallInfo(new NetworkCallInfo(info));
        Log.d(TAG, "accept: ");
    }

    /**
     * tu doi cuoc goi den
     */
    @Override
    public void decline() {}
}
