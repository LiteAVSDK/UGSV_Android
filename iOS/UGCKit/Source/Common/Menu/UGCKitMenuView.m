// Copyright (c) 2019 Tencent. All rights reserved.

#import "UGCKitMenuView.h"
#import "UGCKitMenuItemCell.h"

#define BeautyViewMargin 8
#define BeautyViewSliderHeight 30
#define BeautyViewCollectionHeight 50

#define BeautyViewTitleWidth 40

static const CGFloat MenuTitleFontSize  = 15;
static const CGFloat SubMenuTitleFontSize = 12;


@interface UGCKitMenuView () <UICollectionViewDelegate, UICollectionViewDataSource>
{
    UICollectionView *_collectionView;
}
@property (nonatomic, strong) NSArray *menuArray;
/// array of options for each menu
@property (nonatomic, strong) NSArray<NSArray<id<UGCKitMenuItem>> *> *optionsContainer;
@property (nonatomic, strong) NSMutableDictionary *selectedIndexMap;
@property (nonatomic, strong) UICollectionView *menuCollectionView;
@property (nonatomic, strong) UICollectionView *optionsCollectionView;

//@property (nonatomic, strong) id currentItem;
@end

@implementation UGCKitMenuView

- (instancetype)initWithFrame:(CGRect)frame
                        menus:(NSArray<NSString *> *)menus
                  menuOptions:(NSArray<NSArray<id<UGCKitMenuItem>> *>*)options {
    if (self = [super initWithFrame:frame]) {
        // 选项
        _menuArray = [menus copy];
        _optionsContainer = [options copy];

        // 一级菜单
        UICollectionViewFlowLayout *layout = [[UICollectionViewFlowLayout alloc] init];
        layout.scrollDirection = UICollectionViewScrollDirectionHorizontal;
        //        layout.itemSize = CGSizeMake(100, 40);
        CGRect menuFrame = CGRectMake(0, CGRectGetHeight(self.bounds) - BeautyViewCollectionHeight,
                                      CGRectGetWidth(self.bounds), BeautyViewCollectionHeight);
        _menuCollectionView = [[UICollectionView alloc] initWithFrame:menuFrame collectionViewLayout:layout];
        _menuCollectionView.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleTopMargin;
        _menuCollectionView.backgroundColor = [UIColor colorWithRed:0 green:0 blue:0 alpha:0.6];
        _menuCollectionView.showsHorizontalScrollIndicator = NO;
        _menuCollectionView.delegate = self;
        _menuCollectionView.dataSource = self;
        [_menuCollectionView registerClass:[UGCKitMenuItemCell class] forCellWithReuseIdentifier:[UGCKitMenuItemCell reuseIdentifier]];
        [self addSubview:_menuCollectionView];
        if (menus.count > 0) {
            [_menuCollectionView selectItemAtIndexPath:[NSIndexPath indexPathForItem:0 inSection:0] animated:NO scrollPosition:UICollectionViewScrollPositionNone];
        }
        // 二级菜单
        layout = [[UICollectionViewFlowLayout alloc] init];
        layout.scrollDirection = UICollectionViewScrollDirectionHorizontal;
        const CGFloat SubMenuHeight = CGRectGetHeight(frame) - BeautyViewCollectionHeight;
        CGRect subMenuFrame = CGRectMake(0, CGRectGetMinY(_menuCollectionView.frame) - SubMenuHeight,
                                      CGRectGetWidth(self.bounds), SubMenuHeight);

        _optionsCollectionView = [[UICollectionView alloc] initWithFrame:subMenuFrame collectionViewLayout:layout];
        _optionsCollectionView.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleTopMargin;
        _optionsCollectionView.backgroundColor = [UIColor colorWithRed:0 green:0 blue:0 alpha:0.3];
        _optionsCollectionView.showsHorizontalScrollIndicator = NO;
        _optionsCollectionView.delegate = self;
        _optionsCollectionView.dataSource = self;
        [_optionsCollectionView registerClass:[UGCKitMenuItemCell class] forCellWithReuseIdentifier:[UGCKitMenuItemCell reuseIdentifier]];
        [self addSubview:_optionsCollectionView];

        // 选中状态
        _selectedIndexMap = [NSMutableDictionary dictionaryWithCapacity:_optionsContainer.count];

    }
    return self;
}

- (UIColor *)menuBackgroundColor {
    return _menuCollectionView.backgroundColor;
}

- (void)setMenuBackgroundColor:(UIColor *)menuBackgroundColor {
    _menuCollectionView.backgroundColor = menuBackgroundColor;
}

- (UIColor *)subMenuBackgroundColor {
    return _optionsCollectionView.backgroundColor;
}

- (void)setSubMenuBackgroundColor:(UIColor *)subMenuBackgroundColor {
    _optionsCollectionView.backgroundColor = subMenuBackgroundColor;
}

- (void)setSelectedOption:(NSInteger)optionIndex inMenu:(NSInteger)menuIndex {
    NSIndexPath *indexPath = [NSIndexPath indexPathForItem:optionIndex inSection:0];
    _selectedIndexMap[@(menuIndex)] =  indexPath;
    if (menuIndex == _menuIndex) {
        [self.optionsCollectionView selectItemAtIndexPath:indexPath
                                                 animated:NO
                                           scrollPosition:UICollectionViewScrollPositionCenteredHorizontally];
    }
}

- (void)setMenuSelectionBackgroundImage:(UIImage *)menuSelectionBackgroundImage
{
    _menuSelectionBackgroundImage = menuSelectionBackgroundImage;
    [_optionsCollectionView reloadData];
    [_menuCollectionView reloadData];
}

