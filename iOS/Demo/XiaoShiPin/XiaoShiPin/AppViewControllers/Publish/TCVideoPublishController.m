#import "TCVideoPublishController.h"
//#import <UShareUI/UMSocialUIManager.h>
//#import <UMSocialCore/UMSocialCore.h>
#import "NSString+Common.h"
#import "TCUserInfoModel.h"
#import "UGCKit.h"
#import "SDKHeader.h"
#import "TXUGCPublish.h"
#import "TCLoginModel.h"
#import <AFNetworking/AFNetworking.h>
#import "TCLoginParam.h"
#import "TCUtil.h"
#import "ColorMacro.h"
#import "UIView+Additions.h"
@interface TCVideoPublishController()
@property UILabel         *labPublishState;
@property BOOL isNetWorkErr;
@property UIImageView      *imgPublishState;

@end

@implementation TCVideoPublishController
{
    //分享
    UIView          *_vShare;
    UIView          *_vShareInfo;
    UIView          *_vVideoPreview;
    UITextView       *_txtShareWords;
    UILabel         *_labDefaultWords;
    UILabel         *_labLeftWords;
    
    UILabel         *_labRecordVideo;
    
    UIView          *_vSharePlatform;
    NSMutableArray<UIButton*>   *_btnShareArry;
    
    //发布
    UIView          *_vPublishInfo;
    UIImageView      *_imgPublishState;
    UILabel         *_labPublishState;
    
    TXUGCPublish   *_videoPublish;
    TXLivePlayer     *_livePlayer;
    
    TXPublishParam   *_videoPublishParams;
    TXUGCRecordResult   *_recordResult;
    
    NSInteger       _selectBtnTag;
    BOOL            _isPublished;
    
    BOOL            _playEnable;
    
    id              _videoRecorder;
    BOOL            _isNetWorkErr;
}

- (instancetype)init:(id)videoRecorder
          recordType:(NSInteger)recordType
        recordResult:(TXUGCRecordResult *)recordResult
          tcLiveInfo:(TCLiveInfo *)liveInfo
{
    self = [super init];
    if (self) {
        _videoPublishParams = [[TXPublishParam alloc] init];
        _recordResult = recordResult;
        
        _videoRecorder = videoRecorder;
        
        _isPublished = NO;
        
        _playEnable  = YES;
        
        _isNetWorkErr = NO;
        
        _selectBtnTag = -1;
        
        _videoPublish = [[TXUGCPublish alloc] initWithUserID:[[TCUserInfoModel sharedInstance] getUserProfile].identifier];
        _videoPublish.delegate = self;
        _livePlayer  = [[TXLivePlayer alloc] init];
        _livePlayer.delegate = self;
        
        [[NSNotificationCenter defaultCenter] addObserver:self
                                                 selector:@selector(applicationWillEnterForeground:)
                                                     name:UIApplicationWillEnterForegroundNotification
                                                   object:nil];
        [[NSNotificationCenter defaultCenter] addObserver:self
                                                 selector:@selector(applicationDidEnterBackground:)
                                                     name:UIApplicationDidEnterBackgroundNotification
                                                   object:nil];
        
    }
    return self;
}

- (instancetype)initWithPath:(NSString *)videoPath videoMsg:(TXVideoInfo *) videoMsg
{
    TXUGCRecordResult *recordResult = [TXUGCRecordResult new];
    recordResult.coverImage = videoMsg.coverImage;
    recordResult.videoPath = videoPath;

    
    return [self init:nil recordType:0
         recordResult:recordResult
           tcLiveInfo:nil];
}

