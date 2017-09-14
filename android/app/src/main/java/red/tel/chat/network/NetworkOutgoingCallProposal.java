package red.tel.chat.network;


import red.tel.chat.VoipBackend;

public class NetworkOutgoingCallProposal extends NetworkCallProposal {
    public NetworkOutgoingCallProposal(NetworkCallProposalInfo callProposalInfo) {
        super(callProposalInfo);
    }

    @Override
    public void start() {
        super.start();
        VoipBackend.getInstance().sendCallProposal(callProposalInfo.to, callProposalInfo);
    }

    //huy cuoc goi khi dang cho ket noi
    @Override
    public void stop() {
        VoipBackend.getInstance().sendCallCancel(callProposalInfo.to, callProposalInfo);
        super.stop();
    }

    /**
     * @method nhan tin hieu dong y cuoc goi di
     * @param info
     */
    @Override
    public void accept(NetworkCallProposalInfo info) {
        NetworkCall.getInstance().setNetworkCallInfo(new NetworkCallInfo(info));

        NetworkCallController.getInstance().start(NetworkCall.getInstance().getNetworkCallInfo());
        /*new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                NetworkOutgoingCall.getInstance().start();
            }
        },1000);*/
        super.accept(info);
    }
}
