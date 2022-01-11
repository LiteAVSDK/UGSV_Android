//
//  TCBasePlayViewController.m
//  TCLVBIMDemo
//
//  Created by annidyfeng on 2017/9/15.
//  Copyright © 2017年 tencent. All rights reserved.
//

#import "TCBasePlayViewController.h"
//#import <UShareUI/UMSocialUIManager.h>
//#import <UMSocialCore/UMSocialCore.h>
#import "SDKHeader.h"
#import "SDKHeader.h"
#import <mach/mach.h>
#import <SDWebImage/UIImageView+WebCache.h>
#import "AppDelegate.h"
#import "TCConstants.h"
#import <Accelerate/Accelerate.h>
#import "TCLoginModel.h"
#import "NSString+Common.h"
#import "TCVideoPublishController.h"
#import "TCUserInfoModel.h"
#import "TCUtil.h"

NSString *const kTCLivePlayError = @"kTCLivePlayError";

@interface TCBasePlayViewController ()

@end

@implementation TCBasePlayViewController

-(id)initWithPlayInfo:(TCLiveInfo *)info  videoIsReady:(videoIsReadyBlock)videoIsReady
{
    self = [super init];
    if (self) {
        _videoIsReady = videoIsReady;
        _liveInfo     = info;
    }
    return self;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

/*
#pragma mark - Navigation

// In a storyboard-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.
}
*/


//创建高斯模糊效果图片
-(UIImage *)gsImage:(UIImage *)image withGsNumber:(CGFloat)blur
{
    if (blur < 0.f || blur > 1.f) {
        blur = 0.5f;
    }
    int boxSize = (int)(blur * 40);
    boxSize = boxSize - (boxSize % 2) + 1;
    CGImageRef img = image.CGImage;
    vImage_Buffer inBuffer, outBuffer;
    vImage_Error error;
    void *pixelBuffer;
    //从CGImage中获取数据
    CGDataProviderRef inProvider = CGImageGetDataProvider(img);
    CFDataRef inBitmapData = CGDataProviderCopyData(inProvider);
    //设置从CGImage获取对象的属性
    inBuffer.width = CGImageGetWidth(img);
    inBuffer.height = CGImageGetHeight(img);
    inBuffer.rowBytes = CGImageGetBytesPerRow(img);
    inBuffer.data = (void*)CFDataGetBytePtr(inBitmapData);
    pixelBuffer = malloc(CGImageGetBytesPerRow(img) * CGImageGetHeight(img));
    if(pixelBuffer == NULL)
        NSLog(@"No pixelbuffer");
    outBuffer.data = pixelBuffer;
    outBuffer.width = CGImageGetWidth(img);
    outBuffer.height = CGImageGetHeight(img);
    outBuffer.rowBytes = CGImageGetBytesPerRow(img);
    error = vImageBoxConvolve_ARGB8888(&inBuffer, &outBuffer, NULL, 0, 0, boxSize, boxSize, NULL, kvImageEdgeExtend);
    if (error) {
        NSLog(@"error from convolution %ld", error);
    }
    CGColorSpaceRef colorSpace = CGColorSpaceCreateDeviceRGB();
    CGContextRef ctx = CGBitmapContextCreate( outBuffer.data, outBuffer.width, outBuffer.height, 8, outBuffer.rowBytes, colorSpace, kCGImageAlphaNoneSkipLast);
    CGImageRef imageRef = CGBitmapContextCreateImage (ctx);
    UIImage *returnImage = [UIImage imageWithCGImage:imageRef];
    //clean up
    CGContextRelease(ctx);
    CGColorSpaceRelease(colorSpace);
    free(pixelBuffer);
    CFRelease(inBitmapData);
    CGImageRelease(imageRef);
    return returnImage;
}

/**
 *缩放图片
 */
-(UIImage*)scaleImage:(UIImage *)image scaleToSize:(CGSize)size{
    UIGraphicsBeginImageContext(size);
    [image drawInRect:CGRectMake(0, 0, size.width, size.height)];
    UIImage* scaledImage = UIGraphicsGetImageFromCurrentImageContext();
    UIGraphicsEndImageContext();
    return scaledImage;
}

/**
 *裁剪图片
 */
-(UIImage *)clipImage:(UIImage *)image inRect:(CGRect)rect{
    CGImageRef sourceImageRef = [image CGImage];
    CGImageRef newImageRef = CGImageCreateWithImageInRect(sourceImageRef, rect);
    UIImage *newImage = [UIImage imageWithCGImage:newImageRef];
    CGImageRelease(newImageRef);
    return newImage;
}
/**
 @method 获取指定宽度width的字符串在UITextView上的高度
 @param textView 待计算的UITextView
 @param width 限制字符串显示区域的宽度
 @result float 返回的高度
 */
- (float) heightForString:(UITextView *)textView andWidth:(float)width{
    CGSize sizeToFit = [textView sizeThatFits:CGSizeMake(width, MAXFLOAT)];
    return sizeToFit.height;
}


- (void) toastTip:(NSString*)toastInfo
{
    CGRect frameRC = [[UIScreen mainScreen] bounds];
    frameRC.origin.y = frameRC.size.height - 110;
    frameRC.size.height -= 110;
    __block UITextView * toastView = [[UITextView alloc] init];
    
    toastView.editable = NO;
    toastView.selectable = NO;
    
    frameRC.size.height = [self heightForString:toastView andWidth:frameRC.size.width];
    
    toastView.frame = frameRC;
    
    toastView.text = toastInfo;
    toastView.backgroundColor = [UIColor whiteColor];
    toastView.alpha = 0.5;
    
    [self.view addSubview:toastView];
    
    dispatch_time_t popTime = dispatch_time(DISPATCH_TIME_NOW, 2 * NSEC_PER_SEC);
    
    dispatch_after(popTime, dispatch_get_main_queue(), ^(){
        [toastView removeFromSuperview];
        toastView = nil;
    });
}
/*
- (void)shareLive {
    __weak typeof(self) weakSelf = self;
    //显示分享面板
    [UMSocialUIManager showShareMenuViewInWindowWithPlatformSelectionBlock:^(UMSocialPlatformType platformType, NSDictionary *userInfo) {
          [weakSelf shareDataWithPlatform:platformType];
    }];
}

- (void)shareDataWithPlatform:(UMSocialPlatformType)platformType
{
    // 创建UMSocialMessageObject实例进行分享
    // 分享数据对象
    UMSocialMessageObject *messageObject = [UMSocialMessageObject messageObject];
    
    NSString *title = self.liveInfo.title;
    
    NSString *url = [NSString stringWithFormat:@"%@?userid=%@&type=%d&fileid=%@&ts=%@&sdkappid=%@&acctype=%@",
                     kLivePlayShareAddr,
                     TC_PROTECT_STR([self.liveInfo.userid stringByUrlEncoding]),
                     1,
                     TC_PROTECT_STR([self.liveInfo.fileid stringByUrlEncoding]),
                     [NSString stringWithFormat:@"%d", self.liveInfo.timestamp],
                     [[TCUserInfoModel sharedInstance] getUserProfile].appid,
                     [[TCUserInfoModel sharedInstance] getUserProfile].accountType];
    NSString *text = [NSString stringWithFormat:NSLocalizedString(@"TCBasePlayView.LiveBroadcast", nil), self.liveInfo.userinfo.nickname ? self.liveInfo.userinfo.nickname : self.liveInfo.userid];
    
    
    // 以下分享类型，开发者可根据需求调用
    // 1、纯文本分享
    messageObject.text = NSLocalizedString(@"TCBasePlayView.LiveBroadcastNotify", nil);
    
    
    
    // 2、 图片或图文分享
    // 图片分享参数可设置URL、NSData类型
    // 注意：由于iOS系统限制(iOS9+)，非HTTPS的URL图片可能会分享失败
    UMShareImageObject *shareObject = [UMShareImageObject shareObjectWithTitle:title descr:text thumImage:self.liveInfo.userinfo.frontcover];
    [shareObject setShareImage:self.liveInfo.userinfo.frontcoverImage];
    
    UMShareWebpageObject *share2Object = [UMShareWebpageObject shareObjectWithTitle:title descr:text thumImage:self.liveInfo.userinfo.frontcoverImage];
    share2Object.webpageUrl = url;
    
    //新浪微博有个bug，放在shareObject里面设置url，分享到网页版的微博不显示URL链接，这里在text后面也加上链接
    if (platformType == UMSocialPlatformType_Sina) {
        messageObject.text = [NSString stringWithFormat:@"%@  %@",messageObject.text,share2Object.webpageUrl];
    }else{
        messageObject.shareObject = share2Object;
    }
    [[UMSocialManager defaultManager] shareToPlatform:platformType messageObject:messageObject currentViewController:self completion:^(id data, NSError *error) {
        
        
        NSString *message = nil;
        if (!error) {
            message = [NSString stringWithFormat:NSLocalizedString(@"TCBasePlayView.ShareSucceeded", nil)];
        } else {
            if (error.code == UMSocialPlatformErrorType_Cancel) {
                message = [NSString stringWithFormat:NSLocalizedString(@"TCBasePlayView.ShareCanceled", nil)];
            } else if (error.code == UMSocialPlatformErrorType_NotInstall) {
                message = [NSString stringWithFormat:NSLocalizedString(@"TCBasePlayView.AppNotInstalled", nil)];
            } else {
                message = [NSString stringWithFormat:NSLocalizedString(@"TCBasePlayView.ShareFailed", nil),(int)error.code];
            }
            
        }
        UIAlertController *controller = [UIAlertController alertControllerWithTitle:nil
                                                                            message:message
                                                                     preferredStyle:UIAlertControllerStyleAlert];
        [controller addAction:[UIAlertAction actionWithTitle:NSLocalizedString(@"Common.OK", nil)
                                                       style:UIAlertActionStyleCancel
                                                     handler:nil]];
        [self presentViewController:controller animated:YES completion:nil];
    }];
}
*/
@end