- (void)dealloc
{
    [_livePlayer removeVideoWidget];
    [[NSNotificationCenter defaultCenter] removeObserver:self];
    [[AFNetworkReachabilityManager sharedManager] stopMonitoring];
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    
    self.navigationController.navigationBar.tintColor = UIColorFromRGB(0x0ACCAC);
    self.navigationItem.title = NSLocalizedString(@"Common.Release", nil);
    [self.navigationController.navigationBar setTitleTextAttributes:@{NSFontAttributeName:[UIFont systemFontOfSize:18],NSForegroundColorAttributeName:[UIColor blackColor]}] ;
    self.view.backgroundColor = UIColorFromRGB(0xefeff4);
    
    UIBarButtonItem *btn = [[UIBarButtonItem alloc] initWithTitle:NSLocalizedString(@"Common.Release", nil) style:UIBarButtonItemStylePlain target:self action:@selector(videoPublish)];
    self.navigationItem.rightBarButtonItems = [NSMutableArray arrayWithObject:btn];

    self.view.userInteractionEnabled = YES;
    UITapGestureRecognizer *singleTap = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(closeKeyboard:)];
    singleTap.cancelsTouchesInView = NO;
    [self.view addGestureRecognizer:singleTap];
    
    //分享
    _vShare = [[UIView alloc] init];
    _vShare.backgroundColor = [UIColor clearColor];
    
    _vShareInfo = [[UIView alloc] init];
    _vShareInfo.backgroundColor = [UIColor whiteColor];
    
    _vVideoPreview = [[UIView alloc] init];
    
    _txtShareWords = [[UITextView alloc] init];
    _txtShareWords.delegate = self;
    _txtShareWords.layer.borderColor = _vShareInfo.backgroundColor.CGColor;
    _txtShareWords.font = [UIFont systemFontOfSize:16];
    _txtShareWords.textColor = UIColorFromRGB(0x0ACCAC);
 
    _labDefaultWords = [[UILabel alloc] init];
    _labDefaultWords.text = NSLocalizedString(@"TCVideoPublish.SaySomething", nil);
    _labDefaultWords.textColor = UIColorFromRGB(0xefeff4);
    _labDefaultWords.font = [UIFont systemFontOfSize:16];
    _labDefaultWords.backgroundColor =[UIColor clearColor];
    _labDefaultWords.textAlignment = NSTextAlignmentLeft;
    
    _labLeftWords = [[UILabel alloc] init];
    _labLeftWords.text = LocalizationNotNeeded(@"0/500");
    _labLeftWords.textColor = UIColorFromRGB(0xefeff4);
    _labLeftWords.font = [UIFont systemFontOfSize:12];
    _labLeftWords.backgroundColor =[UIColor clearColor];
    _labLeftWords.textAlignment = NSTextAlignmentRight;
    
    _vSharePlatform = [[UIView alloc] init];
    _vSharePlatform.backgroundColor = [UIColor whiteColor];
    
    NSArray * shareTitleArray       = @[
                                        NSLocalizedString(@"ShareTitleArray1", nil),
                                        NSLocalizedString(@"ShareTitleArray2", nil),
                                        NSLocalizedString(@"ShareTitleArray3", nil),
                                        NSLocalizedString(@"ShareTitleArray4", nil),
                                        NSLocalizedString(@"ShareTitleArray5", nil)];
    
    NSArray * shareIconPressArray        = @[
                                        @"video_record_wechat",
                                        @"video_record_friends",
                                        @"video_record_QQ",
                                        @"video_record_Qzone",
                                        @"video_record_sina"];
    NSArray * shareIconArray   = @[
                                        @"video_record_wechat_gray",
                                        @"video_record_friends_gray",
                                        @"video_record_QQ_gray",
                                        @"video_record_Qzone_gray",
                                        @"video_record_sina_gray"];
    
    _btnShareArry = [[NSMutableArray alloc] init];
    for(int i=0; i<shareTitleArray.count && i<shareIconArray.count && i<shareIconPressArray.count; ++i)
    {
        UIButton * _btn = [UIButton buttonWithType:UIButtonTypeCustom];
        [_btn setImage:[UIImage imageNamed:[shareIconArray objectAtIndex:i]] forState:UIControlStateNormal];
        [_btn setImage:[UIImage imageNamed:[shareIconPressArray objectAtIndex:i]] forState:UIControlStateSelected];
        [_btn setTitle:[shareTitleArray objectAtIndex:i] forState:UIControlStateNormal];
        [_btn setTitleColor:UIColorFromRGB(0x777777) forState:UIControlStateNormal];
        _btn.titleLabel.font = [UIFont systemFontOfSize:12];
        [_btn addTarget:self action:@selector(selectShare:) forControlEvents:UIControlEventTouchUpInside];
        _btn.tag = i;
        _btn.selected = NO;
//        [_btn setContentHorizontalAlignment:UIControlContentHorizontalAlignmentCenter];
        
        [_btnShareArry addObject:_btn];
        [_vSharePlatform addSubview:_btn];
    }
    [_vShare addSubview:_vSharePlatform];
    
    [self.view addSubview:_vShare];
    
    [_vShare addSubview:_vShareInfo];
    
    [_vShareInfo addSubview:_vVideoPreview];
    [_vShareInfo addSubview:_txtShareWords];
    [_vShareInfo addSubview:_labDefaultWords];
    [_vShareInfo addSubview:_labLeftWords];
    
    [_vShare setSize:CGSizeMake(self.view.width, self.view.height - [[UIApplication sharedApplication] statusBarFrame].size.height - self.navigationController.navigationBar.height)];
    [_vShare setY:[[UIApplication sharedApplication] statusBarFrame].size.height+self.navigationController.navigationBar.height];
    [_vShare setX:0];
    
    [_vShareInfo setSize:CGSizeMake(self.view.width, 180)];
    [self setBorderWithView:_vShareInfo top:YES left:NO bottom:YES right:NO borderColor:UIColorFromRGB(0xd8d8d8) borderWidth:0.5];
    [_vShareInfo setY:42];
    [_vShareInfo setX:0];
    
    [_vSharePlatform setSize:CGSizeMake(self.view.width, 100)];
    [self setBorderWithView:_vSharePlatform top:YES left:NO bottom:YES right:NO borderColor:UIColorFromRGB(0xd8d8d8) borderWidth:0.5];
    [_vSharePlatform setY:264];
    [_vSharePlatform setX:0];
    
    [_vVideoPreview setSize:CGSizeMake(100, 150)];
    [_vVideoPreview setX:15];
    [_vVideoPreview setY:15];
    
    [_txtShareWords setSize:CGSizeMake(self.view.width - _vVideoPreview.width - 45, _vVideoPreview.height)];
    [_txtShareWords setX:_vVideoPreview.right + 15];
    [_txtShareWords setY:15];
    
    [_labDefaultWords setSize:CGSizeMake(90, 16)];
    [_labDefaultWords setX:_vVideoPreview.right + 25];
    [_labDefaultWords setY:24];
    
    [_labLeftWords setSize:CGSizeMake(50, 12)];
    [_labLeftWords setX:_labLeftWords.superview.width - 15];
    [_labLeftWords setY:_labLeftWords.superview.height - 15];

    
    UILabel* publish_promise = [[UILabel alloc] init];
    publish_promise.text = NSLocalizedString(@"TCVideoPublish.ReleaseToApp", nil);
    publish_promise.textColor = UIColorFromRGB(0x777777);
    publish_promise.font = [UIFont systemFontOfSize:12];
    publish_promise.backgroundColor =[UIColor clearColor];
    publish_promise.textAlignment = NSTextAlignmentLeft;
    [_vShare addSubview:publish_promise];
    [publish_promise setSize:CGSizeMake(90, 12)];
    [publish_promise setY:20];
    [publish_promise setX:15];
    
    UILabel* share_promise = [[UILabel alloc] init];
    share_promise.text = NSLocalizedString(@"TCVideoPublish.AndShareTo", nil);
    share_promise.textColor = UIColorFromRGB(0x777777);
    share_promise.font = [UIFont systemFontOfSize:12];
    share_promise.backgroundColor =[UIColor clearColor];
    share_promise.textAlignment = NSTextAlignmentLeft;
    [_vShare addSubview:share_promise];
    [share_promise setSize:CGSizeMake(90, 12)];
    [share_promise setY:242];
    [share_promise setX:15];
    
    int gap = 15;
    int shareBtnWidth = 45;
    if (_btnShareArry.count > 1) gap = (self.view.width - 30 - _btnShareArry.count*shareBtnWidth)/(_btnShareArry.count-1);
    for(int i=0; i<_btnShareArry.count; ++i)
    {
        UIButton *btn = [_btnShareArry objectAtIndex:i];
        [btn setSize:CGSizeMake(shareBtnWidth, 70)];
        if (0 == i) {
            [btn setX:15];
        } else {
            [btn setX:[_btnShareArry objectAtIndex:i-1].right + gap];
        }
        [btn setY:15];
        
        btn.titleLabel.backgroundColor = btn.backgroundColor;
        btn.imageView.backgroundColor = btn.backgroundColor;
        CGSize titleSize = btn.titleLabel.bounds.size;
        CGSize imageSize = btn.imageView.bounds.size;
        CGFloat interval = 8.0;
        //(CGFloat top, CGFloat left, CGFloat bottom, CGFloat right)
        [btn setImageEdgeInsets:UIEdgeInsetsMake(0,0, titleSize.height + interval, -(titleSize.width))];
        [btn setTitleEdgeInsets:UIEdgeInsetsMake(imageSize.height + interval, -(imageSize.width), 0, 0)];
    }
    
    //发布
    _vPublishInfo = [[UIView alloc] init];
    _vPublishInfo.backgroundColor = [UIColor clearColor];
    _vPublishInfo.hidden = YES;
    
    _imgPublishState = [[UIImageView alloc] init];
    _imgPublishState.image = [UIImage imageNamed:@"video_record_share_loading_0"];
    
    _labPublishState = [[UILabel alloc] init];
    _labPublishState.text = NSLocalizedString(@"TCVideoPublish.VideoUploading", nil);
    _labPublishState.textColor = UIColorFromRGB(0x0ACCAC);
    _labPublishState.font = [UIFont systemFontOfSize:24];
    _labPublishState.backgroundColor =[UIColor clearColor];
    _labPublishState.textAlignment = NSTextAlignmentCenter;
    
    _labRecordVideo = [[UILabel alloc] init];
    _labRecordVideo.text = @"";
    _labRecordVideo.textColor = UIColorFromRGB(0x0ACCAC);
    _labRecordVideo.font = [UIFont systemFontOfSize:12];
    _labRecordVideo.backgroundColor =[UIColor clearColor];
    _labRecordVideo.numberOfLines = 0;
    _labRecordVideo.lineBreakMode = NSLineBreakByWordWrapping;
    _labRecordVideo.textAlignment = NSTextAlignmentCenter;
    
    [self.view addSubview:_vPublishInfo];
    [_vPublishInfo addSubview:_imgPublishState];
    [_vPublishInfo addSubview:_labPublishState];
    [_vPublishInfo addSubview:_labRecordVideo];
    
    
    [_vPublishInfo setSize:CGSizeMake(self.view.width, self.view.height - [[UIApplication sharedApplication] statusBarFrame].size.height - self.navigationController.navigationBar.height)];
    [_vPublishInfo setY:[[UIApplication sharedApplication] statusBarFrame].size.height+self.navigationController.navigationBar.height];
    [_vPublishInfo setX:0];
    
    [_imgPublishState setSize:CGSizeMake(50, 50)];
    [_imgPublishState setY:100];
    _imgPublishState.center = CGPointMake(self.view.center.x, _imgPublishState.center.y);
    
    [_labPublishState setSize:CGSizeMake(self.view.width, 24)];
    [_labPublishState setY:175];
    _labPublishState.center = CGPointMake(self.view.center.x, _labPublishState.center.y);
    
    _labRecordVideo.hidden = YES;
    
    [_livePlayer setupVideoWidget:CGRectZero containView:_vVideoPreview insertIndex:0];
}

