#include "Rendering/RenderProfile.h"

#include <cassert>

int main() {
    using namespace nexus::rendering;

    assert(isProfileSupported(RenderProfile::Compatibility, RenderPlatform::Desktop));
    assert(isProfileSupported(RenderProfile::Compatibility, RenderPlatform::Android));
    assert(isProfileSupported(RenderProfile::Compatibility, RenderPlatform::Web));
    assert(isProfileSupported(RenderProfile::Forward, RenderPlatform::Desktop));
    assert(isProfileSupported(RenderProfile::Forward, RenderPlatform::Android));
    assert(!isProfileSupported(RenderProfile::Forward, RenderPlatform::Web));
    assert(isProfileSupported(RenderProfile::Advanced, RenderPlatform::Desktop));
    assert(isProfileSupported(RenderProfile::Advanced, RenderPlatform::Android));
    assert(!isProfileSupported(RenderProfile::Advanced, RenderPlatform::Web));

    const auto compatibility = getProfileFeatures(RenderProfile::Compatibility);
    const auto forward = getProfileFeatures(RenderProfile::Forward);
    const auto advanced = getProfileFeatures(RenderProfile::Advanced);
    assert(compatibility.maxDirectionalLights == 1);
    assert(!compatibility.dynamicLighting);
    assert(forward.dynamicLighting && forward.shadows);
    assert(advanced.advancedMaterials && advanced.computeEffects);
    assert(advanced.maxDirectionalLights > forward.maxDirectionalLights);
    return 0;
}
