// Initial Android native-library entry point.
// The Java/Kotlin application wrapper and full Android lifecycle integration
// will be added in a later milestone.

#if defined(__ANDROID__)
#include <jni.h>

extern "C" JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM*, void*)
{
    return JNI_VERSION_1_6;
}
#endif
