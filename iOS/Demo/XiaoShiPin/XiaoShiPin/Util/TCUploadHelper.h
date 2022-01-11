//
//  TCUploadHelper.h
//  TCLVBIMDemo
//
//  Created by felixlin on 16/8/2.
//  Copyright © 2016年 tencent. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface TCUploadHelper : NSObject

+ (instancetype)shareInstance;

- (void)upload:(NSString*)userId image:(UIImage *)image completion:(void (^)(NSInteger errCode, NSString *imageSaveUrl))completion;

@end
