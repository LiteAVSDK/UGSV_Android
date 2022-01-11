
#import <Foundation/Foundation.h>
#import <AVFoundation/AVFoundation.h>
#import <CommonCrypto/CommonDigest.h>
#import <CoreMedia/CoreMedia.h>
#import <ImageIO/ImageIO.h>
#import <UIKit/UIKit.h>
#import "TXUGCPublishUtil.h"


#undef _MODULE_
#define _MODULE_ "TXUGCPublishUtil"

#define degreesToRadians( degrees ) ( ( degrees ) / 180.0 * M_PI )

@implementation TXUGCPublishUtil

+(CMSampleBufferRef)createAudioSample:(void *)audioData
                                 size:(UInt32)len
                           timingInfo:(CMSampleTimingInfo)info
                       numberChannels:(int)channels
                           SampleRate:(int)sampleRate
{
    AudioBufferList audioBufferList;
    audioBufferList.mNumberBuffers = 1;
    audioBufferList.mBuffers[0].mNumberChannels=channels;
    audioBufferList.mBuffers[0].mDataByteSize=len;
    audioBufferList.mBuffers[0].mData = audioData;
    
    AudioStreamBasicDescription asbd;
    asbd.mSampleRate        = sampleRate;
    asbd.mFormatID          = kAudioFormatLinearPCM;
    asbd.mFormatFlags       = kLinearPCMFormatFlagIsPacked | kLinearPCMFormatFlagIsSignedInteger;
    asbd.mBytesPerPacket    = 2*channels;
    asbd.mFramesPerPacket   = 1;
    asbd.mBytesPerFrame     = 2*channels;
    asbd.mChannelsPerFrame  = channels;
    asbd.mBitsPerChannel    = 16;
    asbd.mReserved          = 0;
    
    CMSampleBufferRef buff = NULL;
    CMFormatDescriptionRef format = NULL;
    
    OSStatus error = 0;
    error = CMAudioFormatDescriptionCreate(kCFAllocatorDefault, &asbd, 0, NULL, 0, NULL, NULL, &format);
    
    error = CMSampleBufferCreate(kCFAllocatorDefault, NULL, false, NULL, NULL, format, len/(2*channels), 1, &info, 0, NULL, &buff);
    
    CFRelease(format);
    
    if ( error ) {
        return NULL;
    }
    
    error = CMSampleBufferSetDataBufferFromAudioBufferList(buff, kCFAllocatorDefault, kCFAllocatorDefault, 0, &audioBufferList);
    
    if( error )
    {
        CFRelease(buff);
        return NULL;
    }
    
    return buff;
}


+(NSString*) getFileSHA1Signature:(NSString*)filePath
{
    if (filePath == nil) {
        return nil;
    }
    
    if (NO == [[NSFileManager defaultManager] fileExistsAtPath:filePath]) {
        return nil;
    }
    
    CFStringRef strSHA1Ref = NULL;
    CFURLRef fileURL = NULL;
    CFReadStreamRef readStream = NULL;
    
    do {
        fileURL = CFURLCreateWithFileSystemPath(kCFAllocatorDefault, (__bridge CFStringRef)filePath, kCFURLPOSIXPathStyle, (Boolean)false);
        if (!fileURL) {
            break;
        }
        
        readStream = CFReadStreamCreateWithFile(kCFAllocatorDefault, fileURL);
        if (!readStream) {
            break;
        }
        
        bool openSuccess = (bool)CFReadStreamOpen(readStream);
        if (!openSuccess) {
            break;
        }
        
        CC_SHA1_CTX sha1Ctx;
        CC_SHA1_Init(&sha1Ctx);
        
        size_t chunkSizeForReadingData = 1024 * 8;
        uint8_t buffer[chunkSizeForReadingData];
        bool hasMoreData = true;
        while (hasMoreData) {
            memset(buffer, 0, sizeof(buffer));
            
            CFIndex readBytesCount = CFReadStreamRead(readStream, (UInt8*)buffer, (CFIndex)sizeof(buffer));
            if (readBytesCount == -1) {
                break;
            }
            
            if (readBytesCount == 0) {
                hasMoreData = false;
                continue;
            }
            
            CC_SHA1_Update(&sha1Ctx, (const void*)buffer, (CC_LONG)readBytesCount);
        }
        
        unsigned char digest[CC_SHA1_DIGEST_LENGTH];
        CC_SHA1_Final(digest, &sha1Ctx);
        
        if (hasMoreData) {
            break;
        }
        
        char hash[2 * sizeof(digest) + 1] = {0};
        for (size_t i = 0; i < sizeof(digest); ++i) {
            sprintf(hash + (2 * i), "%02x", (int)(digest[i]));
        }
        strSHA1Ref = CFStringCreateWithCString(kCFAllocatorDefault,
                                               (const char *)hash,
                                               kCFStringEncodingUTF8);
    } while (0);
    
    
    
    if (fileURL) {
        CFRelease(fileURL);
    }
    
    if (readStream) {
        CFReadStreamClose(readStream);
        CFRelease(readStream);
    }
    
    return (__bridge_transfer NSString *)strSHA1Ref;
}