- (void)setBorderWithView:(UIView *)view top:(BOOL)top left:(BOOL)left bottom:(BOOL)bottom right:(BOOL)right borderColor:(UIColor *)color borderWidth:(CGFloat)width
{
    if (top) {
        CALayer *layer = [CALayer layer];
        layer.frame = CGRectMake(0, 0, view.frame.size.width, width);
        layer.backgroundColor = color.CGColor;
        [view.layer addSublayer:layer];
    }
    if (left) {
        CALayer *layer = [CALayer layer];
        layer.frame = CGRectMake(0, 0, width, view.frame.size.height);
        layer.backgroundColor = color.CGColor;
        [view.layer addSublayer:layer];
    }
    if (bottom) {
        CALayer *layer = [CALayer layer];
        layer.frame = CGRectMake(0, view.frame.size.height - width, view.frame.size.width, width);
        layer.backgroundColor = color.CGColor;
        [view.layer addSublayer:layer];
    }
    if (right) {
        CALayer *layer = [CALayer layer];
        layer.frame = CGRectMake(view.frame.size.width - width, 0, width, view.frame.size.height);
        layer.backgroundColor = color.CGColor;
        [view.layer addSublayer:layer];
    }
}

-(void)viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
    [self.navigationController setNavigationBarHidden:NO];
    
    _playEnable = YES;
    if (_isPublished == NO) {
        [_livePlayer startPlay:_recordResult.videoPath type:PLAY_TYPE_LOCAL_VIDEO];
    }
}

