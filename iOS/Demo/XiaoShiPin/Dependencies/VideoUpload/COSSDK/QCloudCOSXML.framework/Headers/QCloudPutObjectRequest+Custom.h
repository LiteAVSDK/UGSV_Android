//
//  QCloudPutObjectRequest+Custom.h
//  Pods-QCloudCOSXMLDemo
//
//  Created by karisli(李雪) on 2018/8/14.
//

#import <Foundation/Foundation.h>
#import "QCloudPutObjectRequest.h"
@interface QCloudPutObjectRequest (Custom)
-(void)setCOSServerSideEncyptionWithKMSCustomKey:(NSString *)customerKey jsonStr:(NSString *)jsonStr;
@end
