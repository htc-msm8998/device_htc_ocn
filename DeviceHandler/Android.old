#
# Copyright (C) 2014 SlimRoms Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_SRC_FILES := $(call all-java-files-under, src)
LOCAL_CERTIFICATE := platform
LOCAL_PACKAGE_NAME := DeviceHandler
LOCAL_DEX_PREOPT := false

## Slim framework
LOCAL_JAVA_LIBRARIES += \
        org.slim.framework

LOCAL_STATIC_JAVA_LIBRARIES := \
    android-support-v4 \
    android-support-v13 \
    android-support-v7-preference \
    android-support-v7-appcompat \
    android-support-v14-preference \
    android-support-v7-recyclerview \
    jsr305 \
    slim-preference \
    color-picker

LOCAL_RESOURCE_DIR := \
    $(LOCAL_PATH)/res \
    frameworks/support/v7/preference/res \
    frameworks/support/v14/preference/res \
    frameworks/support/v7/appcompat/res \
    frameworks/support/v7/recyclerview/res \
    frameworks/slim/preference/res \
    frameworks/opt/color-picker/cpl/src/main/res


LOCAL_AAPT_FLAGS := --auto-add-overlay \
    --extra-packages android.support.v7.preference:android.support.v14.preference:android.support.v7.appcompat:android.support.v7.recyclerview \
    --extra-packages org.slim.preference:com.enrico.colorpicker

LOCAL_PROGUARD_ENABLED:= disabled

include frameworks/base/packages/SettingsLib/common.mk

include $(BUILD_PACKAGE)