- (void)viewDidDisappear:(BOOL)animated
{
    [super viewDidDisappear:animated];
    [self.navigationController setNavigationBarHidden:NO];
    
    _playEnable = NO;
    [_livePlayer stopPlay];
}

- (void)closeKeyboard:(UITapGestureRecognizer *)gestureRecognizer
{
    [_txtShareWords resignFirstResponder];
}

- (void)videoPublish
{
    [[TCLoginModel sharedInstance] getVodSign:^(int errCode, NSString *msg, NSDictionary *resultDict){
        if (200 == errCode && resultDict[@"signature"]) {
            _videoPublishParams.signature = resultDict[@"signature"];
            _videoPublishParams.coverPath = [self getCoverPath:_recordResult.coverImage];
            _videoPublishParams.videoPath = _recordResult.videoPath;
            errCode = [_videoPublish publishVideo:_videoPublishParams];
        }else{
            [self toastTip:[NSString stringWithFormat:NSLocalizedString(@"TCVideoPublish.HintFetchingSignatureFailedFmt", nil), errCode]];
            return;
        }
    
        __weak typeof(self) wkSelf = self;
        [[AFNetworkReachabilityManager sharedManager] setReachabilityStatusChangeBlock:^(AFNetworkReachabilityStatus status) {
            switch (status) {
                case AFNetworkReachabilityStatusNotReachable:
                    wkSelf.labPublishState.text = NSLocalizedString(@"TCVideoPublish.HintUploadingFailedNetwork", nil);
                    wkSelf.imgPublishState.hidden = YES;
                    wkSelf.isNetWorkErr = YES;
                    break;
                default:
                    break;
            }
        }];
        [[AFNetworkReachabilityManager sharedManager] startMonitoring]; //开启网络监控
        
        if(errCode != 0){
            [self toastTip:[NSString stringWithFormat:NSLocalizedString(@"TCVideoPublish.HintUploadingFailedFmt", nil), errCode]];
            return;
        }
        
        self.navigationItem.rightBarButtonItems = nil;
        self.navigationItem.title = NSLocalizedString(@"TCVideoPublish.PublishingTitle", nil);
        
        _vPublishInfo.hidden = NO;
        _vShare.hidden = YES;
        
        _labPublishState.text = NSLocalizedString(@"TCVideoPublish.PublishingHint", nil);
        _imgPublishState.image = [UIImage imageNamed:@"video_record_share_loading_0"];
        
        [_txtShareWords resignFirstResponder];
        [_livePlayer stopPlay];
    }];
}