+(NSString*) renameFile:(NSString*)filePath newFileName:(NSString*)newName {
    if (filePath == nil || [[NSFileManager defaultManager] fileExistsAtPath:filePath] != YES) {
        NSLog(@"rename file failed, file not exist [%s]", filePath == nil ? "" : [filePath UTF8String]);
        return nil;
    }
    
    if (newName == nil) {
        NSLog(@"rename file failed, invalid fileName");
        return nil;
    }
    
    
    NSString * newFileName = [NSString stringWithFormat:@"%@.%@", newName, [filePath pathExtension]];
    
    NSString * newFilePath = [filePath stringByDeletingLastPathComponent];
    
    newFilePath = [newFilePath stringByAppendingPathComponent:newFileName];
    
    NSError * error;
    if ([[NSFileManager defaultManager] fileExistsAtPath:newFilePath] == YES) {
        [[NSFileManager defaultManager] removeItemAtPath:newFilePath error:&error];
    }
    
    if ([[NSFileManager defaultManager] moveItemAtPath:filePath toPath:newFilePath error:&error]!= YES) {
        NSLog(@"rename file failed: %@", [error localizedDescription]);
        return nil;
    }
    
    return newFilePath;
}

+(void) removeCacheFile:(NSString*)filePath {
    if ([[NSFileManager defaultManager] fileExistsAtPath:filePath] == YES) {
        [[NSFileManager defaultManager] removeItemAtPath:filePath error:nil]; //发布成功，删除视频文件
    }
}

+(void)clearFileType:(NSArray*)extensions AtFolderPath:(NSString*)path
{
    NSFileManager *fileManager = [NSFileManager defaultManager];
    
    NSArray *contents = [fileManager contentsOfDirectoryAtPath:path error:NULL];
    NSEnumerator *e = [contents objectEnumerator];
    NSString *filename;
    while ((filename = [e nextObject])) {
        
        for(int i=0; i<extensions.count; ++i)
        {
            if ([[filename pathExtension] isEqualToString:extensions[i]]) {
                
                [fileManager removeItemAtPath:[path stringByAppendingPathComponent:filename] error:NULL];
            }
        }
    }
}

+(NSString*)getCacheFolderPath
{
    return [NSTemporaryDirectory() stringByAppendingPathComponent:@"TXUGC"];
}

+(NSString *)getFileNameByTimeNow:(NSString *)type fileType:(NSString *)fileType {
    NSTimeInterval now = [[NSDate date] timeIntervalSince1970];
    NSDateFormatter * formatter = [[NSDateFormatter alloc] init];
    [formatter setDateFormat:@"yyyyMMdd_HHmmss"];
    NSDate * NowDate = [NSDate dateWithTimeIntervalSince1970:now];
    ;
    NSString * timeStr = [formatter stringFromDate:NowDate];
    NSString *fileName = ((fileType == nil) ||
                          (fileType.length == 0)
                          ) ? [NSString stringWithFormat:@"%@_%@",type,timeStr] : [NSString stringWithFormat:@"%@_%@.%@",type,timeStr,fileType];
    return fileName;
}

+(UIImage*)imageFromPixelBuffer:(CVPixelBufferRef)pixelBuffer
{
    CVPixelBufferLockBaseAddress(pixelBuffer, kCVPixelBufferLock_ReadOnly);
    CIImage *ciImage = [CIImage imageWithCVPixelBuffer:pixelBuffer];
    CIContext *temporaryContext = [CIContext contextWithOptions:nil];
    CGImageRef videoImage = [temporaryContext createCGImage:ciImage
                                                   fromRect:CGRectMake(0,
                                                                       0,
                                                                       CVPixelBufferGetWidth(pixelBuffer),
                                                                       CVPixelBufferGetHeight(pixelBuffer)
                                                                       )];
    UIImage *image = [[UIImage alloc] initWithCGImage:videoImage];
    CGImageRelease(videoImage);
    CVPixelBufferUnlockBaseAddress(pixelBuffer, kCVPixelBufferLock_ReadOnly);
    
    return image;
}

+(int)savePixelBuffer:(CVPixelBufferRef)pixelBuffer ToJPEGPath:(NSString*)path
{
    [TXUGCPublishUtil save:[TXUGCPublishUtil imageFromPixelBuffer:pixelBuffer] ToPath:path];
    
    return 0;
}

+(void)checkVideoPath:(NSString *)videoPath
{
    NSFileManager *manager = [[NSFileManager alloc] init];
    if ([manager fileExistsAtPath:videoPath]) {
        BOOL success =  [manager removeItemAtPath:videoPath error:nil];
        if (success) {
            //NSLog(@"Already exist. Removed!");
        }
    }
}

+(void)save:(UIImage*)uiImage ToPath:(NSString*)path
{
    if (uiImage && path) {
        // 保证目录存在
        [[NSFileManager defaultManager] createDirectoryAtPath:[path stringByDeletingLastPathComponent]
                                  withIntermediateDirectories:YES
                                                   attributes:nil
                                                        error:nil];
        
        [UIImageJPEGRepresentation(uiImage, 1.0) writeToFile:path atomically:YES];
    }
}

+(UIImage *)loadThumbNail:(NSURL *)urlVideo
{
    AVURLAsset *asset = [[AVURLAsset alloc] initWithURL:urlVideo options:nil];
    AVAssetImageGenerator *generate = [[AVAssetImageGenerator alloc] initWithAsset:asset];
    generate.appliesPreferredTrackTransform= YES;
    NSError *err = NULL;
    CMTime time = CMTimeMake(15, 30);
    CGImageRef imgRef = [generate copyCGImageAtTime:time actualTime:nil error:&err];
    UIImage *image =  [[UIImage alloc] initWithCGImage:imgRef];
    return image;
}

@end
