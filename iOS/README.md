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

1. 导入 UGCKit 
   将 UGCKit 文件夹拷贝到工程目录中，并将UGCKit中的UGCKit.xcodeproj拖拽到工程中。
   <img src="https://main.qcloudimg.com/raw/4b8ff842eb939cd920eb16b22424ef22.png" width=800px />

2. 配置依赖关系   
   点击工程的Target, 选择 Build Phase 标签，在 Dependencies 中点击加号，选择UGCKit.framework 和 UGCKitResources，点击 Add。
   <img src="https://main.qcloudimg.com/raw/eadf4d86b3dd62067417d4d449127348.jpg" width=800px />
   
3. 链接 UGCKit.framework 和 SDK
   点击工程的Target, 选择 Build Phase 标签， 在 Link Binary With Libraries 中点击加号，选择UGCKit.framework。
   <img src="https://main.qcloudimg.com/raw/f58b5a64a5074b334b2c97ec010800fc.jpg" width=800px />
   在Finder中打开 SDK 目录，将 SDK 拖动到 Link Binary With Libraries 中。
   <img src="https://main.qcloudimg.com/raw/217de0d27d3fc67152a71d2a1e800647.jpg" width=800px />
   将 SDK 目录下的 FilterResource.bundle 拖动到工程中并勾选 App Target。
   
4. 导入资源
   点击工程的Target, 选择 Build Phase 标签， 展开 Copy Bundle Resources。然后在左侧目录中依次展开UGCKit.xcodeproj、Products，拖动 UGCKitResources.bundle 到 Copy Bundle Resources 中。
   <img src="https://main.qcloudimg.com/raw/fbca78b281f8e87cbbaa036c4f208725.jpg" width=800px />

5. 导入商业版资源（仅用于商业版）
   将商业版 SDK zip包中SDK/Resouce 拖动到工程中，选择“Create groups"并勾选您的Target，点击Finish。
   <img src="https://main.qcloudimg.com/raw/5ae899aff95984bf34839653ad2c4b51.jpg" width=800px />

   <img src="https://main.qcloudimg.com/raw/fba634dc19e9e0bf3443f1451a9a2b60.jpg" width=800px />

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
   