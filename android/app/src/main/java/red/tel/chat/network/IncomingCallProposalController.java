package red.tel.chat.network;

import red.tel.chat.RxBus;

public class IncomingCallProposalController extends BaseCallProposalController {
    //static nested class
    private IncomingCallProposalController() {
    }

    public static synchronized IncomingCallProposalController getInstance() {
        return IncomingCallProposalController.SingletonHelper.INSTANCE;
    }

    /**
     * @method tao moi du lieu tu cuoc goi den
     * @param info
     * @return
     */
    @Override
    protected NetworkCallProposal create(NetworkCallProposalInfo info) {
        return new NetworkIncomingCallProposal(info);
    }

    @Override
    public void start(NetworkCallProposalInfo callProposalInfo) {
        super.start(callProposalInfo);
        RxBus.getInstance().sendEvent(callProposalInfo);
    }

    private static class SingletonHelper {
        private static final IncomingCallProposalController INSTANCE = new IncomingCallProposalController();
    }
}
