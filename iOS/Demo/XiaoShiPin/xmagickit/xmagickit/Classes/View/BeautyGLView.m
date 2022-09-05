#import "BeautyGLView.h"

@interface BeautyGLView () {
    GLuint _displayTextureID;
    
    GLuint _renderProgram;
    GLuint _renderLocation;
    GLuint _renderInputImageTexture;
    GLuint _renderTextureCoordinate;
    GLfloat vertex[8];
    
    CGSize _savedSize;
    int    _orientation;
}

//ciContext
@property (nonatomic, strong) CIContext *ciContext;
@property (nonatomic, strong) EAGLContext *glContext;
@property (nonatomic, assign) unsigned int screenWidth;
@property (nonatomic, assign) unsigned int screenHeight;
@property (nonatomic, strong) UIImageView *imageView;
@property (nonatomic, assign) NSTimeInterval lastRenderTime;
@end

static float TEXTURE_FLIPPED[] = {0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f,};
static float TEXTURE_ROTATED_0[] = {
    0.0f, 0.0f,
    1.0f, 0.0f,
    0.0f, 1.0f,
    1.0f, 1.0f,
};
static float TEXTURE_ROTATED_90[] = {
    0.0f, 1.0f,
    0.0f, 0.0f,
    1.0f, 1.0f,
    1.0f, 0.0f,
};
static float TEXTURE_ROTATED_180[] = {
    1.0f, 1.0f,
    0.0f, 1.0f,
    1.0f, 0.0f,
    0.0f, 0.0f,
};
static float TEXTURE_ROTATED_270[] = {
    1.0f, 0.0f,
    1.0f, 1.0f,
    0.0f, 0.0f,
    0.0f, 1.0f,
};
static float CUBE[] = {-1.0f, 1.0f, 1.0f, 1.0f, -1.0f, -1.0f, 1.0f, -1.0f,};

#define TTF_STRINGIZE(x) #x
#define TTF_STRINGIZE2(x) TTF_STRINGIZE(x)
#define TTF_SHADER_STRING(text) @ TTF_STRINGIZE2(text)

static NSString *const RENDER_VERTEX = TTF_SHADER_STRING
(
 attribute vec4 position;
 attribute vec4 inputTextureCoordinate;
 varying vec2 textureCoordinate;
 void main(){
     textureCoordinate = inputTextureCoordinate.xy;
     gl_Position = position;
 }
 );

static NSString *const RENDER_FRAGMENT = TTF_SHADER_STRING
(
 precision mediump float;
 varying highp vec2 textureCoordinate;
 uniform sampler2D inputImageTexture;
 void main()
 {
     gl_FragColor = texture2D(inputImageTexture, textureCoordinate);
 }
 );

@implementation BeautyGLView

- (void)dealloc
{
    [EAGLContext setCurrentContext:nil];
    self.glContext = nil;
}

- (instancetype)initWithFrame:(CGRect)frame {
    self = [super initWithFrame:frame];
    if (self) {
        self.glContext = [[EAGLContext alloc] initWithAPI:kEAGLRenderingAPIOpenGLES2];
        self.ciContext = [CIContext contextWithEAGLContext:self.glContext];
        self.context = self.glContext;
        
        if ([EAGLContext currentContext] != self.glContext) {
            [EAGLContext setCurrentContext:self.glContext];
        }
        [self loadRenderShader];
        CGFloat scale = [UIScreen mainScreen].scale;
        UIInterfaceOrientation orientation = [[UIApplication sharedApplication] statusBarOrientation];
        if (orientation == UIInterfaceOrientationPortrait
            || orientation == UIInterfaceOrientationPortraitUpsideDown) {
            _screenWidth = [UIScreen mainScreen].bounds.size.width * scale;
            _screenHeight = [UIScreen mainScreen].bounds.size.height * scale;
        } else {
            _screenHeight = [UIScreen mainScreen].bounds.size.width * scale;
            _screenWidth = [UIScreen mainScreen].bounds.size.height * scale;
        }
        [self be_resetVertex:720 imageHeight:1280 fitType:0];
        
//        [CSToastManager setQueueEnabled:YES];
        _lastRenderTime = [NSDate date].timeIntervalSince1970;
    }
    return self;
}

- (void)resetWidthAndHeight {
    _screenWidth = (int)self.drawableWidth;
    _screenHeight = (int)self.drawableHeight;
}

/*
 * 将纹理绘制到屏幕上
 */
- (void)renderWithTexture:(unsigned int)name
                     size:(CGSize)size
                  flipped:(BOOL)flipped
      applyingOrientation:(int)orientation
                  fitType:(int)fitType {
    if ([EAGLContext currentContext] != self.context) {
        [EAGLContext setCurrentContext:self.context];
    }
    if (!glIsTexture(name)) {
        NSLog(@"texture %d is invalide", name);
    }
    
    bool isLandscape = orientation % 180 != 0;
    [self be_resetVertex:isLandscape ? size.height : size.width imageHeight:isLandscape ? size.width : size.height fitType:fitType];
    _displayTextureID = name;
    _orientation = orientation;
    [self display];
}

