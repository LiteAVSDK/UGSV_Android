// Copyright (c) 2019 Tencent. All rights reserved.

#import "UGCKitImageScrollerViewController.h"

@interface _UGCKitImageScrollerCellView : UICollectionViewCell
@property (readonly, nonatomic) UIImageView *imageView;
@property (readonly, nonatomic) UIButton *closeButton;
@end

static const CGFloat CellSize = 70;
static const CGFloat CloseButtonSize = 20;

typedef _UGCKitImageScrollerCellView CellClass;


@interface UGCKitImageScrollerViewController () <UICollectionViewDelegateFlowLayout>
{
    NSMutableArray<PHAsset *> *_assets;
}
@property (strong, nonatomic) PHCachingImageManager *imageManager;
@end

@implementation UGCKitImageScrollerViewController

static NSString * const reuseIdentifier = @"Cell";

- (instancetype)initWithImageManage:(PHCachingImageManager *)imageManager
{
    UICollectionViewFlowLayout *flowLayout = [[UICollectionViewFlowLayout alloc] init];
    flowLayout.scrollDirection = UICollectionViewScrollDirectionHorizontal;
    flowLayout.minimumInteritemSpacing = 2;
    flowLayout.sectionInset = UIEdgeInsetsMake(0, 8, 0, 8);
    if (self = [self initWithCollectionViewLayout:flowLayout]) {
        _imageManager = imageManager;
    }
    return self;
}

- (PHCachingImageManager *)imageManager {
    if (!_imageManager) {
        _imageManager = [[PHCachingImageManager alloc] init];
    }
    return _imageManager;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    _assets = [[NSMutableArray alloc] init];
    UILongPressGestureRecognizer *longGesture = [[UILongPressGestureRecognizer alloc] initWithTarget:self action:@selector(onLongPress:)];
    [self.collectionView addGestureRecognizer:longGesture];
    [self.collectionView registerClass:[CellClass class] forCellWithReuseIdentifier:reuseIdentifier];
}

#pragma mark - Public

- (NSArray<PHAsset *> *)currentAssets {
    if (_assets) {
        return _assets;
    } else {
        return @[];
    }
}

- (void)addAsset:(PHAsset *)asset {
    [_assets addObject:asset];
    [self.collectionView insertItemsAtIndexPaths:@[[NSIndexPath indexPathForRow:_assets.count-1 inSection:0]]];
}

- (void)removeAsset:(PHAsset *)asset
{
    NSUInteger index = [_assets indexOfObject:asset];
    [_assets removeObjectAtIndex:index];
    [self.collectionView deleteItemsAtIndexPaths:@[[NSIndexPath indexPathForRow:index inSection:0]]];
}
#pragma mark <UICollectionViewDataSource>

- (NSInteger)numberOfSectionsInCollectionView:(UICollectionView *)collectionView {
    return 1;
}

- (NSInteger)collectionView:(UICollectionView *)collectionView numberOfItemsInSection:(NSInteger)section {
    return _assets.count;
}

- (UICollectionViewCell *)collectionView:(UICollectionView *)collectionView cellForItemAtIndexPath:(NSIndexPath *)indexPath {
    CellClass *cell = (CellClass*)[collectionView dequeueReusableCellWithReuseIdentifier:reuseIdentifier forIndexPath:indexPath];
    PHAsset *asset = _assets[indexPath.item];
    [cell.closeButton setImage:self.closeIcon forState:UIControlStateNormal];
    if (cell.closeButton.allTargets.count == 0) {
        [cell.closeButton addTarget:self action:@selector(onRemoveItem:) forControlEvents:UIControlEventTouchUpInside];
    }
    cell.tag = indexPath.item;
    PHImageRequestOptions *option = [[PHImageRequestOptions alloc] init];
    option.deliveryMode = PHImageRequestOptionsDeliveryModeFastFormat;
    option.networkAccessAllowed = YES;
    [self.imageManager requestImageForAsset:asset
                                 targetSize:cell.imageView.bounds.size
                                contentMode:PHImageContentModeAspectFill
                                    options:option
                              resultHandler:^(UIImage *result, NSDictionary *info) {
        dispatch_async(dispatch_get_main_queue(), ^{
            if (cell.tag == indexPath.item) {
                cell.imageView.image = result;
            }
        });
    }];
    return cell;
}

