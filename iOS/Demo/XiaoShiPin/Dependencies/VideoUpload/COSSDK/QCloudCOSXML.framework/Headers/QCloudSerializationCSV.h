//
//  QCloudSerializationCSV.h
//  QCloudSerializationCSV
//
//  Created by tencent
//  Copyright (c) 2015年 tencent. All rights reserved.
//
//   ██████╗  ██████╗██╗      ██████╗ ██╗   ██╗██████╗     ████████╗███████╗██████╗ ███╗   ███╗██╗███╗   ██╗ █████╗ ██╗         ██╗      █████╗
//   ██████╗
//  ██╔═══██╗██╔════╝██║     ██╔═══██╗██║   ██║██╔══██╗    ╚══██╔══╝██╔════╝██╔══██╗████╗ ████║██║████╗  ██║██╔══██╗██║         ██║ ██╔══██╗██╔══██╗
//  ██║   ██║██║     ██║     ██║   ██║██║   ██║██║  ██║       ██║   █████╗  ██████╔╝██╔████╔██║██║██╔██╗ ██║███████║██║         ██║ ███████║██████╔╝
//  ██║▄▄ ██║██║     ██║     ██║   ██║██║   ██║██║  ██║       ██║   ██╔══╝  ██╔══██╗██║╚██╔╝██║██║██║╚██╗██║██╔══██║██║         ██║ ██╔══██║██╔══██╗
//  ╚██████╔╝╚██████╗███████╗╚██████╔╝╚██████╔╝██████╔╝       ██║   ███████╗██║  ██║██║ ╚═╝ ██║██║██║ ╚████║██║  ██║███████╗    ███████╗██║
//  ██║██████╔╝
//   ╚══▀▀═╝  ╚═════╝╚══════╝ ╚═════╝  ╚═════╝ ╚═════╝        ╚═╝   ╚══════╝╚═╝  ╚═╝╚═╝     ╚═╝╚═╝╚═╝  ╚═══╝╚═╝  ╚═╝╚══════╝    ╚══════╝╚═╝ ╚═╝╚═════╝
//
//
//                                                                              _             __                 _                _
//                                                                             (_)           / _|               | |              | |
//                                                          ___  ___ _ ____   ___  ___ ___  | |_ ___  _ __    __| | _____   _____| | ___  _ __   ___ _
//                                                          __ ___
//                                                         / __|/ _ \ '__\ \ / / |/ __/ _ \ |  _/ _ \| '__|  / _` |/ _ \ \ / / _ \ |/ _ \| '_ \ / _ \
//                                                         '__/ __|
//                                                         \__ \  __/ |   \ V /| | (_|  __/ | || (_) | |    | (_| |  __/\ V /  __/ | (_) | |_) |  __/
//                                                         |  \__
//                                                         |___/\___|_|    \_/ |_|\___\___| |_| \___/|_|     \__,_|\___| \_/ \___|_|\___/| .__/
//                                                         \___|_|  |___/
//    ______ ______ ______ ______ ______ ______ ______ ______                                                                            | |
//   |______|______|______|______|______|______|______|______|                                                                           |_|
//

#import <Foundation/Foundation.h>
#import <QCloudCore/QCloudCore.h>
#import "QCloudInputFileHeaderInfoEnum.h"
#import "QCloudOutputQuoteFieldsEnum.h"

NS_ASSUME_NONNULL_BEGIN
/**
描述在CSV对象格式下所需的文件参数。
*/
@interface QCloudSerializationCSV : NSObject
/**
将 CSV 对象中记录分隔为不同行的字符，默认您通过\n进行分隔。您可以指定任意8进制字符，
 如逗号、分号、Tab 等。该参数最多支持2个字节，即您可以输入\r\n这类格式的分隔符。默认值为\n。
*/
@property (strong, nonatomic) NSString *recordDelimiter;
/**
    指定分隔 CSV 对象中每一行的字符，默认您通过,进行分隔。您可以指定任意8进制字符，
 该参数最多支持1个字节。默认值为,。
    */
@property (strong, nonatomic) NSString *fieldDelimiter;
/**
    如果您待检索的 CSV 对象中存在包含分隔符的字符串，您可以使用 QuoteCharacter 进行转义，
 避免该字符串被切割成几个部分。如 CSV 对象中存在"a, b"这个字符串，双引号"可以避免这一字符串被分隔成
 a 和 b 两个字符。默认值为"。
    */
@property (strong, nonatomic) NSString *quoteCharacter;
/**
    如果您待检索的字符串中已经存在"，那您需要使用"进行转义以保证字符串可以正常转义。
 如您的字符串 """ a , b """将会被解析为" a , b "。默认值为"。
    */
@property (strong, nonatomic) NSString *quoteEscapeCharacter;
/**
    指定待检索对象中是否存在与分隔符相同且需要用"转义的字符。设定为 TRUE 时，COS Select 将会在检索进行转义，
 这会导致检索性能下降；设定为 FALSE 时，则不会做转义处理。默认值为 FALSE。
    */
@property (strong, nonatomic) NSString *inputAllowQuotedRecordDelimiter;
/**
    待检索对象中是否存在列表头。该参数为存在 NONE、USE、IGNORE 三个选项。NONE 代表对象
 中没有列表头，USE 代表对象中存在列表头并且您可以使用表头进行检索（例如 SELECT "name" FROM COSObject）
 ，IGNORE 代表对象中存在列表头且您不打算使用表头进行检索（但您仍然可以通过列索引进行检索，
 如 SELECT s._1 FROM COSObject s）。合法值为 NONE、USE、IGNORE。
    */
@property (assign, nonatomic) QCloudInputFileHeaderInfo inputFileHeaderInfo;
/**
    指定某行记录为注释行，该字符会被添加到该行记录的首字符。如果某一行记录被指定为注释，
 则 COS Select 将不对此行做任何分析。默认值为#。
    */
@property (strong, nonatomic) NSString *inputComments;
/**
    指定输出结果为文件时，是否需要使用"进行转义。可选项包括 ALWAYS、ASNEEDED、ALWAYS 代表
 对所有本次输出的检索文件应用"，ASNEEDED 代表仅在需要时使用。合法值为 ALWAYS、ASNEEDED，默认值为 ASNEEDED。
    */
@property (assign, nonatomic) QCloudOutputQuoteFields quoteFields;
@end
NS_ASSUME_NONNULL_END
