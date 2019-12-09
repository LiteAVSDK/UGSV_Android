// Copyright (c) 2019 Tencent. All rights reserved.

#import "UGCKit_UIViewAdditions.h"
#import <QuartzCore/QuartzCore.h>

@implementation UIView (UGCKitAdditions)

- (CGFloat)ugckit_x {
	return self.frame.origin.x;
}

- (void)setUgckit_x:(CGFloat)x {
	CGRect rect = self.frame;
	rect.origin.x = x;
	[self setFrame:rect];
}

- (CGFloat)ugckit_y {
	return self.frame.origin.y;
}

- (void)setUgckit_y:(CGFloat)y {
	CGRect rect = self.frame;
	rect.origin.y = y;
	[self setFrame:rect];
}


- (CGFloat)ugckit_left {
	return self.frame.origin.x;
}

- (void)setUgckit_left:(CGFloat)x {
	CGRect frame = self.frame;
	frame.origin.x = x;
	self.frame = frame;
}

- (CGFloat)ugckit_top {
	return self.frame.origin.y;
}

- (void)setUgckit_top:(CGFloat)y {
	CGRect frame = self.frame;
	frame.origin.y = y;
	self.frame = frame;
}

- (CGFloat)ugckit_right {
	return self.frame.origin.x + self.frame.size.width;
}

- (void)setUgckit_right:(CGFloat)right {
	CGRect frame = self.frame;
	frame.origin.x = right - frame.size.width;
	self.frame = frame;
}

- (CGFloat)ugckit_bottom {
	return self.frame.origin.y + self.frame.size.height;
}

- (void)setUgckit_bottom:(CGFloat)bottom {
	CGRect frame = self.frame;
	frame.origin.y = bottom - frame.size.height;
	self.frame = frame;
}

- (CGFloat)ugckit_width {
	return self.frame.size.width;
}

- (void)setUgckit_width:(CGFloat)width {
	CGRect rect = self.frame;
	rect.size.width = width;
	[self setFrame:rect];
}

- (CGFloat)ugckit_height {
	return self.frame.size.height;
}

- (void)setUgckit_height:(CGFloat)height {
	CGRect rect = self.frame;
	rect.size.height = height;
	[self setFrame:rect];
}

@end
