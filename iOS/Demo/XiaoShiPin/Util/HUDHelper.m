//
//  HUDHelper.m
//  
//
//  Created by Alexi on 12-11-28.
//  Copyright (c) 2012年 . All rights reserved.
//

#import "HUDHelper.h"

#import "NSString+Common.h"
#import "AppDelegate.h"
#import "Mem.h"

@implementation HUDHelper
{
    void (^_tipCompletionBlock)(void);
    NSTimer *_tipCompletionBlockTimer;
}
static HUDHelper *_instance = nil;


+ (HUDHelper *)sharedInstance
{
    @synchronized(_instance)
    {
        if (_instance == nil)
        {
            _instance = [[HUDHelper alloc] init];
        }
        return _instance;
    }
}

+ (void)alert:(NSString *)msg
{
    [HUDHelper alert:msg cancel:NSLocalizedString(@"Common.OK", nil)];
}
+ (void)alert:(NSString *)msg action:(void(^)(void))action
{
    [HUDHelper alert:msg cancel:NSLocalizedString(@"Common.OK", nil) action:action];
}
+ (void)alert:(NSString *)msg cancel:(NSString *)cancel
{
    [HUDHelper alertTitle:NSLocalizedString(@"Common.Hint", nil) message:msg cancel:cancel];
}
+ (void)alert:(NSString *)msg cancel:(NSString *)cancel action:(void(^)(void))action
{
    [HUDHelper alertTitle:NSLocalizedString(@"Common.Hint", nil) message:msg cancel:cancel action:action];
}
+ (void)alertTitle:(NSString *)title message:(NSString *)msg cancel:(NSString *)cancel
{
    UIAlertController *controller = [UIAlertController alertControllerWithTitle:title message:msg preferredStyle:UIAlertControllerStyleAlert];
    [controller addAction:[UIAlertAction actionWithTitle:cancel style:UIAlertActionStyleCancel handler:nil]];
    [[UIApplication sharedApplication].keyWindow.rootViewController presentViewController:controller animated:YES completion:nil];
}

+ (void)alertTitle:(NSString *)title message:(NSString *)msg cancel:(NSString *)cancel action:(void(^)(void))action
{
    UIAlertController *controller = [UIAlertController alertControllerWithTitle:title message:msg preferredStyle:UIAlertControllerStyleAlert];
    [controller addAction:[UIAlertAction actionWithTitle:cancel style:UIAlertActionStyleCancel handler:^(UIAlertAction * _Nonnull _) {
        if (action) {
            action();
        }
    }]];
    [[UIApplication sharedApplication].keyWindow.rootViewController presentViewController:controller animated:YES completion:nil];
}

- (MBProgressHUD *)loading
{
    return [self loading:nil];
}

- (MBProgressHUD *)loading:(NSString *)msg
{
    return [self loading:msg inView:nil];
}

- (MBProgressHUD *)loading:(NSString *)msg inView:(UIView *)view
{
    UIView *inView = view ? view : self.keyWindow;
    MBProgressHUD *hud = [[MBProgressHUD alloc] initWithView:inView];
    
    dispatch_async(dispatch_get_main_queue(), ^{
        if (![NSString isEmpty:msg])
        {
            hud.mode = MBProgressHUDModeIndeterminate;
            hud.label.text = msg;
        }
        [inView addSubview:hud];
        [hud showAnimated:YES];
        // 超时自动消失
        // [hud hideAnimated:YES afterDelay:kRequestTimeOutTime];
    });
    return hud;
}

- (void)loading:(NSString *)msg delay:(CGFloat)seconds execute:(void (^)(void))exec completion:(void (^)(void))completion
{
    dispatch_async(dispatch_get_main_queue(), ^{
        UIView *inView = self.keyWindow;
        MBProgressHUD *hud = [[MBProgressHUD alloc] initWithView:inView];
        if (![NSString isEmpty:msg])
        {
            hud.mode = MBProgressHUDModeText;
            hud.label.text = msg;
        }
        
        [inView addSubview:hud];
        [hud showAnimated:YES];
        if (exec)
        {
            exec();
        }
        
        // 超时自动消失
        [hud hideAnimated:YES afterDelay:seconds];
        
        
        dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(seconds * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
            if (completion)
            {
                completion();
            }
        });
    });
}


- (void)stopLoading:(MBProgressHUD *)hud
{
    [self stopLoading:hud message:nil];
}

