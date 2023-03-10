/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein is
 * confidential and proprietary to MediaTek Inc. and/or its licensors. Without
 * the prior written permission of MediaTek inc. and/or its licensors, any
 * reproduction, modification, use or disclosure of MediaTek Software, and
 * information contained herein, in whole or in part, shall be strictly
 * prohibited.
 *
 * MediaTek Inc. (C) 2016. All rights reserved.
 *
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER
 * ON AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL
 * WARRANTIES, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NONINFRINGEMENT. NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH
 * RESPECT TO THE SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY,
 * INCORPORATED IN, OR SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES
 * TO LOOK ONLY TO SUCH THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO.
 * RECEIVER EXPRESSLY ACKNOWLEDGES THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO
 * OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES CONTAINED IN MEDIATEK
 * SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK SOFTWARE
 * RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S
 * ENTIRE AND CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE
 * RELEASED HEREUNDER WILL BE, AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE
 * MEDIATEK SOFTWARE AT ISSUE, OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE
 * CHARGE PAID BY RECEIVER TO MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek
 * Software") have been modified by MediaTek Inc. All revisions are subject to
 * any receiver's applicable license agreements with MediaTek Inc.
 */
package com.android.soundrecorder;

import android.os.Build;
import android.util.Log;

/**
 * Log helper class.
 */
public class LogUtils {
    private static final boolean SR_XLOG_ENABLED = false;
    private final static boolean ENG = true;
    /**
     * Log helper function.
     *
     * @param tag log tag
     * @param msg log message
     *
     * @return print log result
     */
    public static final int v(String tag, String msg) {
        if (!ENG) {
            return -1;
        }
        int result;
        if (SR_XLOG_ENABLED) {
            result = Log.v("@M_" + tag, msg);
        } else {
            result = Log.v(tag, msg);
        }
        return result;
    }

    /**
     * Log helper function.
     *
     * @param tag log tag
     * @param msg log message
     * @param tr throwable object
     *
     * @return print log result
     */
    public static final int v(String tag, String msg, Throwable tr) {
        if (!ENG) {
            return -1;
        }
        int result;
        if (SR_XLOG_ENABLED) {
            result = Log.v("@M_" + tag, msg, tr);
        } else {
            result = Log.v(tag, msg, tr);
        }
        return result;
    }

    /**
     * Log helper function.
     *
     * @param tag log tag
     * @param msg log message
     *
     * @return print log result
     */
    public static final int d(String tag, String msg) {
        if (!ENG) {
            return -1;
        }
        int result;
        if (SR_XLOG_ENABLED) {
            result = Log.d("@M_" + tag, msg);
        } else {
            result = Log.d(tag, msg);
        }
        return result;
    }

    /**
     * Log helper function.
     *
     * @param tag log tag
     * @param msg log message
     * @param tr throwable object
     *
     * @return print log result
     */
    public static final int d(String tag, String msg, Throwable tr) {
        if (!ENG) {
            return -1;
        }
        int result;
        if (SR_XLOG_ENABLED) {
            result = Log.d("@M_" + tag, msg, tr);
        } else {
            result = Log.d(tag, msg, tr);
        }
        return result;
    }

    /**
     * Log helper function.
     *
     * @param tag log tag
     * @param msg log message
     *
     * @return print log result
     */
    public static final int i(String tag, String msg) {
        if (!ENG) {
            return -1;
        }
        int result;
        if (SR_XLOG_ENABLED) {
            result = Log.i("@M_" + tag, msg);
        } else {
            result = Log.i(tag, msg);
        }
        return result;
    }

    /**
     * Log helper function.
     *
     * @param tag log tag
     * @param msg log message
     * @param tr throwable object
     *
     * @return print log result
     */
    public static final int i(String tag, String msg, Throwable tr) {
        if (!ENG) {
            return -1;
        }
        int result;
        if (SR_XLOG_ENABLED) {
            result = Log.i("@M_" + tag, msg, tr);
        } else {
            result = Log.i(tag, msg, tr);
        }
        return result;
    }

    /**
     * Log helper function.
     *
     * @param tag log tag
     * @param msg log message
     *
     * @return print log result
     */
    public static final int w(String tag, String msg) {
        if (!ENG) {
            return -1;
        }
        int result;
        if (SR_XLOG_ENABLED) {
            result = Log.w("@M_" + tag, msg);
        } else {
            result = Log.w(tag, msg);
        }
        return result;
    }

    /**
     * Log helper function.
     *
     * @param tag log tag
     * @param msg log message
     * @param tr throwable object
     *
     * @return print log result
     */
    public static final int w(String tag, String msg, Throwable tr) {
        if (!ENG) {
            return -1;
        }
        int result;
        if (SR_XLOG_ENABLED) {
            result = Log.w("@M_" + tag, msg, tr);
        } else {
            result = Log.w(tag, msg, tr);
        }
        return result;
    }

    /**
     * Log helper function.
     *
     * @param tag log tag
     * @param msg log message
     *
     * @return print log result
     */
    public static final int e(String tag, String msg) {
        int result;
        if (SR_XLOG_ENABLED) {
            result = Log.e("@M_" + tag, msg);
        } else {
            result = Log.e(tag, msg);
        }
        return result;
    }

    /**
     * Log helper function.
     *
     * @param tag log tag
     * @param msg log message
     * @param tr throwable object
     *
     * @return print log result
     */
    public static final int e(String tag, String msg, Throwable tr) {
        int result;
        if (SR_XLOG_ENABLED) {
            result = Log.e("@M_" + tag, msg, tr);
        } else {
            result = Log.e(tag, msg, tr);
        }
        return result;
    }

}
