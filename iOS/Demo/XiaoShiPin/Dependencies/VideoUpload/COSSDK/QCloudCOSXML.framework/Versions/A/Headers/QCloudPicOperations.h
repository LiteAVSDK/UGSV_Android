//
//  QCloudPutObjectWatermarkInfo.h
//  Pods-QCloudCOSXMLDemo
//
//  Created by garenwang on 2020/6/4.
//

#import <Foundation/Foundation.h>
@class QCloudPicOperationRule;

NS_ASSUME_NONNULL_BEGIN

typedef NS_ENUM(NSUInteger, QCloudPicOperationRuleEnum) {
    QCloudPicOperationRuleHalf = 1, /// 抗攻击性强，但提取水印需原图 使用场景：小图（640x640以下）使用
    QCloudPicOperationRuleFull,     /// 提取方便，提取水印仅需水印图，无需对比原图 使用场景：批量添加，批量校验
    QCloudPicOperationRuleText,     /// 可直接将文字信息添加至图片中 使用场景：终端信息添加
};

typedef NS_ENUM(NSUInteger, QCloudPicOperationRuleActionEnum) {
    QCloudPicOperationRuleActionPut = 3, /// 添加盲水印
    QCloudPicOperationRuleActionExtrac = 4,     /// 提取盲水印
};


/**
 图片添加盲水印接口 参数类
 包含水印规则，水印类型，水印参数
 */
@interface QCloudPicOperations : NSObject

/// 是否返回原图信息。0表示不返回原图信息，1表示返回原图信息，默认为0
@property (nonatomic, assign) BOOL is_pic_info;

/// 处理规则，一条规则对应一个处理结果（目前最多支持五条规则），不填则不进行图片处理
@property (nonatomic, copy) NSArray<QCloudPicOperationRule *> *rule;

- (NSString *)getPicOperationsJson;

@end

/**
 给图片添加水印规则
 */
@interface QCloudPicOperationRule : NSObject

/// 处理结果的文件路径名称，如以/开头，则存入指定文件夹中，否则，存入原图文件存储的同目录
@property (nonatomic, copy) NSString *fileid;

/// 处理参数，参见数据万象图片处理 API。 若按指定样式处理，则以style/开头，后加样式名，如样式名为“test”，
/// 则 rule 字段为style/test
@property (nonatomic, copy) NSString *rule;

/// 盲水印类型，有效值：1 半盲；2 全盲；3 文字
@property (nonatomic, assign) QCloudPicOperationRuleEnum type;

/// 水印操作：提取水印：4，添加水印：3
@property (nonatomic, assign) QCloudPicOperationRuleActionEnum actionType;
/// 盲水印类型，有效值：1 半盲；2 全盲；3 文字

/**
 盲水印图片地址，需要经过 URL 安全的 Base64 编码。 当 type 为1或2时必填，type 为3时无效。
    指定的水印图片必须同时满足如下 3 个条件：
 1. 盲水印图片与原图片必须位于同一个对象存储桶下；
 2. URL 需使用数据万象源站域名（不能使用 CDN 加速、COS 源站域名），例如
    examplebucket-1250000000.image.myqcloud.com属于 CDN 加速域名，不能在水印 URL 中使用；
 3. URL 必须以http://开始，不能省略http头，
 也不能填https头，例如examplebucket-1250000000.picsh.myqcloud.com/shuiyin_2.png，
 https://examplebucket-1250000000.picsh.myqcloud.com/shuiyin_2.png
 就是非法的水印 URL。
 */
@property (nonatomic, copy) NSString *imageURL;

/**
 盲水印文字，需要经过 URL 安全的 Base64 编码。当 type 为3时必填，type 为1或2时无效。
 */
@property (nonatomic, copy) NSString *text;

/// 只对全盲水印（type=2）有效。level 的取值范围为{1,2,3}，默认值为1，level
/// 值越大则图片受影响程度越大、盲水印效果越好。
@property (nonatomic, assign) NSInteger level;

@end

NS_ASSUME_NONNULL_END
