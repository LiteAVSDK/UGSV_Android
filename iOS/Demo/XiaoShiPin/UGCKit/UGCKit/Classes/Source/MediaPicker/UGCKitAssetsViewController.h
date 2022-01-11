//Copyright (c) 2015 Katsuma Tanaka
//Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to
//deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
//sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
//The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
//THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
//FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
//LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
//IN THE SOFTWARE.
#import <UIKit/UIKit.h>
#import <AVFoundation/AVFoundation.h>
#import "UGCKitTheme.h"

@class UGCKitMediaPickerViewController;
@class PHAssetCollection;

@interface UGCKitAssetsViewController : UIViewController <UICollectionViewDelegate, UICollectionViewDataSource>//UICollectionViewController
@property (nonatomic, strong) UGCKitTheme *theme;
@property (nonatomic, weak) IBOutlet UILabel *timeLabel;
@property (nonatomic, weak) IBOutlet UILabel *descriptionLabel;
@property (nonatomic, weak) IBOutlet UIButton *doneButton;
@property (nonatomic, weak) IBOutlet UICollectionView *collectionView;
@property (nonatomic, weak) UGCKitMediaPickerViewController *imagePickerController;
@property (nonatomic, strong) PHAssetCollection *assetCollection;
@property (nonatomic, readonly) NSArray<AVAsset *> *exportedAssets;
@end
