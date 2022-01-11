// Copyright (c) 2019 Tencent. All rights reserved.

#ifndef UGCKit_Localization_h
#define UGCKit_Localization_h

__attribute__((annotate("returns_localized_nsstring")))
NS_INLINE NSString *LocalizationNotNeeded(NSString *s) {
    return s;
}

#endif /* UGCKit_Localization_h */
