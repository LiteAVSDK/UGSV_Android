//
//  Mem.h
//  XiaoShiPin
//
//  Created by cui on 2019/11/12.
//  Copyright © 2019 Tencent. All rights reserved.
//

#ifndef Mem_h
#define Mem_h

#define WEAKIFY(x) __weak __typeof(x) weak_##x = self
#define STRONGIFY(x) __strong __typeof(weak_##x) x = weak_##x
#define STRONGIFY_OR_RETURN(x) __strong __typeof(weak_##x) x = weak_##x; if (x == nil) {return;};


#endif /* Mem_h */
