//
//  FilterCollectionViewCell.h
//  PituMotionDemo
//
//  Created by 吴梦添 on 2020/4/6.
//  Copyright © 2020 吴梦添. All rights reserved.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@class FilterCellModel;
/*
 * FilterCollectionViewCell 类
 *
 */
@interface FilterCollectionViewCell : UICollectionViewCell

@property (strong, nonatomic) FilterCellModel* filterModel; //filterModel

@end

NS_ASSUME_NONNULL_END
