package red.tel.chat.network;

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

    private static class SingletonHelper {
        private static final IncomingCallProposalController INSTANCE = new IncomingCallProposalController();
    }
}
