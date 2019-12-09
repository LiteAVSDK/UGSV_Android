
#ifndef TXUGCUtil_h
#define TXUGCUtil_h

#import <AVFoundation/AVFoundation.h>
#import "TXUGCPublishTypeDef.h"

@interface TXUGCPublishUtil : NSObject

+(CMSampleBufferRef)createAudioSample:(void *)audioData
                                 size:(UInt32)len
                           timingInfo:(CMSampleTimingInfo)info
                       numberChannels:(int)channels
                           SampleRate:(int)sampleRate;

+(NSString*) getFileSHA1Signature:(NSString*)filePath;

+(NSString*) renameFile:(NSString*)filePath newFileName:(NSString*)newName;

+(void) removeCacheFile:(NSString*)filePath;

+(void)clearFileType:(NSArray*)extensions AtFolderPath:(NSString*)path;

+(NSString*)getCacheFolderPath;

+(NSString *)getFileNameByTimeNow:(NSString *)type fileType:(NSString *)fileType;

+(UIImage*)imageFromPixelBuffer:(CVPixelBufferRef)pixelBuffer;

+(void)checkVideoPath:(NSString *)videoPath;

+(int)savePixelBuffer:(CVPixelBufferRef)pixelBuffer ToJPEGPath:(NSString*)path;

+(void)save:(UIImage*)uiImage ToPath:(NSString*)path;

+(UIImage *)loadThumbNail:(NSURL *)urlVideo;
@end

#endif /* TXUGCUtil_h */
