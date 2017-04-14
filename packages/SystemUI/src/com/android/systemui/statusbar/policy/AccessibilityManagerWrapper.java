/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.android.systemui.statusbar.policy;

import android.content.Context;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityManager.AccessibilityServicesStateChangeListener;

/**
 * For mocking because AccessibilyManager is final for some reason...
 */
public class AccessibilityManagerWrapper implements
        CallbackController<AccessibilityServicesStateChangeListener> {

    private final AccessibilityManager mAccessibilityManager;

    public AccessibilityManagerWrapper(Context context) {
        mAccessibilityManager = context.getSystemService(AccessibilityManager.class);
    }

    @Override
    public void addCallback(AccessibilityServicesStateChangeListener listener) {
        mAccessibilityManager.addAccessibilityServicesStateChangeListener(listener);
    }

    @Override
    public void removeCallback(AccessibilityServicesStateChangeListener listener) {
        mAccessibilityManager.removeAccessibilityServicesStateChangeListener(listener);
    }
}