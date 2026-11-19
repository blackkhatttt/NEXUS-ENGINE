#if defined(__ANDROID__)
#include <jni.h>
#include "Scripting/NXS/Runtime.h"
#include <string>

extern "C" JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM*, void*) {
    return JNI_VERSION_1_6;
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_nexus_hub_MainActivity_nativeEngineVersion(JNIEnv* env, jobject) {
    return env->NewStringUTF("NEXUS ENGINE 0.1.0");
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_nexus_hub_MainActivity_nativeRunNXS(JNIEnv* env, jobject, jstring source) {
    if (source == nullptr) return env->NewStringUTF("ERROR: Empty NXS source.");
    const char* chars = env->GetStringUTFChars(source, nullptr);
    if (chars == nullptr) return env->NewStringUTF("ERROR: Could not read NXS source.");
    const std::string code(chars);
    env->ReleaseStringUTFChars(source, chars);

    const nexus::nxs::ExecutionResult result = nexus::nxs::Runtime{}.execute(code);
    std::string output;
    if (!result.success) {
        output = "NXS ERROR";
        if (result.errorLine > 0) output += " • line " + std::to_string(result.errorLine);
        output += "\n" + result.error;
    } else {
        output = "NXS EXECUTION SUCCESSFUL";
        if (!result.projectName.empty()) output += "\nProject: " + result.projectName;
        for (const std::string& line : result.output) output += "\n> " + line;
        if (result.output.empty()) output += "\n(no output)";
    }
    return env->NewStringUTF(output.c_str());
}
#endif
