//
//  RTCShader.mm
//  RCTWebRTC
//

#import <Foundation/Foundation.h>
#import "RTCShader.h"

#if TARGET_OS_IPHONE
#import <OpenGLES/ES3/gl.h>
#else
#import <OpenGL/gl3.h>
#endif

#include <algorithm>
#include <array>
#include <memory>

#import "RTCOpenGLDefines.h"

const char kRTCVertexShaderSource[] =
        SHADER_VERSION
        VERTEX_SHADER_IN " vec2 position;\n"
        VERTEX_SHADER_IN " vec2 texcoord;\n"
        VERTEX_SHADER_OUT " vec2 v_texcoord;\n"
        "void main() {\n"
        "    gl_Position = vec4(position.x, position.y, 0.0, 1.0);\n"
        "    v_texcoord = texcoord;\n"
        "}\n";

GLuint RTCCreateShader(GLenum type, const GLchar *source) {
    GLuint shader = glCreateShader(type);
    if (!shader) {
        return 0;
    }
    glShaderSource(shader, 1, &source, NULL);
    glCompileShader(shader);
    GLint compileStatus = GL_FALSE;
    glGetShaderiv(shader, GL_COMPILE_STATUS, &compileStatus);
    if (compileStatus == GL_FALSE) {
        GLint logLength = 0;
        glGetShaderiv(shader, GL_INFO_LOG_LENGTH, &logLength);
        if (logLength > 0) {
            std::unique_ptr<char[]> compileLog(new char[logLength]);
            glGetShaderInfoLog(shader, logLength, NULL, compileLog.get());
        }
        glDeleteShader(shader);
        shader = 0;
    }
    return shader;
}

GLuint RTCCreateProgram(GLuint vertexShader, GLuint fragmentShader) {
    if (vertexShader == 0 || fragmentShader == 0) {
        return 0;
    }
    GLuint program = glCreateProgram();
    if (!program) {
        return 0;
    }
    glAttachShader(program, vertexShader);
    glAttachShader(program, fragmentShader);
    glLinkProgram(program);
    GLint linkStatus = GL_FALSE;
    glGetProgramiv(program, GL_LINK_STATUS, &linkStatus);
    if (linkStatus == GL_FALSE) {
        glDeleteProgram(program);
        program = 0;
    }
    return program;
}

GLuint RTCCreateProgramFromFragmentSource(const char fragmentShaderSource[]) {
    GLuint vertexShader = RTCCreateShader(GL_VERTEX_SHADER, kRTCVertexShaderSource);
    GLuint fragmentShader = RTCCreateShader(GL_FRAGMENT_SHADER, fragmentShaderSource);
    GLuint program = RTCCreateProgram(vertexShader, fragmentShader);
    if (vertexShader) {
        glDeleteShader(vertexShader);
    }
    if (fragmentShader) {
        glDeleteShader(fragmentShader);
    }

    GLint position = glGetAttribLocation(program, "position");
    GLint texcoord = glGetAttribLocation(program, "texcoord");
    if (position < 0 || texcoord < 0) {
        glDeleteProgram(program);
        return 0;
    }

    glVertexAttribPointer(position, 2, GL_FLOAT, GL_FALSE, 4 * sizeof(GLfloat), (void *)0);
    glEnableVertexAttribArray(position);

    glVertexAttribPointer(
            texcoord, 2, GL_FLOAT, GL_FALSE, 4 * sizeof(GLfloat), (void *)(2 * sizeof(GLfloat)));
    glEnableVertexAttribArray(texcoord);

    return program;
}

BOOL RTCCreateVertexBuffer(GLuint *vertexBuffer, GLuint *vertexArray) {
#if !TARGET_OS_IPHONE
    glGenVertexArrays(1, vertexArray);
    if (*vertexArray == 0) {
        return NO;
    }
    glBindVertexArray(*vertexArray);
#endif
    glGenBuffers(1, vertexBuffer);
    if (*vertexBuffer == 0) {
#if !TARGET_OS_IPHONE
        glDeleteVertexArrays(1, vertexArray);
#endif
        return NO;
    }
    glBindBuffer(GL_ARRAY_BUFFER, *vertexBuffer);
    glBufferData(GL_ARRAY_BUFFER, 4 * 4 * sizeof(GLfloat), NULL, GL_DYNAMIC_DRAW);
    return YES;
}

void RTCSetVertexData(RTCVideoRotation rotation) {
    std::array<std::array<GLfloat, 2>, 4> UVCoords = {{
            {{0, 1}},
            {{1, 1}},
            {{1, 0}},
            {{0, 0}},
    }};

    int rotation_offset;
    switch (rotation) {
        case RTCVideoRotation_0:
            rotation_offset = 0;
            break;
        case RTCVideoRotation_90:
            rotation_offset = 1;
            break;
        case RTCVideoRotation_180:
            rotation_offset = 2;
            break;
        case RTCVideoRotation_270:
            rotation_offset = 3;
            break;
    }
    std::rotate(UVCoords.begin(), UVCoords.begin() + rotation_offset, UVCoords.end());

    const GLfloat gVertices[] = {
            -1, -1, UVCoords[0][0], UVCoords[0][1],
            1, -1, UVCoords[1][0], UVCoords[1][1],
            1, 1, UVCoords[2][0], UVCoords[2][1],
            -1, 1, UVCoords[3][0], UVCoords[3][1],
    };

    glBufferSubData(GL_ARRAY_BUFFER, 0, sizeof(gVertices), gVertices);
}
