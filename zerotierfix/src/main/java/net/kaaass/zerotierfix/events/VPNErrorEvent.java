package kill.online.helper.zeroTier.events;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * VPN 错误事件
 */
@Data
@AllArgsConstructor
public class VPNErrorEvent {
    private final String message;
}