#pragma mark <UICollectionViewDelegate>
- (CGSize)collectionView:(UICollectionView *)collectionView
                  layout:(UICollectionViewLayout *)collectionViewLayout
  sizeForItemAtIndexPath:(NSIndexPath *)indexPath
{
    return CGSizeMake(CellSize+CloseButtonSize/2, CellSize+CloseButtonSize/2);
}

- (BOOL)collectionView:(UICollectionView *)collectionView canMoveItemAtIndexPath:(NSIndexPath *)indexPath
{
    return YES;
}

- (void)collectionView:(UICollectionView *)collectionView moveItemAtIndexPath:(NSIndexPath *)sourceIndexPath toIndexPath:(NSIndexPath *)destinationIndexPath
{
    PHAsset *asset = _assets[sourceIndexPath.item];
    [_assets removeObjectAtIndex:sourceIndexPath.item];
    [_assets insertObject:asset atIndex:destinationIndexPath.item];
}

#pragma mark - Actions
- (void)onRemoveItem:(UIButton *)button
{
    CellClass *cell = (CellClass *)button.superview.superview;
    NSIndexPath *indexPath = [self.collectionView indexPathForCell:cell];

    if (indexPath) {
        NSUInteger index = indexPath.item;
        PHAsset *asset = _assets[index];
        [self removeAsset:asset];
        if (self.onRemoveHandler) {
            self.onRemoveHandler(asset);
        }
    }
}

- (void)onLongPress:(UILongPressGestureRecognizer *)sender {
    CGPoint location = [sender locationInView:self.collectionView];
    switch (sender.state) {
        case UIGestureRecognizerStateBegan: {
            NSIndexPath *indexPath = [self.collectionView indexPathForItemAtPoint:location];
            [self.collectionView beginInteractiveMovementForItemAtIndexPath:indexPath];
        }   break;
        case UIGestureRecognizerStateChanged:
            [self.collectionView updateInteractiveMovementTargetPosition:location];
            break;
        case UIGestureRecognizerStateEnded:
            [self.collectionView endInteractiveMovement];
            break;
        default:
            [self.collectionView cancelInteractiveMovement];
            break;
    }
}

@end


@implementation _UGCKitImageScrollerCellView
- (instancetype)initWithCoder:(NSCoder *)coder
{
    self = [super initWithCoder:coder];
    if (self) {
        [self commonInit];
    }
    return self;
}

- (instancetype)initWithFrame:(CGRect)frame
{
    self = [super initWithFrame:frame];
    if (self) {
        [self commonInit];
    }
    return self;
}

- (void)commonInit {
    _imageView = [[UIImageView alloc] initWithFrame:self.contentView.bounds];
    _imageView.contentMode = UIViewContentModeScaleAspectFill;
    _imageView.clipsToBounds = YES;
    _closeButton = [UIButton buttonWithType:UIButtonTypeCustom];
    [self.contentView addSubview:_imageView];
    [self.contentView addSubview:_closeButton];
}

- (void)layoutSubviews {
    [super layoutSubviews];
    _imageView.frame = UIEdgeInsetsInsetRect(self.bounds, UIEdgeInsetsMake(CloseButtonSize/2, 0, 0, CloseButtonSize/2));
    _closeButton.frame = CGRectMake(0, 0, CloseButtonSize, CloseButtonSize);
    _closeButton.center = CGPointMake(CGRectGetMaxX(_imageView.frame), CGRectGetMinY(_imageView.frame));
}
@end
