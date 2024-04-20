package kill.online.helper.zeroTier.service;

import kill.online.helper.zeroTier.util.InetAddressUtils;

import java.net.InetAddress;

import lombok.Data;

/**
 * 路由记录数据类
 */
@Data
public class Route {
    private final InetAddress address;
    private final int prefix;
    private InetAddress gateway = null;

    public boolean belongsToRoute(InetAddress inetAddress) {
        return this.address.equals(InetAddressUtils.addressToRoute(inetAddress, this.prefix));
    }
}
