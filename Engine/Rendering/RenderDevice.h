#pragma once

#include "Rendering/RenderProfile.h"

#include <cstdint>
#include <string>

namespace nexus::rendering {

enum class GraphicsBackend : std::uint8_t { None, Vulkan, DirectX12, Metal, WebGPU };

struct RenderDeviceCreateInfo final {
    RenderProfile profile = RenderProfile::Compatibility;
    RenderPlatform platform = RenderPlatform::Desktop;
    GraphicsBackend preferredBackend = GraphicsBackend::None;
    bool enableDebugValidation = false;
};

struct RenderDeviceCapabilities final {
    GraphicsBackend backend = GraphicsBackend::None;
    std::string deviceName;
    bool supportsComputeShaders = false;
    bool supportsTextureCompression = false;
    bool supportsRayTracing = false;
    std::uint32_t maxTextureDimension2D = 0;
};

// Backend-independent NXRHI contract. Concrete graphics API adapters are future work.
// This header deliberately has no dependency on OpenGL or another graphics API.
class RenderDevice {
public:
    virtual ~RenderDevice() = default;
    [[nodiscard]] virtual bool initialize(const RenderDeviceCreateInfo& createInfo) = 0;
    virtual void shutdown() noexcept = 0;
    [[nodiscard]] virtual bool beginFrame() = 0;
    [[nodiscard]] virtual bool endFrame() = 0;
    virtual void waitIdle() noexcept = 0;
    [[nodiscard]] virtual const RenderDeviceCapabilities& capabilities() const noexcept = 0;
    [[nodiscard]] virtual RenderProfile profile() const noexcept = 0;
};

} // namespace nexus::rendering