#pragma mark - Menu Switch
- (void)changeSubmenu:(NSInteger)menuIndex
{
    NSAssert(menuIndex < _optionsContainer.count, @"index out of range");
    [self.menuCollectionView cellForItemAtIndexPath:[NSIndexPath indexPathForItem:_menuIndex inSection:0]].selected = NO;
    _menuIndex = menuIndex;
    NSIndexPath *indexPath = _selectedIndexMap[@(menuIndex)];
    [self.optionsCollectionView reloadData];
    if (indexPath) {
        [self.optionsCollectionView selectItemAtIndexPath:indexPath
                                                 animated:NO
                                           scrollPosition:UICollectionViewScrollPositionCenteredHorizontally];
    }
    _optionIndex = indexPath ? indexPath.item : 0;
    [self.delegate menu:self didChangeToIndex:menuIndex option:_optionIndex];
}

- (void)setSelectedIndexPath:(NSIndexPath *)indexPath {
    [self setSelectedIndexPath:indexPath forMenu:_menuIndex];
}

- (void)setSelectedIndexPath:(NSIndexPath *)indexPath forMenu:(NSInteger)menuIndex {
    _selectedIndexMap[@(menuIndex)] = indexPath;
}

#pragma mark - UICollectionViewDataSource
- (NSInteger)numberOfSectionsInCollectionView:(UICollectionView *)collectionView {
    return 1;
}

- (NSInteger)collectionView:(UICollectionView *)collectionView numberOfItemsInSection:(NSInteger)section
{
    if (collectionView == self.menuCollectionView) {
        return self.menuArray.count;
    }
    return [_optionsContainer[_menuIndex] count];
}

- (NSIndexPath *)selectedIndexPath {
    return [self selectedIndexPathForMenu:_menuIndex];
}

- (NSIndexPath *)selectedIndexPathForMenu:(NSInteger)index {
    return _selectedIndexMap[@(index)] ?: [NSIndexPath indexPathForItem:0 inSection:0];
}

- (UICollectionViewCell *)collectionView:(UICollectionView *)collectionView cellForItemAtIndexPath:(NSIndexPath *)indexPath
{
    if(collectionView == _menuCollectionView){
        UGCKitMenuItemCell *cell = [collectionView dequeueReusableCellWithReuseIdentifier:[UGCKitMenuItemCell reuseIdentifier] forIndexPath:indexPath];
        cell.label.font = [UIFont systemFontOfSize: MenuTitleFontSize];
        cell.label.text = self.menuArray[indexPath.row];
        cell.selectedBackgroundImage = self.menuSelectionBackgroundImage;
        cell.selected = indexPath.row == _menuIndex;
        cell.label.textColor = self.menuTitleColor;
        return cell;
    } else {
        UGCKitMenuItemCell *cell = [collectionView dequeueReusableCellWithReuseIdentifier:[UGCKitMenuItemCell reuseIdentifier] forIndexPath:indexPath];
        cell.label.font = [UIFont systemFontOfSize: SubMenuTitleFontSize];
        id<UGCKitMenuItem> item = _optionsContainer[_menuIndex][indexPath.item];
        if ([item isKindOfClass:[NSString class]]) {
            cell.imageView.image = nil;
            cell.label.text = (NSString*)item;
        } else {
            cell.imageView.image = [item icon];
            cell.label.text = [item title];
        }
        cell.selected = [indexPath isEqual: [self selectedIndexPath]];
        cell.label.highlightedTextColor = self.subMenuSelectionColor;
        cell.label.textColor = self.menuTitleColor;
        return cell;
    }
}

- (void)collectionView:(UICollectionView *)collectionView didSelectItemAtIndexPath:(NSIndexPath *)indexPath
{
    if(collectionView == _menuCollectionView){
        if(indexPath.row != _menuIndex){
            [self changeSubmenu:indexPath.item];
        }
    } else {
        // select options
        NSIndexPath *prevSelectedIndexPath = [self selectedIndexPath];
        [collectionView cellForItemAtIndexPath:prevSelectedIndexPath].selected = NO;

        if([indexPath isEqual:prevSelectedIndexPath]){
            // 和上次选的一样
            return;
        }
        [self setSelectedIndexPath:indexPath];
        _optionIndex = indexPath.item;
        [self.delegate menu:self didChangeToIndex:_menuIndex option:indexPath.item];
    }
}

- (CGSize)collectionView:(UICollectionView *)collectionView layout:(UICollectionViewLayout *)collectionViewLayout sizeForItemAtIndexPath:(NSIndexPath *)indexPath
{
    NSString *text = nil;
    CGFloat minWidth = 0;
    CGFloat fontSize = 0;
    if(collectionView == _menuCollectionView){
        text = self.menuArray[indexPath.row];
        fontSize = MenuTitleFontSize;
        minWidth = self.minMenuWidth;
    } else {
        minWidth = self.minSubMenuWidth;
        fontSize = SubMenuTitleFontSize;
        id<UGCKitMenuItem> item = _optionsContainer[_menuIndex][indexPath.item];
        if ([item isKindOfClass:[NSString class]]) {
            text = (NSString*)item;
        } else {
            text = item.title;
        }
    }
    UIFont *font = [UIFont systemFontOfSize: fontSize];
    NSDictionary *attrs = @{NSFontAttributeName : font};
    CGSize size=[text sizeWithAttributes:attrs];
    CGFloat width = MAX(size.width, minWidth);
    return CGSizeMake(width, collectionView.frame.size.height);
}

@end
