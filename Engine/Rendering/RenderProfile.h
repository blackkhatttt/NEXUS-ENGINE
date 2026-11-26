#pragma once

#include <cstdint>

namespace nexus::rendering {

enum class RenderProfile : std::uint8_t { Compatibility, Forward, Advanced };
enum class RenderPlatform : std::uint8_t { Desktop, Android, Web };

// Engine feature targets only; a real backend must intersect these with GPU capabilities.
struct RenderProfileFeatures final {
    bool dynamicLighting = false;
    bool shadows = false;
    bool advancedMaterials = false;
    bool postProcessing = false;
    bool computeEffects = false;
    std::uint8_t maxDirectionalLights = 1;
};

[[nodiscard]] constexpr bool isProfileSupported(RenderProfile profile, RenderPlatform platform) noexcept {
    if (profile == RenderProfile::Compatibility) return true;
    return platform == RenderPlatform::Desktop || platform == RenderPlatform::Android;
}

[[nodiscard]] constexpr RenderProfileFeatures getProfileFeatures(RenderProfile profile) noexcept {
    switch (profile) {
        case RenderProfile::Compatibility:
            return {.dynamicLighting=false, .shadows=false, .advancedMaterials=false,
                    .postProcessing=false, .computeEffects=false, .maxDirectionalLights=1};
        case RenderProfile::Forward:
            return {.dynamicLighting=true, .shadows=true, .advancedMaterials=false,
                    .postProcessing=true, .computeEffects=false, .maxDirectionalLights=2};
        case RenderProfile::Advanced:
            return {.dynamicLighting=true, .shadows=true, .advancedMaterials=true,
                    .postProcessing=true, .computeEffects=true, .maxDirectionalLights=4};
    }
    return {};
}

} // namespace nexus::rendering
