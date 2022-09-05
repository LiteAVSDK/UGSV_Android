//
//  BeautyViewModel.h
//  PituMotionDemo
//
//  Created by xingyunmao on 2021/2/22.
//  Copyright © 2021 Pitu. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "BeautyCellModel.h"
#import "XmagicKitTheme.h"
NS_ASSUME_NONNULL_BEGIN

/*
 * BeautyViewModel：美颜数据模型配置
 *
 */
@interface BeautyViewModel : NSObject

@property (strong, nonatomic) NSIndexPath *beautySelectedIndex;  //美颜选中index
@property (strong, nonatomic) NSIndexPath *beautyThinFaceSelectedIndex;  //瘦脸选中index
@property (strong, nonatomic) NSIndexPath *beautylipSelectedIndex;  //口红选中index
@property (strong, nonatomic) NSIndexPath *beautyCheekSelectedIndex;  //腮红选中index
@property (strong, nonatomic) NSIndexPath *beautyDimensionSelectedIndex;  //立体选中index
@property (strong, nonatomic) NSIndexPath *beautySegSelectedIndex;  //分割选中index

@property (strong, nonatomic) NSIndexPath *lutSelectedIndex;  //滤镜选中index
@property (strong, nonatomic) NSIndexPath *motionSelectedIndex;  //动效选中index
@property (strong, nonatomic) NSIndexPath *makeupSelectedIndex;  //美妆选中index

@property (strong, nonatomic) NSIndexPath *motion2DMenuSelectedIndex;  //2D选中index
@property (strong, nonatomic) NSIndexPath *motion3DMenuSelectedIndex;  //3D选中index
@property (strong, nonatomic) NSIndexPath *motionHandMenuSelectedIndex;  //手势选中index
@property (strong, nonatomic) NSIndexPath *motionGanMenuSelectedIndex;  //趣味选中index

@property (assign, nonatomic) BOOL basicFaceEnable;  //basicFaceEnable
@property (strong ,nonatomic) NSMutableArray<BeautyCellModel *> *beautyIDs;  //美颜数据
@property (strong ,nonatomic) NSMutableArray<BeautyCellModel *> *beautyThinFaceIDs;  //瘦脸数据
@property (strong ,nonatomic) NSMutableArray<BeautyCellModel *> *beautylipIDs;  //口红数据
@property (strong ,nonatomic) NSMutableArray<BeautyCellModel *> *beautyCheekIDs;  //腮红数据
@property (strong ,nonatomic) NSMutableArray<BeautyCellModel *> *beautyDimensionIDs;  //立体数据
@property (strong ,nonatomic) NSMutableArray<BeautyCellModel *> *lutIDs;  //滤镜数据
@property (strong ,nonatomic) NSMutableArray<BeautyCellModel *> *motionIDs;  //动效数据
@property (strong ,nonatomic) NSMutableArray<BeautyCellModel *> *makeupIDS;  //美妆数据
@property (strong ,nonatomic) NSMutableArray<BeautyCellModel *> *beautySegIDS;  //分割数据
@property (strong ,nonatomic) NSMutableArray<BeautyCellModel *> *motion2DMenuIDS;  //2D动效数据
@property (strong ,nonatomic) NSMutableArray<BeautyCellModel *> *motion3DMenuIDS;  //3D动效数据
@property (strong ,nonatomic) NSMutableArray<BeautyCellModel *> *motionHandMenuIDS;  //手势动效数据
@property (strong ,nonatomic) NSMutableArray<BeautyCellModel *> *motionGanMenuIDS;  //趣味动效数据
@property (assign, nonatomic) NSInteger sortType;  //当前sortType

@property (assign, nonatomic) XmagicKitTheme *theme;  //theme
- (void)setupData;
- (void)sortByType:(NSInteger)type;
@end

NS_ASSUME_NONNULL_END
