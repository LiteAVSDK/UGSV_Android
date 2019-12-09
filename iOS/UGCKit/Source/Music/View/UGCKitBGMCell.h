// Copyright (c) 2019 Tencent. All rights reserved.

#import <UIKit/UIKit.h>
#import "UGCKitCircleProgressView.h"

@class UGCKitBGMCell;

@protocol UGCKitBGMCellDelegate <NSObject>
- (void)onBGMDownLoad:(UGCKitBGMCell *)cell;
@end

@interface UGCKitBGMCell : UITableViewCell
@property (weak, nonatomic) id <UGCKitBGMCellDelegate> delegate;
@property (weak, nonatomic) IBOutlet UIButton *downLoadBtn;
@property (weak, nonatomic) IBOutlet UILabel *musicLabel;
@property (weak, nonatomic) IBOutlet UILabel *authorLabel;
@property (strong, nonatomic) UIView *progressView;

@property (strong, nonatomic) NSString *downloadText;
@property (strong, nonatomic) NSString *downloadingText;
@property (strong, nonatomic) NSString *applyText;
@property (strong, nonatomic) UIImage *downloadButtonBackground;
@property (strong, nonatomic) UIImage *progressButtonBackground;

- (void) setDownloadProgress:(CGFloat)progress;
@end