#pragma mark - UITextViewDelegate
- (void)textViewDidChange:(UITextView*)textView
{
    if([textView.text length] == 0){
        _labDefaultWords.hidden = NO;
    }else{
        _labDefaultWords.hidden = YES;
    }
    
    _labLeftWords.text = [NSString stringWithFormat:@"%02ld/500", 500 - (long)[textView.text length]];
}

- (BOOL)textView:(UITextView*)textView shouldChangeTextInRange:(NSRange)range replacementText:(NSString*)text
{
    if ([text isEqualToString:@"\n"]) {
        [textView resignFirstResponder];
    }
    
    if (range.location >= 500)
    {
        return NO;
    } else {
        return YES;
    }
}

#pragma mark - TXVideoPublishListener
-(void) onPublishProgress:(uint64_t)uploadBytes totalBytes: (uint64_t)totalBytes
{
    long progress = (long)(8 * uploadBytes / totalBytes);
    _imgPublishState.image = [UIImage imageNamed:[NSString stringWithFormat:@"video_record_share_loading_%ld", progress]];
}

-(void) onPublishComplete:(TXPublishResult*)result
{
    if (!result.retCode) {
        _labPublishState.text = NSLocalizedString(@"TCVideoPublish.PublishingSucceeded", nil);
    } else {
        if (_isNetWorkErr == NO) {
            _labPublishState.text = [NSString stringWithFormat:NSLocalizedString(@"TCVideoPublish.PublishingFailedFmt", nil), result.retCode];
        }
        return;
    }
    
    NSString *title = _txtShareWords.text;
    if (title.length<=0) title = NSLocalizedString(@"Common.App", nil);
    NSDictionary* dictParam = @{@"userid" :[TCLoginParam shareInstance].identifier,
                                @"file_id" : result.videoId,
                                @"title":title,
                                @"frontcover":result.coverURL == nil ? @"" : result.coverURL,
                                @"location":NSLocalizedString(@"Common.Unknown", nil),
                                @"play_url":result.videoURL};
    [[TCLoginModel sharedInstance] uploadUGC:dictParam completion:^(int errCode, NSString *msg, NSDictionary *resultDict)  {
        if (200 == errCode) {
            if (_selectBtnTag >= 0) {
//                int  shareIndex[] = {1,2,4,5,0};
//                [self shareDataWithPlatform:shareIndex[_selectBtnTag] withFileID:result.videoId];
            }
        } else {
            [self toastTip:[NSString stringWithFormat:@"UploadUGCVideo Failed[%d]", errCode]];
        }
        
        _isPublished = YES;
    }];
    
    _imgPublishState.image = [UIImage imageNamed:@"video_record_success"];
    UIBarButtonItem *btn = [[UIBarButtonItem alloc] initWithTitle:NSLocalizedString(@"Common.Done", nil) style:UIBarButtonItemStylePlain target:self action:@selector(publishFinished)];
    self.navigationItem.rightBarButtonItems = [NSMutableArray arrayWithObject:btn];
}


