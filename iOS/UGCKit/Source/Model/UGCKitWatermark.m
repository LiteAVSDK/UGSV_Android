// Copyright (c) 2019 Tencent. All rights reserved.

#import "UGCKitWatermark.h"

@implementation UGCKitWatermark
- (instancetype)initWithImage:(UIImage *)image frame:(CGRect)frame duration:(NSTimeInterval)duration
{
    if (self = [super init]) {
        _image = image;
        _frame = frame;
        _duration = duration;
    }
    return self;
}

+ (instancetype)watermarkWithImage:(UIImage *)image frame:(CGRect)frame duration:(NSTimeInterval)duration;
{
    return [[UGCKitWatermark alloc] initWithImage:image frame:frame duration:duration];
}
@end
