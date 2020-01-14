//
//  QCloudSupervisoryRecord.h
//  Pods
//
//  Created by Dong Zhao on 2017/4/7.
//
//

#import <Foundation/Foundation.h>
#import "QCloudNetEnv.h"

typedef NS_ENUM(NSInteger, QCloudSupervisoryRecordType) {
    QCloudSupervisoryRecordTypeNetwork
};

@interface QCloudSupervisoryRecord : NSObject
@property (nonatomic, strong) NSDate* logDate;
@property (nonatomic, assign) QCloudSupervisoryRecordType type;
@end

@interface QCloudSupervisoryNetworkRecord : QCloudSupervisoryRecord
@property (nonatomic, assign) NSTimeInterval taskTookTime;
@property (nonatomic, assign) NSTimeInterval calculateMD5STookTime;
@property (nonatomic, assign) NSTimeInterval signRequestTookTime;
@property (nonatomic, assign) NSTimeInterval dnsLookupTookTime;
@property (nonatomic, assign) NSTimeInterval connectTookTime;
@property (nonatomic, assign) NSTimeInterval secureConnectTookTime;
@property (nonatomic, assign) NSTimeInterval writeRequestBodyTookTime;
@property (nonatomic, assign) NSTimeInterval readResponseHeaderTookTime;
@property (nonatomic, assign) NSTimeInterval readResponseBodyTookTime;


@property (nonatomic, strong) NSString* service;
@property (nonatomic, strong) NSString* method;
@property (nonatomic, assign) int errorCode;
@property (nonatomic, strong) NSString* errorMessage;
@property (nonatomic, assign) QCloudNetworkStatus networkStatus;
@property (nonatomic, strong) NSString* userAgent;
@property (nonatomic,assign) NSTimeInterval caculateMD5;
@property (nonatomic,assign) NSTimeInterval sign;
@end
