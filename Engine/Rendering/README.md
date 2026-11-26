# NXRHI — NetherNexus Rendering Hardware Interface

NXRHI is NetherNexus Engine's backend-independent graphics boundary. It is an original engine interface, not a graphics API or GPU driver.

## Rendering profiles

| Profile | Target platforms | Initial intent |
|---|---|---|
| Compatibility | Desktop, Android, Web | Lowest baseline and broadest device/browser reach |
| Forward | Desktop, Android | Balanced rendering quality and performance |
| Advanced | Desktop, Android | Higher-end rendering features where hardware permits |

Profile feature definitions are engine targets, not guarantees about a particular GPU. A real device backend must check hardware capabilities and disable or downgrade unsupported features.

## Backend boundary

RenderDevice.h defines the initial backend-independent device contract. RenderProfile.h defines platform eligibility and the initial feature tiers. Neither header includes OpenGL, Vulkan, DirectX, Metal, or WebGPU headers.

Planned backend adapters:
- Vulkan — desktop and Android where supported.
- Direct3D 12 — supported Windows desktop systems.
- Metal — Apple platforms if added to the target matrix.
- WebGPU — browsers that expose a compatible implementation.

These are planned integration paths, not implemented backends. WebGPU availability varies by browser and device. Compatibility will need capability detection and a graceful unsupported-device path.

## Current milestone: NXRHI foundation

Implemented:
- Backend-independent profile and platform types.
- Compatibility / Forward / Advanced support policy.
- Initial profile feature targets.
- Abstract render-device lifecycle contract.
- C++ tests for the profile/platform matrix.

Not implemented:
- GPU discovery or adapter selection.
- Real graphics API backends.
- Swapchains, command buffers, resource allocation or shader compilation.
- 2D or 3D rendering through NXRHI.

Next: backend selection and capability model, then the first real backend. OpenGL is not a required dependency in this architecture.
