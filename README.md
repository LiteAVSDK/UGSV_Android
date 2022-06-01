# 腾讯云短视频终端组件 UGSV SDK

## SDK 下载

访问 Github 较慢的客户可以考虑使用国内下载地址：  [DOWNLOAD](https://cloud.tencent.com/document/product/584/9366) 。

| 所属平台 | Zip下载                                                      | SDK集成指引                                                 |
| -------- | ------------------------------------------------------------ | ----------------------------------------------------------- |
| iOS      | [下载](https://liteav.sdk.qcloud.com/download/latest/TXLiteAVSDK_UGC_iOS_latest.zip) | [DOC](https://cloud.tencent.com/document/product/584/11638) |
| Android  | [下载](https://liteav.sdk.qcloud.com/download/latest/TXLiteAVSDK_UGC_Android_latest.zip) | [DOC](https://cloud.tencent.com/document/product/584/11631) |

### Version 8.8 @ 2022.06.01
- iOS&Android: UGC上传支持quic&升级cos sdk版本，提供定制分片大小&并发上传数量的接口
- iOS&Android：适配10.1 SDK需要微调的代码

  

**SDK升级说明**

UGSV SDK 移动端 10.1 版本采用“腾讯视频”同款播放内核打造，视频播放能力获得全面优化升级。

同时从该版本开始将增加对“视频播放”功能模块的授权校验，**如果您的APP已经拥有短视频 License 授权，当您升级至10.1 版本后仍可以继续正常使用，**不受到此次变更影响，您可以登录 [腾讯云视立方控制台](https://console.cloud.tencent.com/vcube) 查看您当前的 License 授权信息。

如果您在此之前从未获得过上述License授权**，且需要使用新版本SDK（10.1及其更高版本）中的点播播放功能，则需购买指定 License 获得授权**，详情参见[授权说明](https://cloud.tencent.com/document/product/584/54333)；若您无需使用相关功能或未升级至最新版本SDK，将不受到此次变更的影响。



## 问题反馈

为了更好的了解您使用 UGSVSDK 所遇到的问题，方便快速有效定位解决  UGSVSDK 问题，希望您按如下反馈指引反馈issue，方便我们尽快解决您的问题  
[UGSVSDK issue反馈指引](https://github.com/tencentyun/UGSVSDK/blob/master/UGSVSDK%20issue有效反馈模板.md)

## Demo 体验地址

<table style="text-align:center;vertical-align:middle;">
  <tr>
    <th style="text-align:center"><b>iOS 版</b></th>
    <th style="text-align:center"><b>Android 版</b></th>
  </tr>
  <tr>
    <td style="width:300px;height:300px;text-align:center"><img src="https://liteav.sdk.qcloud.com/doc/res/ugc/picture/xiaoshipin_app_qr_code_ios.png" /></td>
    <td style="width:300px;height:300px;text-align:center"><img src="https://liteav.sdk.qcloud.com/doc/res/ugc/picture/xiaoshipin_app_qr_code_android.png" /></td>
  </tr>
</table>


<div align="left">
<img src="https://main.qcloudimg.com/raw/1e90b3e4c4eda655c4994bd5da293c97.png" height="391" width="220" >
<img src="https://main.qcloudimg.com/raw/6d2996c86edf6a796b681580f3c1fb05.png" height="391" width="220" >
<img src="https://main.qcloudimg.com/raw/a06caa5a974ff7b129255710840148e1.png" height="391" width="220" >
</div>

<div align="left">
<img src="https://main.qcloudimg.com/raw/b7ada99f174e21e09e7b9c78e96c1858.png" height="391" width="220" >
<img src="https://main.qcloudimg.com/raw/dc697e4f7074e6e5477dab0b1746ea87.png" height="391" width="220" >
<img src="https://main.qcloudimg.com/raw/db67663711a7680886a86534e4937e54.png" height="391" width="220" >
</div>












