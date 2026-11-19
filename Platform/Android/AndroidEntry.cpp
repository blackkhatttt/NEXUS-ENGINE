#if defined(__ANDROID__)
#include <jni.h>
extern "C" JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM*, void*) {
    return JNI_VERSION_1_6;
}
extern "C" JNIEXPORT jstring JNICALL
Java_com_nexus_hub_MainActivity_nativeEngineVersion(JNIEnv* env, jobject) {
    return env->NewStringUTF("NEXUS ENGINE 0.1.0");
}
#endif
