public class Message {
    // Message types
    public final static int MESSAGE_TYPE_CONFIRM = 0;
    public final static int MESSAGE_TYPE_REQUEST = 1;
    public final static int MESSAGE_TYPE_SEND = 2;
    public final static int MESSAGE_TYPE_RECEIVE = 3;
    public final static int MESSAGE_TYPE_NOTFOUND = 4;

    // variables
    final int msgType;
    final String msgTarget;
    final String msgTargertIP;
    final byte[] msg;

    public Message(int msgType, String msgTarget, byte[] msg, String msgTargetIP) {
        this.msgType = msgType;
        this.msgTarget = msgTarget;
        this.msg = msg;
        this.msgTargertIP = msgTargetIP;
    }
}