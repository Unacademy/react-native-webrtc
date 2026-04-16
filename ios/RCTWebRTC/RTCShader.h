//
//  RTCShader.h
//  RCTWebRTC
//

#import <Foundation/Foundation.h>
#import <TargetConditionals.h>
#if TARGET_OS_IPHONE
#import <OpenGLES/ES3/gl.h>
#elif TARGET_OS_OSX
#import <OpenGL/gl3.h>
#endif
#import <WebRTC/RTCVideoFrame.h>

#define RTC_EXPORT __attribute__((visibility("default")))

RTC_EXPORT GLuint RTCCreateShader(GLenum type, const GLchar *source);
RTC_EXPORT GLuint RTCCreateProgram(GLuint vertexShader, GLuint fragmentShader);
RTC_EXPORT GLuint RTCCreateProgramFromFragmentSource(const char fragmentShaderSource[]);
RTC_EXPORT BOOL RTCCreateVertexBuffer(GLuint *vertexBuffer, GLuint *vertexArray);
RTC_EXPORT void RTCSetVertexData(RTCVideoRotation rotation);
