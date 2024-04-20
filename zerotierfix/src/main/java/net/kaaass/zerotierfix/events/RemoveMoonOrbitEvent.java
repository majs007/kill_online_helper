package kill.online.helper.zeroTier.events;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 删除 Moon 入轨信息事件
 */
@Data
@AllArgsConstructor
public class RemoveMoonOrbitEvent {

    /**
     * Moon 地址
     */
    private Long moonWorldId;

    /**
     * Moon 种子
     */
    private Long moonSeed;
}
