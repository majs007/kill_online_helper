package kill.online.helper.zeroTier.events;

public class NodeIDEvent {
    private final long nodeId;

    public NodeIDEvent(long j) {
        this.nodeId = j;
    }

    public long getNodeId() {
        return this.nodeId;
    }
}
