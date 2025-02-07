# 简介

kill联机助手是一款使用zerotier One开源内核，并参考ZerotierFix项目，实现的自定义的第三方zerotier客户端。

zerotier One是一款虚拟局域网组网软件，可以实现多个设备在不同网络环境下，通过zerotier
One的网络组网，实现局域网内的设备互相访问。

kill联机助手基于zerotier的网络组网，可以使kill联机玩家同处于一个虚拟局域网内，实现局域网联机。在此基础之上，还专门为联机玩家提供了一些有趣的功能。

# 功能

- ✅ 房间状态列表
- ✅ 在线玩家列表
- ✅ 房间密码、黑名单
- ✅ 房主拉黑、禁言权限
- ✅ 自定义房间名、房间描述
- ✅ 弹幕消息、语音消息
- ✅ 大号指定位置动态贴图
- ✅ 导入自定义贴图

# 悬浮窗Tips

- 第一行表情共10个，为常用表情
- 长按表情可发送大号指定位置贴图
- 超大贴图双击可快速关闭
- 贴图虽好，不要刷屏哦，所有消息限制10s发送间隔
- 房主【点击】玩家标签行 可对其拉黑、禁言，有图标显示其状态
- 语音消息目前不完善（功力不足），开始录音时请确保将联机助手以【小窗】浮在界面上，这样录音正常，否则无法录音，录音后可以拖动小窗藏进屏幕边缘，下次录音时再唤出来

# Q&A

## 【守序善良】苹果用户能联机吗？

苹果用户可以在 App Store 下载 ZeroTier One 客户端，并加入`a09acf02339ffab1`
，这样可以与其他玩家联机，但苹果用户无法在游戏中开房，只能作为加入房间的玩家。Android用户使用官方客户端同理。官方客户端不会处理游戏房主发出的广播包，其他成员搜不到房间。
*********

## 【守序善良】为什么有时候会闪退？

### 游戏本身bug，已知：

- 加入房间后在等待界面退出房间会闪退
- 游戏结算界面退出会闪退，而在游戏过程中退出不会闪退
- 搜索房间界面取消再重新搜索会闪退
- 先创建房间再退出，然后搜索房间会闪退

### 网络问题，确保设备具有公网ip

- 当网络波动时（延迟超高，还丢包），游戏疑似会因此而闪退
- 具有公网ipv4(绝大部分人不可能有)
- 具有公网ipv6(
  移动蜂窝网络100%分配ipv6地址，但是家庭宽带不一定有，尤其是使用较旧的光猫和路由器，推荐使用流量联机)
- 运营商发癫，跨网数据，又是UDP流量，debuff拉满，让你见识下Qos的威力，比较玄学，分时间和地区

*********

## 【守序善良】为什么会卡顿？

- 两台具有公网ip的设备，通过zerotier组网后，可以直连（peer to peer），不需要中继服务器，延迟低。
- 若设备无公网ip，很可能需要zerotier官方服务器（位于国外）帮忙中转数据，数据包将周游世界，喜提十倍延迟。
- 自建Moon节点，是zertier官方提供的解决方案。

*********

## 【混沌邪恶】怎么联机啊？

哦，我的上帝，点那个超大的【未启用】按钮，然后授予该死的VPN权限
*********

## 【混沌邪恶】我搜不到房啊？

看在上帝的份上，哦不，我是说，你要等有人开房之后，再搜索。
*********

## 【混沌邪恶】zerotier全是英文，看不懂啊，怎么添加网络？

![😡](https://i2.hdslb.com/bfs/archive/9f926ed9365cf4ffcdb6d5175b1e6c20cef69aaa.jpg@420w_238h_1c_!web-video-rcmd-cover.avif )
