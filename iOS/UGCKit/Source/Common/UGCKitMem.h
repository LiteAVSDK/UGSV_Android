// Copyright (c) 2019 Tencent. All rights reserved.

#ifndef Memory_h
#define Memory_h

#define WEAKIFY(x) __weak __typeof(x) weak_##x = x
#define STRONGIFY(x) __strong __typeof(weak_##x) x = weak_##x
#define STRONGIFY_OR_RETURN(x) __strong __typeof(weak_##x) x = weak_##x; if (x == nil) {return;};

#endif /* Memory_h */
