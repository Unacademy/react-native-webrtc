//
//  RTCShader.h
//  RCTWebRTC
//
//  Created by Kushagra Gupta on 02/07/19.
//

//#import "base/RTCVideoFrame.h"

#import <Foundation/Foundation.h>
#import <OpenGLES/ES3/gl.h>
#import <WebRTC/RTCVideoFrame.h>
#define RTC_EXPORT __attribute__((visibility("default")))

//RTC_EXPORT const char kRTCVertexShaderSource[];

RTC_EXPORT GLuint RTCCreateShader(GLenum type, const GLchar* source);
RTC_EXPORT GLuint RTCCreateProgram(GLuint vertexShader, GLuint fragmentShader);
RTC_EXPORT GLuint
RTCCreateProgramFromFragmentSource(const char fragmentShaderSource[]);
RTC_EXPORT BOOL RTCCreateVertexBuffer(GLuint* vertexBuffer,
                                      GLuint* vertexArray);
RTC_EXPORT void RTCSetVertexData(RTCVideoRotation rotation);
