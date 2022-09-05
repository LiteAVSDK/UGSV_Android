//
//  SDKHeader.h
//  TXLiteAVDemo
//
//  Created by shengcui on 2018/9/12.
//  Copyright © 2018年 Tencent. All rights reserved.
//

//@import TXLiteAVSDK_Enterprise;
#ifndef LIVE
#ifndef ENABLE_INTERNATIONAL
#import "TXAudioCustomProcessDelegate.h"
#import "TXAudioRawDataDelegate.h"

#ifndef TRTC
#ifndef DISABLE_VOD
#import "TXBitrateItem.h"
#import "TXImageSprite.h"
#import "TXPlayerAuthParams.h"
#import "TXVodPlayConfig.h"
#import "TXVodPlayListener.h"
#import "TXVodPlayer.h"
#endif
#endif
#endif
#endif

#ifndef LIVE
#import "TXLiveAudioSessionDelegate.h"
#import "TXLiveBase.h"
#endif

#ifndef ENABLE_INTERNATIONAL
#ifndef LIVE
#import "TXLivePlayConfig.h"
#import "TXLivePlayListener.h"
#import "TXLivePlayer.h"
#import "TXLiveRecordListener.h"
#import "TXLiveRecordTypeDef.h"
#import "TXLiveSDKEventDef.h"
#import "TXLiveSDKTypeDef.h"

#ifndef TRTC
#ifndef SMART
#import "TXUGCBase.h"
#import "TXUGCPartsManager.h"
#import "TXUGCRecord.h"
#import "TXUGCRecordListener.h"
#import "TXUGCRecordTypeDef.h"
#import "TXVideoEditer.h"
#import "TXVideoEditerListener.h"
#import "TXVideoEditerTypeDef.h"
#endif
#endif


#import "TXVideoCustomProcessDelegate.h"
#endif
#endif


#ifdef LIVE
#import "V2TXLivePremier.h"
#endif
