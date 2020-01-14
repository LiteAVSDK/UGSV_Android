// Copyright (c) 2019 Tencent. All rights reserved.

#import "UGCKitVideoRecordProcessView.h"
#import "UGCKitColorMacro.h"

#define VIEW_PAUSE_WIDTH 2

@implementation UGCKitVideoRecordProcessView
{
    UGCKitTheme *_theme;
    UIView *    _processView;
    UIView *    _deleteView;
    UIView *    _minimumView;
    CGSize      _viewSize;
    NSMutableArray * _pauseViewList;
    NSTimeInterval _minDuration;
    NSTimeInterval _maxDuration;
}

-(instancetype)initWithTheme:(UGCKitTheme *)theme
                       frame:(CGRect)frame
                 minDuration:(NSTimeInterval)minDuration
                 maxDuration:(NSTimeInterval)maxDuration
{
    self = [super initWithFrame:frame];
    if (self) {
        _theme = theme;
        _minDuration = minDuration;
        _maxDuration = maxDuration;
        [self setup];
    }
    return self;
}

- (instancetype)initWithCoder:(NSCoder *)aDecoder
{
    if (self = [super initWithCoder:aDecoder]) {
        [self setup];
    }
    return self;
}

- (void)setMinDuration:(NSTimeInterval)minDuration
           maxDuration:(NSTimeInterval)maxDuration
{
    _minDuration = minDuration;
    _maxDuration = maxDuration;
    _minimumView.frame = CGRectMake(CGRectGetWidth(self.frame) * _minDuration / _maxDuration, 0, 2, self.frame.size.height);
}

- (void)setMinimumTimeTipHidden:(BOOL)minimumTimeTipHidden {
    _minimumTimeTipHidden = minimumTimeTipHidden;
    _minimumView.hidden = minimumTimeTipHidden;
}

- (void)setup {
    _viewSize = self.frame.size;
    _processView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, 0, _viewSize.height)];
    _processView.backgroundColor = _theme.recordTimelineColor;// UIColorFromRGB(0xFF584C);
    [self addSubview:_processView];
    
    UIView * minimumView = [[UIView alloc] initWithFrame:CGRectMake(CGRectGetWidth(self.frame) * _minDuration / _maxDuration, 0, 2, self.frame.size.height)];
    minimumView.backgroundColor = [UIColor whiteColor];
    [self addSubview:minimumView];
    _minimumView = minimumView;
    _pauseViewList = [NSMutableArray array];
}

-(void)update:(CGFloat)progress
{
    _processView.frame = CGRectMake(0, 0, _viewSize.width * progress, _viewSize.height);
}

-(void)pause
{
    UIView *pauseView = [[UIView alloc] initWithFrame:CGRectMake(CGRectGetMaxX(_processView.frame) - VIEW_PAUSE_WIDTH, CGRectGetMinY(_processView.frame),
                                                                 VIEW_PAUSE_WIDTH, CGRectGetHeight(_processView.frame))];
    pauseView.backgroundColor = _theme.recordTimelineSeperatorColor;// UIColorFromRGB(0xA8002D);
    [_pauseViewList addObject:pauseView];
    [self addSubview:pauseView];
}

-(void)pauseAtTime:(CGFloat)time
{
    _processView.frame = CGRectMake(0, 0, _viewSize.width * time / _maxDuration, _viewSize.height);
    [self pause];
}

-(void)prepareDeletePart
{
    if (_pauseViewList.count == 0) {
        return;
    }
    UIView *lastPauseView = [_pauseViewList lastObject];
    UIView *beforeLastPauseView = nil;
    if (_pauseViewList.count > 1) {
        beforeLastPauseView = [_pauseViewList objectAtIndex:_pauseViewList.count - VIEW_PAUSE_WIDTH];
    }

    _deleteView = [[UIView alloc] initWithFrame:CGRectMake(CGRectGetMaxX(beforeLastPauseView.frame), CGRectGetMinY(_processView.frame),
                                                           CGRectGetMinX(lastPauseView.frame) - CGRectGetMaxX(beforeLastPauseView.frame),
                                                           CGRectGetHeight(_processView.frame))];
    _deleteView.backgroundColor = _theme.recordTimelineSelectionColor;// UIColorFromRGB(0xA8002D);
    [self addSubview:_deleteView];
}

-(void)cancelDelete
{
    if (_deleteView) {
        [_deleteView removeFromSuperview];
    }
}

-(void)comfirmDeletePart
{
    UIView *lastPauseView = [_pauseViewList lastObject];
    if (lastPauseView) {
        [lastPauseView removeFromSuperview];
    }
    [_pauseViewList removeObject:lastPauseView];
    [_deleteView removeFromSuperview];
}

-(void)deleteAllPart
{
    for(UIView *view in _pauseViewList)
    {
        [view removeFromSuperview];
    }
    [_pauseViewList removeAllObjects];
    [_deleteView removeFromSuperview];
}
@end