- (void)stopLoading:(MBProgressHUD *)hud message:(NSString *)msg
{
    [self stopLoading:hud message:msg delay:0 completion:nil];
}
- (void)stopLoading:(MBProgressHUD *)hud message:(NSString *)msg delay:(CGFloat)seconds completion:(void (^)(void))completion
{
    WEAKIFY(self);
    if (hud && hud.superview)
    {
        dispatch_async(dispatch_get_main_queue(), ^{
            STRONGIFY(self);
            if (!self) {
                if (completion) completion();
                return;
            }
            if (![NSString isEmpty:msg])
            {
                hud.label.text = msg;
                hud.mode = MBProgressHUDModeText;
            }
            
            [hud hideAnimated:YES afterDelay:seconds];
            self->_syncHUD = nil;
            
            dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(seconds * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
                if (completion)
                {
                    completion();
                }
            });
        });
    }
    else
    {
        if (completion)
        {
            completion();
        }
    }
    
}


- (MBProgressHUD *)tipMessage:(NSString *)msg
{
    return [self tipMessage:msg delay:2];
}

- (MBProgressHUD *)tipMessage:(NSString *)msg delay:(CGFloat)seconds
{
    return [self tipMessage:msg delay:seconds completion:nil];
    
}

- (MBProgressHUD *)tipMessage:(NSString *)msg delay:(CGFloat)seconds completion:(void (^)(void))completion
{
    NSAssert([NSThread isMainThread], @"Call HUD on main thread only");
    if ([NSString isEmpty:msg])
    {
        return nil;
    }
    MBProgressHUD *hud = [MBProgressHUD HUDForView:self.keyWindow];
    if (hud == nil) {
        hud = [[MBProgressHUD alloc] initWithView:self.keyWindow];
        hud.removeFromSuperViewOnHide = YES;
    }
    [self.keyWindow addSubview:hud];
    hud.mode = MBProgressHUDModeText;
    hud.label.text = msg;
    [hud showAnimated:YES];
    [hud hideAnimated:YES afterDelay:seconds];

    [_tipCompletionBlockTimer invalidate];
    if (_tipCompletionBlock) {
        _tipCompletionBlock();
    }
    _tipCompletionBlock = completion;
    if (completion) {
        _tipCompletionBlockTimer = [NSTimer scheduledTimerWithTimeInterval:seconds target:self selector:@selector(onTipTimer:) userInfo:nil repeats:NO];
    }
    return hud;
}

- (void)onTipTimer:(NSTimer *)timer {
    if (_tipCompletionBlock) {
        _tipCompletionBlock();
        _tipCompletionBlock = nil;
    }
}

#define kSyncHUDStartTag  100000

// 网络请求
- (void)syncLoading
{
    [self syncLoading:nil];
}
- (void)syncLoading:(NSString *)msg
{
    [self syncLoading:msg inView:nil];
}
- (void)syncLoading:(NSString *)msg inView:(UIView *)view
{
    if (_syncHUD)
    {
        _syncHUD.tag++;
        
        if (![NSString isEmpty:msg])
        {
            _syncHUD.label.text = msg;
            _syncHUD.mode = MBProgressHUDModeText;
        }
        else
        {
            _syncHUD.label.text = nil;
            _syncHUD.mode = MBProgressHUDModeIndeterminate;
        }
        
        return;
    }
    _syncHUD = [self loading:msg inView:view];
    _syncHUD.tag = kSyncHUDStartTag;
}

- (void)syncStopLoading
{
    [self syncStopLoadingMessage:nil delay:0 completion:nil];
}
- (void)syncStopLoadingMessage:(NSString *)msg
{
    [self syncStopLoadingMessage:msg delay:1 completion:nil];
}
- (void)syncStopLoadingMessage:(NSString *)msg delay:(CGFloat)seconds completion:(void (^)(void))completion
{
    _syncHUD.tag--;
    if (_syncHUD.tag > kSyncHUDStartTag)
    {
        if (![NSString isEmpty:msg])
        {
            _syncHUD.label.text = msg;
            _syncHUD.mode = MBProgressHUDModeText;
        }
        else
        {
            _syncHUD.label.text = nil;
            _syncHUD.mode = MBProgressHUDModeIndeterminate;
        }

    }
    else
    {
        [self stopLoading:_syncHUD message:msg delay:seconds completion:completion];
    }
}



@end
