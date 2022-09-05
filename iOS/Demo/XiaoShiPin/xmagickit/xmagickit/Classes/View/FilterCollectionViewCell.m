//
//  FilterCollectionViewCell.m
//  PituMotionDemo
//
//  Created by 吴梦添 on 2020/4/6.
//  Copyright © 2020 吴梦添. All rights reserved.
//

#import "FilterCollectionViewCell.h"
#import "FilterCellModel.h"
#import "Masonry.h"
#import <SDWebImage/UIImageView+WebCache.h>

@interface FilterCollectionViewCell()

//滤镜图片
@property (strong, nonatomic) UIImageView *image; // 滤镜图片
//滤镜名称
@property (strong, nonatomic) UILabel *label; // 滤镜名称

@end

@implementation FilterCollectionViewCell

- (instancetype)initWithFrame:(CGRect)frame {
    if (self = [super initWithFrame:frame]) {
        [self setupUI];
    }
    return self;
}

- (void) setFilterModel:(FilterCellModel *)model{
    _filterModel = model;
    NSData *data;
    if (_filterModel.iconUrl == nil) {
        data = [NSData dataWithContentsOfFile:_filterModel.icon];
        UIImage *icon = [UIImage imageWithData:data];
        [self.image setImage:icon];
        [self.label setText:_filterModel.title];
    }else{
        [self.image sd_setImageWithURL:_filterModel.iconUrl placeholderImage:nil];
        [self.label setText:_filterModel.title];
    }
}

- (void) setSelected:(BOOL)isSelected{
    [super setSelected:isSelected];
    if(isSelected){
        _image.layer.borderWidth = 2;
        _image.layer.borderColor = [[UIColor systemPinkColor] CGColor];
        [_label setTextColor:[UIColor systemPinkColor]];
        _image.layer.cornerRadius = 8;
    }else{
        _image.layer.borderWidth = 0;
        [_label setTextColor:[UIColor whiteColor]];
    }
}

- (void)setupUI{

    self.image = [[UIImageView alloc] init];
    [self.contentView addSubview:self.image];
    [self.image mas_makeConstraints:^(MASConstraintMaker *make) {
        make.width.height.mas_equalTo(50);
        make.right.mas_equalTo(self.contentView.mas_right);
        make.top.mas_equalTo(self.contentView.mas_top).mas_offset(0);
    }];

    self.label = [[UILabel alloc] init];
    self.label.textAlignment = NSTextAlignmentCenter;
    [self.label setFont:[UIFont systemFontOfSize:12]];
    [self.contentView addSubview:self.label];
    [_label setTextColor:[UIColor whiteColor]];
    [self.label mas_makeConstraints:^(MASConstraintMaker *make) {
        make.width.mas_equalTo(50);
        make.height.mas_equalTo(20);
        make.right.mas_equalTo(self.contentView.mas_right);
        make.bottom.mas_equalTo(self.contentView.mas_bottom).mas_offset(-12.33);
    }];

}
@end
