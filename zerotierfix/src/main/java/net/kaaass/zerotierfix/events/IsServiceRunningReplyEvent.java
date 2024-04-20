package kill.online.helper.zeroTier.events;

import lombok.Data;

/**
 * 应答服务是否运行事件
 */
@Data
public class IsServiceRunningReplyEvent {
    private final boolean isRunning;
}