- (void)selectShare:(UIButton *)button
{
    for(int i=0; i<_btnShareArry.count; ++i)
    {
        UIButton *btn = [_btnShareArry objectAtIndex:i];
        if (button == btn) {
            continue;
        }
        btn.selected = NO;
    }
    
    if (button.selected == YES) {
        button.selected = NO;
        _selectBtnTag = -1;
    } else {
        button.selected = YES;
        _selectBtnTag = button.tag;
    }
    
}

/*
- (void)shareDataWithPlatform:(UMSocialPlatformType)platformType withFileID:(NSString *)fileId
{
    TCUserInfoData *profile = [[TCUserInfoModel sharedInstance] getUserProfile];
    
    // 创建UMSocialMessageObject实例进行分享
    // 分享数据对象
    UMSocialMessageObject *messageObject = [UMSocialMessageObject messageObject];
    
    NSString *title = _txtShareWords.text;
    NSString *text = [NSString stringWithFormat:NSLocalizedString(@"TCVideoPublish.WhoseAppFmt", nil), profile.nickName ? profile.nickName: profile.identifier];
    if ( [title length] == 0) title = text;
    
    NSString *url = [NSString stringWithFormat:@"%@?userid=%@&type=%@&fileid=%@&ts=%@&sdkappid=%@&acctype=%@",
                     kLivePlayShareAddr,
                     TC_PROTECT_STR([profile.identifier stringByUrlEncoding]),
                     [NSString stringWithFormat:@"%d", 2],
                     TC_PROTECT_STR([fileId stringByUrlEncoding]),
                     [NSString stringWithFormat:@"%d", 2],
                     [[TCUserInfoModel sharedInstance] getUserProfile].appid,
                     [[TCUserInfoModel sharedInstance] getUserProfile].accountType];
    
    
    // 以下分享类型，开发者可根据需求调用
    // 1、纯文本分享
    messageObject.text = text;
    
    // 2、 图片或图文分享
    // 图片分享参数可设置URL、NSData类型
    // 注意：由于iOS系统限制(iOS9+)，非HTTPS的URL图片可能会分享失败
    UMShareImageObject *shareObject = [UMShareImageObject shareObjectWithTitle:title descr:text thumImage:_recordResult.coverImage];
    [shareObject setShareImage:_recordResult.coverImage];
    
    UMShareWebpageObject *share2Object = [UMShareWebpageObject shareObjectWithTitle:title descr:text thumImage:_recordResult.coverImage];

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
        [controller addAction:[UIAlertAction actionWithTitle:NSLocalizedString(@"Common.OK", nil) style:UIAlertActionStyleDefault handler:nil]];
        [self presentViewController:controller animated:YES completion:nil];
    }];
}
*/

