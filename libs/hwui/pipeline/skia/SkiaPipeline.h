/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#pragma once

#include "renderthread/CanvasContext.h"
#include "FrameBuilder.h"
#include "renderthread/IRenderPipeline.h"
#include <SkSurface.h>

namespace android {
namespace uirenderer {
namespace skiapipeline {

class SkiaPipeline : public renderthread::IRenderPipeline {
public:
    SkiaPipeline(renderthread::RenderThread& thread);
    virtual ~SkiaPipeline() {}

    TaskManager* getTaskManager() override;

    void onDestroyHardwareResources() override;

    void renderLayers(const FrameBuilder::LightGeometry& lightGeometry,
            LayerUpdateQueue* layerUpdateQueue, bool opaque,
            const BakedOpRenderer::LightInfo& lightInfo) override;

    bool createOrUpdateLayer(RenderNode* node,
            const DamageAccumulator& damageAccumulator) override;

    void renderFrame(const LayerUpdateQueue& layers, const SkRect& clip,
            const std::vector< sp<RenderNode> >& nodes, bool opaque, const Rect &contentDrawBounds,
            sk_sp<SkSurface> surface);

    static void destroyLayer(RenderNode* node);

    static void prepareToDraw(const renderthread::RenderThread& thread, Bitmap* bitmap);

    static void renderLayersImpl(const LayerUpdateQueue& layers, bool opaque);

    static bool skpCaptureEnabled() { return false; }

    static float getLightRadius() {
        if (CC_UNLIKELY(Properties::overrideLightRadius > 0)) {
            return Properties::overrideLightRadius;
        }
        return mLightRadius;
    }

    static uint8_t getAmbientShadowAlpha() {
        if (CC_UNLIKELY(Properties::overrideAmbientShadowStrength >= 0)) {
            return Properties::overrideAmbientShadowStrength;
        }
        return mAmbientShadowAlpha;
    }

    static uint8_t getSpotShadowAlpha() {
        if (CC_UNLIKELY(Properties::overrideSpotShadowStrength >= 0)) {
            return Properties::overrideSpotShadowStrength;
        }
        return mSpotShadowAlpha;
    }

    static Vector3 getLightCenter() {
        if (CC_UNLIKELY(Properties::overrideLightPosY > 0 || Properties::overrideLightPosZ > 0)) {
            Vector3 adjustedLightCenter = mLightCenter;
            if (CC_UNLIKELY(Properties::overrideLightPosY > 0)) {
                // negated since this shifts up
                adjustedLightCenter.y = - Properties::overrideLightPosY;
            }
            if (CC_UNLIKELY(Properties::overrideLightPosZ > 0)) {
                adjustedLightCenter.z = Properties::overrideLightPosZ;
            }
            return adjustedLightCenter;
        }
        return mLightCenter;
    }

    static void updateLighting(const FrameBuilder::LightGeometry& lightGeometry,
            const BakedOpRenderer::LightInfo& lightInfo) {
        mLightRadius = lightGeometry.radius;
        mAmbientShadowAlpha = lightInfo.ambientShadowAlpha;
        mSpotShadowAlpha = lightInfo.spotShadowAlpha;
        mLightCenter = lightGeometry.center;
    }
protected:
    renderthread::RenderThread& mRenderThread;

private:
    TaskManager mTaskManager;
    static float mLightRadius;
    static uint8_t mAmbientShadowAlpha;
    static uint8_t mSpotShadowAlpha;
    static Vector3 mLightCenter;
};

} /* namespace skiapipeline */
} /* namespace uirenderer */
} /* namespace android */