//
//  TVCReport.h
//  TXMUploader
//
//  Created by carolsuo on 2018/3/28.
//  Copyright © 2018年 lynxzhang. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <Foundation/Foundation.h>
#import "TVCClientInner.h"

@interface TVCReport : NSObject

+ (instancetype)shareInstance;

- (void) addReportInfo:(TVCReportInfo *)info;

@property (strong, nonatomic) NSMutableArray *reportCaches;
@property (nonatomic, weak) NSTimer* timer;

@end