- (void)applicationWillEnterForeground:(NSNotification *)noti
{
    //temporary fix bug
    if ([self.navigationItem.title isEqualToString:NSLocalizedString(@"TCVideoPublish.PublishingTitle", nil)])
        return;
    
    if (_isPublished == NO) {

        [_livePlayer startPlay:_recordResult.videoPath type:PLAY_TYPE_LOCAL_VIDEO];
    }
}

- (void)publishFinished
{
    if ([_videoRecorder isMemberOfClass:[TXLivePlayer class]]) {
        [self.navigationController  popViewControllerAnimated:YES];
    } else {
        [self dismissViewControllerAnimated:YES completion:^{
            
        }];
    }
}

- (void)applicationDidEnterBackground:(NSNotification *)noti
{
    [_livePlayer stopPlay];
}


#pragma mark TXLivePlayListener
-(void) onPlayEvent:(int)EvtID withParam:(NSDictionary*)param
{
    dispatch_async(dispatch_get_main_queue(), ^{
        if (EvtID == PLAY_EVT_PLAY_END && _playEnable) {
            [_livePlayer stopPlay];
            [_livePlayer startPlay:_recordResult.videoPath type:PLAY_TYPE_LOCAL_VIDEO];
            return;
        }
    });

}

-(void) onNetStatus:(NSDictionary*) param
{
    return;
}


#pragma mark Utils

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

-(NSString *)getCoverPath:(UIImage *)coverImage
{
    UIImage *image = coverImage;
    if (image == nil) {
        return nil;
    }
    
    NSString *coverPath = [NSTemporaryDirectory() stringByAppendingPathComponent:@"TXUGC"];
    coverPath = [coverPath stringByAppendingPathComponent:[self getFileNameByTimeNow:@"TXUGC" fileType:@"jpg"]];
    if (coverPath) {
        // 保证目录存在
        [[NSFileManager defaultManager] createDirectoryAtPath:[coverPath stringByDeletingLastPathComponent]
                                  withIntermediateDirectories:YES
                                                   attributes:nil
                                                        error:nil];
        
        [UIImageJPEGRepresentation(image, 1.0) writeToFile:coverPath atomically:YES];
    }
    return coverPath;
}

-(NSString *)getFileNameByTimeNow:(NSString *)type fileType:(NSString *)fileType {
    NSTimeInterval now = [[NSDate date] timeIntervalSince1970];
    NSDateFormatter * formatter = [[NSDateFormatter alloc] init];
    [formatter setDateFormat:@"yyyyMMdd_HHmmss"];
    NSDate * NowDate = [NSDate dateWithTimeIntervalSince1970:now];
    ;
    NSString * timeStr = [formatter stringFromDate:NowDate];
    NSString *fileName = ((fileType == nil) ||
                          (fileType.length == 0)
                          ) ? [NSString stringWithFormat:@"%@_%@",type,timeStr] : [NSString stringWithFormat:@"%@_%@.%@",type,timeStr,fileType];
    return fileName;
}
@end
