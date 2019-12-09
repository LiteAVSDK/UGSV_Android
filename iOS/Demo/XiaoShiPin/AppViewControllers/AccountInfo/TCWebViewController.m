//
//  SDKIntroViewController.m
//  TXXiaoShiPinDemo
//
//  Created by shengcui on 2018/8/31.
//  Copyright © 2018年 tencent. All rights reserved.
//

#import "TCWebViewController.h"
#import <Availability.h>

@interface TCWebViewController () <WKUIDelegate>
{
    WKUserContentController *_contentController;
    NSURL *_url;
}
@end

@implementation TCWebViewController
- (instancetype)initWithURL:(NSString *)url {
    self = [super initWithNibName:nil bundle:nil];
    if (self) {
        _url = [NSURL URLWithString:url];
    }
    return self;
}
    
- (void)viewDidLoad {
    [super viewDidLoad];
    // AppStore审核不允许出现'购买'和Android字样，去掉相关信息
    WKUserContentController *ucc = [[WKUserContentController alloc] init];
    _contentController = ucc;
    [self installUserScripts:ucc];
    
    WKWebViewConfiguration *config = [[WKWebViewConfiguration alloc] init];
    config.userContentController = ucc;
    
    WKWebView *webView = [[WKWebView alloc] initWithFrame:self.view.bounds configuration:config];
    webView.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleHeight;
    webView.UIDelegate = self;
    [self.view addSubview:webView];
    webView.navigationDelegate = self;
    self.webView = webView;
    [webView loadRequest:[NSURLRequest requestWithURL:_url]];
}

- (void)installUserScripts:(WKUserContentController *)ucc {
    [ucc removeAllUserScripts];
    NSArray *jsCmdList = @[@"$(\"div.c-hero-section-btn>a.c-btn\").remove();"
                           , @"$('div.sdk-wrap').remove();"
                           , @"$('div.ugsv-buy').remove();"
                           , @"$('li[data-type=\"pricing\"]').remove();"
                           , @"$('video.ugsv-demo-video').removeAttr('autoplay');"
                           , @"$('li[data-type=\"getting-started\"]').remove();"
                           , @"$('a[href=\"https://cloud.tencent.com/document/product/584/9368\"]').remove();"
                           , @"$('a[href=\"tel:4009-100-100\"]').text(function(){return $(this).text().replace('购买', '');});"];
    NSString *combine = [jsCmdList componentsJoinedByString:@""];
    NSString *observer = [NSString stringWithFormat: @"$('div.product-body.J-mainContent').bind('DOMSubtreeModified',function(e) {%@});", combine];

    NSString *final = [combine stringByAppendingString:observer];
    WKUserScript *script = [[WKUserScript alloc]initWithSource:final injectionTime:WKUserScriptInjectionTimeAtDocumentEnd forMainFrameOnly:NO];
    [ucc addUserScript:script];
/*
    NSMutableArray *jsList = [NSMutableArray arrayWithCapacity:jsCmdList.count];
    for (NSString *js in jsCmdList) {
        [jsList addObject:[NSString stringWithFormat:@"$(document).ready(function() { %@; });", js]];
    }

    for (NSString *js in jsList) {
        WKUserScript *script = [[WKUserScript alloc]initWithSource:js injectionTime:WKUserScriptInjectionTimeAtDocumentEnd forMainFrameOnly:NO];
        [ucc addUserScript:script];
    }
 */
}

- (void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    self.navigationController.navigationBarHidden = NO;
}

- (void)viewWillDisappear:(BOOL)animated
{
    [super viewWillDisappear:animated];
    self.navigationController.navigationBarHidden = YES;
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (void)webView:(WKWebView *)webView didFinishNavigation:(null_unspecified WKNavigation *)navigation;
{
    self.title = webView.title;
}

// ignore warning that shows "'WKPreviewElementInfo' is only available on iOS 10.0 or newer"
#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wunguarded-availability"
- (BOOL)webView:(WKWebView *)webView shouldPreviewElement:(WKPreviewElementInfo *)elementInfo {
#pragma clang diagnostic pop
    return NO;
}

@end
