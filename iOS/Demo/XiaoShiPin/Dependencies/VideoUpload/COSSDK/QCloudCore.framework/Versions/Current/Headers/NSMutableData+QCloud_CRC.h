//
//  NSMutableData+Qcloud_CRC.h
//  AOPKit
//
//  Created by karisli(李雪) on 2020/7/1.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface NSMutableData (QCloud_CRC)
- (uint64_t)qcloud_crc64;
- (uint64_t)qcloud_crc64ForCombineCRC1:(uint64_t)crc1 CRC2:(uint64_t)crc2 length:(size_t)len2;
@end

NS_ASSUME_NONNULL_END
