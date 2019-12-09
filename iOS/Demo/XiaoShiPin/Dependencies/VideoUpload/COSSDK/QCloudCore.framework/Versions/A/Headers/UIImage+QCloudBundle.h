//
//  UIImage+QCloudBunle.h
//  Pods
//
//  Created by Dong Zhao on 2017/3/20.
//
//

#import <UIKit/UIKit.h>


#define QCloudImageNamed(name, cla)    [UIImage qcloudImageNamed:@""#name class:cla]
#define QCloudImageNamedInSelfBundle(name)    [UIImage qcloudImageNamed:@""#name class:self.class]

@interface UIImage (QCloudBunle)
+ (UIImage*) qcloudImageNamed:(NSString *)name class:(Class)cla;
@end
