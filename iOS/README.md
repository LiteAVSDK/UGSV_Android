# iOS快速接入
### 简介

UGCKit 是在腾讯云短视频SDK基础上构建的一套UI组建库。

使用 UGCKit，仅需几步就可以在您的 App 中加入短视频的相关功能。

UGCKit 的开发环境要求如下:
- Xcode 10 及以上
- iOS 9.0 及以上


### 快速集成

#### 一、导入

在使用 UGCKit 之前，需要将 UGCKit 集成到您的工程当中

1. 项目配置：

    i. 项目中使用 cocoapods，根据实际情况选择其中一种操作：
    
      - 在项目根目录，执行 `pod init && pod install`，可得到 Podfile 文件。
      - 把 **BeautySettingKit** 和 **UGCKit** 文件夹拷贝到项目根目录下（Podfile 同级目录）。
     
    ii. 打开 Podfile 文件，增加：
    ```
     pod 'BeautySettingKit', :path => 'BeautySettingKit/BeautySettingKit.podspec'
     pod 'UGCKit', :path => 'UGCKit/UGCKit.podspec', :subspecs => ["UGC"]   #subspecs 根据SDK来选择
    ```
     
    iii. 执行`pod install`，并打开`项目名.xcworkspace`，可以看到在`Pods/Development Pods`目录下已有`UGCKit BeautySettingKit`。
2. 导入商业版资源（仅用于商业版）：
将商业版 SDK ZIP 包中 EnterprisePITU（在 `App/AppCommon` 目录下）文件夹拖动到工程中，选择 **Create groups** 并勾选您的 Target，单击 **Finish**。
### 使用

1. 录制
   `UGCKitRecordViewController` 提供了完整的录制功能，您只需实例化这个控制器后展现在界面中即可。
   ```
   UGCKitRecordViewController *recordViewController = [[UGCKitRecordViewController alloc] initWithConfig:nil theme:nil];
   [self.navigationController pushViewController:recordViewController]
   ```
   录制后的结果将通过 completion block 回调，示例如下：
   ```
   recordViewController.completion = ^(TCUGCResult *result) {
       if (result.error) {
           // 录制出错
       		[self showAlertWithError:error];
       } else {
           if (result.cancelled) {
               // 用户取消录制，退出录制界面
               [self.navigationController popViewControllerAnimated:YES];
   	      } else {
               // 录制成功, 用结果进行下一步处理
               [self processRecordedVideo:result.media];
           }
       }
   };
   ```
   
2. 编辑
   `UGCKitEditViewController` 提供了完整的图片转场和视频编辑功能，实例化时需要传入待编辑的媒体对象，以处理录制结果为例，示例如下：
   ```
   - (void)processRecordedVideo:(UGCKitMedia *)media {
       // 实例化编辑控制器
       UGCKitEditViewController *editViewController = [[UKEditViewController alloc] initWithMedia:media conifg:nil theme:nil];
       // 展示编辑控制器
       [self.navigationController pushViewController:editViewController animated:YES];
   ```
   编辑后的结果将通过 completion block 回调，示例如下：
   ```
       editViewController.completion = ^(TCUGCResult *result) {
       if (result.error) {
           // 出错
       		[self showAlertWithError:error];
       } else {
           if (result.cancelled) {
               // 用户取消录制，退出编辑界面
               [self.navigationController popViewControllerAnimated:YES];
   	      } else {
               // 编辑保存成功, 用结果进行下一步处理
               [self processEditedVideo:result.path];
           }
       }
   }
   ```
   
3. 从相册中选择视频或图片
   `UGCKitMediaPickerViewController`用来处理媒体的选择与合并，当选择多个视频时，将会返回拼接后的视频。示例如下:
   
   ```
   // 初始化配置
   UGCKitMediaPickerConfig *config = [[UGCKitMediaPickerConfig alloc] init];
   config.mediaType = UGCKitMediaTypeVideo;//选则视频
   config.maxItemCount = 5;                //最多选5个

   // 实例化媒体选择器
   UGCKitMediaPickerViewController *mediaPickerViewController = [[UGCKitMediaPickerViewController alloc] initWithConfig:config theme:nil];
   // 展示媒体选择器
   [self presentViewController:mediaPickerViewController animated:YES completion:nil];
   ```
   选择的结果将通过 completion block 回调，示例如下：
   ```
   mediaPickerViewController.completion = ^(UGCKitResult *result) {
     if (result.error) {
         // 出错
         [self showAlertWithError:error];
     } else {
          if (result.cancelled) {
               // 用户取消录制，退出选择器界面
               [self dismissViewControllerAnimated:YES completion:nil];
   	      } else {
               // 编辑保存成功, 用结果进行下一步处理
               [self processEditedVideo:result.media];
          }
     }
   }
   ```
   
4. 裁剪
   `UGCKitCutViewController`提供视频的裁剪功能，与编辑接口相同，在实例化时传入媒体对象，在completion中处理剪辑结果即可。
   示例:
   ```
   UGCKitMedia *media = [UGCKitMedia mediaWithVideoPath:@"<#视频路径#>"];
   UGCKitCutViewController *cutViewController = [[UGCKitCutViewController alloc] initWithMedia:media theme:nil];
   cutViewController.completion = ^(UGCKitResult *result) {
        if (!result.cancelled && !result.error) {
             [self editVideo:result.media];
        } else {
             [self.navigationController popViewControllerAnimated:YES];
        }
   }
   [self.navigationController pushViewController: cutViewController]
   ```
   
