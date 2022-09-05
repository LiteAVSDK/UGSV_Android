// Copyright (C) 2018 Beijing Bytedance Network Technology Co., Ltd.
#import <GLKit/GLKit.h>

/*
 * BeautyGLView
 *
 */
@interface BeautyGLView : GLKView

- (void)resetWidthAndHeight;
- (void)renderWithTexture:(unsigned int)name
                     size:(CGSize)size
                  flipped:(BOOL)flipped
      applyingOrientation:(int)orientation
                  fitType:(int)fitType;
- (void)releaseContext;
@end