- (void)drawRect:(CGRect)rect {
    [super drawRect:rect];
    
    CGSize size = self.bounds.size;
    if (!CGSizeEqualToSize(size, _savedSize)) {
        [self resetWidthAndHeight];
        _savedSize = size;
    }
    
    glClearColor(0.0, 0.0, 0.0, 0.0);
    glClear(GL_COLOR_BUFFER_BIT| GL_DEPTH_BUFFER_BIT);
    
    glUseProgram(_renderProgram);
    
    glVertexAttribPointer(_renderLocation, 2, GL_FLOAT, false, 0, vertex);
    glEnableVertexAttribArray(_renderLocation);
    glVertexAttribPointer(_renderTextureCoordinate, 2, GL_FLOAT, false, 0, [self be_textureCoordinate]);
//    glVertexAttribPointer(_renderTextureCoordinate, 2, GL_FLOAT, false, 0, TEXTURE_FLIPPED);
    glEnableVertexAttribArray(_renderTextureCoordinate);
    
    glActiveTexture(GL_TEXTURE0);
    glBindTexture(GL_TEXTURE_2D, _displayTextureID);
    glUniform1i(_renderInputImageTexture, 0);
    glViewport(0, 0, _screenWidth, _screenHeight);
    glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
    
    glDisableVertexAttribArray(_renderLocation);
    glDisableVertexAttribArray(_renderTextureCoordinate);
    glActiveTexture(GL_TEXTURE0);
    glBindTexture(GL_TEXTURE_2D, 0);
    
    glUseProgram(0);
    glFlush();
    glFinish();
    
#if BEF_AUTO_PROFILE
    /// recored the time
#if TIME_LOG
    NSTimeInterval currentTime =  [NSDate date].timeIntervalSince1970;
    NSTimeInterval diff = (currentTime - _lastRenderTime);
    double fps = 1.0 / diff / 1000.0;
    [BETimeRecoder be_recordOnce:@"fps" interval:fps];
    _lastRenderTime = currentTime;
#endif
#endif
}

/*
 * load resize shader
 */
- (void) loadRenderShader{
    GLuint vertexShader = [self compileShader:RENDER_VERTEX withType:GL_VERTEX_SHADER];
    GLuint fragmentShader = [self compileShader:RENDER_FRAGMENT withType:GL_FRAGMENT_SHADER];
    
    _renderProgram = glCreateProgram();
    glAttachShader(_renderProgram, vertexShader);
    glAttachShader(_renderProgram, fragmentShader);
    glLinkProgram(_renderProgram);
    
    GLint linkSuccess;
    glGetProgramiv(_renderProgram, GL_LINK_STATUS, &linkSuccess);
    if (linkSuccess == GL_FALSE){
        NSLog(@"BERenderHelper link shader error");
    }
    
    glUseProgram(_renderProgram);
    _renderLocation = glGetAttribLocation(_renderProgram, "position");
    _renderTextureCoordinate = glGetAttribLocation(_renderProgram, "inputTextureCoordinate");
    _renderInputImageTexture = glGetUniformLocation(_renderProgram, "inputImageTexture");
    
    if (vertexShader)
        glDeleteShader(vertexShader);
    
    if (fragmentShader)
        glDeleteShader(fragmentShader);
}

- (int) compileShader:(NSString *)shaderString withType:(GLenum)shaderType {
    GLuint shaderHandle = glCreateShader(shaderType);
    const char * shaderStringUTF8 = [shaderString UTF8String];
    
    int shaderStringLength = (int) [shaderString length];
    glShaderSource(shaderHandle, 1, &shaderStringUTF8, &shaderStringLength);
    glCompileShader(shaderHandle);
    GLint success;
    glGetShaderiv(shaderHandle, GL_COMPILE_STATUS, &success);
    
    if (success == GL_FALSE){
        NSLog(@"BErenderHelper compiler shader error: %s", shaderStringUTF8);
        return 0;
    }
    return shaderHandle;
}

- (void)releaseContext {
    [EAGLContext setCurrentContext:nil];
}

#pragma mark - private

/// 计算 texture vertex
/// @param imageWidth texture 宽度
/// @param imageHeight texture 高度
/// @param fitType 填充方式，0: 铺满 1: 留白
- (void)be_resetVertex:(int)imageWidth imageHeight:(int)imageHeight fitType:(int)fitType {
    float ratio[2] = {1.0, 1.0};
    
    int outputWidth = _screenWidth;
    int outputHeight = _screenHeight;
    
    float finalRatio;
    if (fitType == 0) {
        finalRatio = MIN((float)outputWidth / imageWidth, (float)outputHeight / imageHeight);
    } else {
        finalRatio = MAX((float)outputWidth / imageWidth, (float)outputHeight / imageHeight);
    }
    
    int imageNewHeight = round(imageHeight * finalRatio);
    int imageNewWidth = round(imageWidth * finalRatio);
    
    ratio[0] = imageNewHeight / (float)outputHeight;
    ratio[1] = imageNewWidth / (float)outputWidth;
    
    for (int i = 0; i < 4; i ++){
        vertex[i * 2] = CUBE[i * 2] / ratio[0];
        vertex[i * 2 + 1] = CUBE[i * 2 + 1] / ratio[1];
    }
}

- (float *)be_textureCoordinate {
    switch (_orientation) {
        case 0:
            return TEXTURE_ROTATED_0;
        case 90:
            return TEXTURE_ROTATED_90;
        case 180:
            return TEXTURE_ROTATED_180;
        case 270:
            return TEXTURE_ROTATED_270;
        default:
            return TEXTURE_ROTATED_0;
    }
}

- (void)checkGLError {
    int error = glGetError();
    if (error != GL_NO_ERROR) {
        NSLog(@"%d", error);
        @throw [NSException exceptionWithName:@"GLError" reason:@"error " userInfo:nil];
    }
}
@end
