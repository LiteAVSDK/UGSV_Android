//
//  TCLivePusherInfo.h
//  TCLVBIMDemo
//
//  Created by annidyfeng on 16/8/3.
//  Copyright © 2016年 tencent. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "TCLiveListModel.h"

@class TCLiveInfo;

/**
 *  点播列表的Cell类，主要展示封面、标题、昵称、定位位置
 */
@interface TCVideoListCell : UICollectionViewCell
{
    TCLiveInfo *_model;
}

@property (nonatomic , retain) TCLiveInfo *model;
@end
