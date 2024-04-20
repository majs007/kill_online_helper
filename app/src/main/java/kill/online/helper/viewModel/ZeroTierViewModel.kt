package kill.online.helper.viewModel

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.ViewModel
import com.zerotier.sdk.Peer
import kill.online.helper.data.DnsConfig
import kill.online.helper.data.ZeroTierConfig
import kill.online.helper.zeroTier.events.NodeStatusEvent
import kill.online.helper.zeroTier.model.Network
import kill.online.helper.zeroTier.service.ZeroTierOneService

import org.greenrobot.eventbus.EventBus

class ZeroTierViewModel : ViewModel() {
    private val eventBus: EventBus = EventBus.getDefault()
    private val networkConfig: ZeroTierConfig =
        ZeroTierConfig(
            "a09acf02339ffab1", true, DnsConfig.NoDNS,
            null, null, null, null
        )
    private var isBound = false
    private var boundService: ZeroTierOneService? = null
    private val connection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName, iBinder: IBinder) {
            this@ZeroTierViewModel.boundService =
                (iBinder as ZeroTierOneService.ZeroTierBinder).service
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            this@ZeroTierViewModel.boundService = null
            this@ZeroTierViewModel.isBound = false
        }
    }
    private val vpnAuthLauncher: ActivityResultLauncher<Intent>? = null
    private val networks = mutableListOf<Network>()
    private val peers = mutableListOf<Peer>()
    private var onNodeStatus: (event: NodeStatusEvent) -> Unit = {}


}