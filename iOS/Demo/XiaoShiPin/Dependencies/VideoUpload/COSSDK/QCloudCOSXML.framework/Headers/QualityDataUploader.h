//
//  QualityDataUploader.h
//  QCloudCOSXML
//
//  Created by erichmzhang(张恒铭) on 2018/8/23.
//

#import <Foundation/Foundation.h>
#import <QCloudCore/MTA.h>

@interface QualityDataUploader : NSObject

+ (void)trackRequestSentWithType:(Class)cls;
+ (void)trackRequestFailWithType:(Class)cls Error:(NSError *)error;
@end


