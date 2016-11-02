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
package com.android.server.notification;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.PendingIntent;
import android.content.Context;
import android.os.UserHandle;
import android.service.notification.StatusBarNotification;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.SmallTest;

import java.util.ArrayList;
import java.util.List;

@SmallTest
@RunWith(AndroidJUnit4.class)
public class GroupHelperTest {
    private @Mock GroupHelper.Callback mCallback;

    private GroupHelper mGroupHelper;

    private Context getContext() {
        return InstrumentationRegistry.getTargetContext();
    }

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        mGroupHelper = new GroupHelper(mCallback);
    }

    private StatusBarNotification getSbn(String pkg, int id, String tag,
            UserHandle user, String groupKey) {
        Notification.Builder nb = new Notification.Builder(getContext())
                .setContentTitle("A")
                .setWhen(1205);
        if (groupKey != null) {
            nb.setGroup(groupKey);
        }
        return new StatusBarNotification(pkg, pkg, id, tag, 0, 0, 0, nb.build(), user);
    }

    private StatusBarNotification getSbn(String pkg, int id, String tag,
            UserHandle user) {
        return getSbn(pkg, id, tag, user, null);
    }

    @Test
    public void testNoGroup_postingUnderLimit() throws Exception {
        final String pkg = "package";
        for (int i = 0; i < GroupHelper.AUTOGROUP_AT_COUNT - 1; i++) {
            mGroupHelper.onNotificationPosted(getSbn(pkg, i, String.valueOf(i), UserHandle.SYSTEM));
        }
        verify(mCallback, never()).addAutoGroupSummary(
                eq(UserHandle.USER_SYSTEM), eq(pkg), anyString());
        verify(mCallback, never()).addAutoGroup(anyString());
        verify(mCallback, never()).removeAutoGroup(anyString());
        verify(mCallback, never()).removeAutoGroupSummary(anyInt(), anyString());
    }

    @Test
    public void testNoGroup_multiPackage() throws Exception {
        final String pkg = "package";
        final String pkg2 = "package2";
        for (int i = 0; i < GroupHelper.AUTOGROUP_AT_COUNT - 1; i++) {
            mGroupHelper.onNotificationPosted(getSbn(pkg, i, String.valueOf(i), UserHandle.SYSTEM));
        }
        mGroupHelper.onNotificationPosted(
                getSbn(pkg2, GroupHelper.AUTOGROUP_AT_COUNT, "four", UserHandle.SYSTEM));
        verify(mCallback, never()).addAutoGroupSummary(
                eq(UserHandle.USER_SYSTEM), eq(pkg), anyString());
        verify(mCallback, never()).addAutoGroup(anyString());
        verify(mCallback, never()).removeAutoGroup(anyString());
        verify(mCallback, never()).removeAutoGroupSummary(anyInt(), anyString());
    }

    @Test
    public void testNoGroup_multiUser() throws Exception {
        final String pkg = "package";
        for (int i = 0; i < GroupHelper.AUTOGROUP_AT_COUNT - 1; i++) {
            mGroupHelper.onNotificationPosted(getSbn(pkg, i, String.valueOf(i), UserHandle.SYSTEM));
        }
        mGroupHelper.onNotificationPosted(
                getSbn(pkg, GroupHelper.AUTOGROUP_AT_COUNT, "four", UserHandle.ALL));
        verify(mCallback, never()).addAutoGroupSummary(anyInt(), eq(pkg), anyString());
        verify(mCallback, never()).addAutoGroup(anyString());
        verify(mCallback, never()).removeAutoGroup(anyString());
        verify(mCallback, never()).removeAutoGroupSummary(anyInt(), anyString());
    }

    @Test
    public void testNoGroup_someAreGrouped() throws Exception {
        final String pkg = "package";
        for (int i = 0; i < GroupHelper.AUTOGROUP_AT_COUNT - 1; i++) {
            mGroupHelper.onNotificationPosted(getSbn(pkg, i, String.valueOf(i), UserHandle.SYSTEM));
        }
        mGroupHelper.onNotificationPosted(
                getSbn(pkg, GroupHelper.AUTOGROUP_AT_COUNT, "four", UserHandle.SYSTEM, "a"));
        verify(mCallback, never()).addAutoGroupSummary(
                eq(UserHandle.USER_SYSTEM), eq(pkg), anyString());
        verify(mCallback, never()).addAutoGroup(anyString());
        verify(mCallback, never()).removeAutoGroup(anyString());
        verify(mCallback, never()).removeAutoGroupSummary(anyInt(), anyString());
    }


    @Test
    public void testPostingOverLimit() throws Exception {
        final String pkg = "package";
        for (int i = 0; i < GroupHelper.AUTOGROUP_AT_COUNT; i++) {
            mGroupHelper.onNotificationPosted(getSbn(pkg, i, String.valueOf(i), UserHandle.SYSTEM));
        }
        verify(mCallback, times(1)).addAutoGroupSummary(anyInt(), eq(pkg), anyString());
        verify(mCallback, times(GroupHelper.AUTOGROUP_AT_COUNT)).addAutoGroup(anyString());
        verify(mCallback, never()).removeAutoGroup(anyString());
        verify(mCallback, never()).removeAutoGroupSummary(anyInt(), anyString());
    }

    @Test
    public void testDropBelowLimitRemoveGroup() throws Exception {
        final String pkg = "package";
        List<StatusBarNotification> posted = new ArrayList<>();
        for (int i = 0; i < GroupHelper.AUTOGROUP_AT_COUNT; i++) {
            final StatusBarNotification sbn = getSbn(pkg, i, String.valueOf(i), UserHandle.SYSTEM);
            posted.add(sbn);
            mGroupHelper.onNotificationPosted(sbn);
        }
        mGroupHelper.onNotificationRemoved(posted.remove(0));
        verify(mCallback, times(1)).addAutoGroupSummary(anyInt(), eq(pkg), anyString());
        verify(mCallback, times(GroupHelper.AUTOGROUP_AT_COUNT)).addAutoGroup(anyString());
        verify(mCallback, times(GroupHelper.AUTOGROUP_AT_COUNT - 1)).removeAutoGroup(anyString());
        verify(mCallback, times(1)).removeAutoGroupSummary(anyInt(), anyString());
    }
}