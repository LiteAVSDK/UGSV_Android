//
//  TCAvatarListCell.h
//  XiaoShiPinApp
//
//  Created by tao yue on 2022/3/8.
//  Copyright © 2022 Tencent. All rights reserved.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

/**
 TCAvatarListCell 类说明 : 该类显示用户头像的cell
 */
@interface TCAvatarListCell : UICollectionViewCell
{
    NSString *_avatarUrl;
    NSString *_avatarUrlSelected;
}
/*
 * TCAvatarListCell 类说明 : 该类显示用户头像的cell
 */
@property (nonatomic , retain) NSString *avatarUrl; //头像URL
@property (nonatomic , retain) NSString *avatarUrlSelected; //头像是否被选择

@end

NS_ASSUME_NONNULL_END
