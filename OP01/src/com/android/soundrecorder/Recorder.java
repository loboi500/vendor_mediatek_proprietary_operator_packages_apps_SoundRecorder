/*
* Copyright (C) 2014 MediaTek Inc.
* Modification based on code covered by the mentioned copyright
* and/or permission notice(s).
*/
/*
 * Copyright (C) 2011 The Android Open Source Project
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

package com.android.soundrecorder;

import android.content.Context;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.SystemClock;
import android.os.storage.StorageManager;

import com.android.soundrecorder.RecordParamsSetting.RecordParams;

import com.mediatek.soundrecorder.ext.ExtensionHelper;
import com.mediatek.soundrecorder.ext.IRecordingTimeCalculationExt;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Recorder class to wrapper the MediaRecorder.
 */
public class Recorder implements MediaRecorder.OnErrorListener {

    public static final String RECORD_FOLDER = "Recording";
    public static final String SAMPLE_SUFFIX = ".tmp";

    private static final String TAG = "SR/Recorder";
    private static final String SAMPLE_PREFIX = "record";

    // M: the three below are all in millseconds
    private long mSampleLength = 0;
    private long mSampleStart = 0;
    private long mPreviousTime = 0;

    private File mSampleFile = null;
    private final StorageManager mStorageManager;
    private MediaRecorder mRecorder = null;
    private RecorderListener mListener = null;
    private int mCurrentState = SoundRecorderService.STATE_IDLE;

    // M: used for audio pre-process
    private boolean[] mSelectEffect = null;

    /**
     * Recorder Callback.
     */
    public interface RecorderListener {
        /**
         * State Callback.
         *
         * @param recorder the Recorder instance
         * @param stateCode the status code
         */
        void onStateChanged(Recorder recorder, int stateCode);

        /**
         * Error Callback.
         *
         * @param recorder the Recorder instance
         * @param errorCode the error code
         */
        void onError(Recorder recorder, int errorCode);
    }

    @Override
    public void onError(MediaRecorder recorder, int errorType, int extraCode) {
        LogUtils.i(TAG, "<onError> errorType = " + errorType + "; extraCode = " + extraCode);
        stopRecording();
        mListener.onError(this, ErrorHandle.ERROR_RECORDING_FAILED);
    }

    /**
     * M: Constructor of Recorder.
     * @param storageManager instance of StorageManager
     * @param listener recording callback
     */
    public Recorder(StorageManager storageManager, RecorderListener listener) {
        mStorageManager = storageManager;
        mListener = listener;
    }

    /**
     * M: get the current amplitude of MediaRecorder, used by VUMeter.
     * @return the amplitude value
     */
    public int getMaxAmplitude() {
        synchronized (this) {
            if (null == mRecorder) {
                return 0;
            }
            return (SoundRecorderService.STATE_RECORDING != mCurrentState) ? 0 : mRecorder
                    .getMaxAmplitude();
        }
    }

    public long getSampleLength() {
        return mSampleLength;
    }

    /**
     * M: get how long time we has recorded.
     * @return the record length, in millseconds
     */
    public long getCurrentProgress() {
        if (SoundRecorderService.STATE_RECORDING == mCurrentState) {
            long current = SystemClock.elapsedRealtime();
            return (long) (current - mSampleStart + mPreviousTime);
        } else if (SoundRecorderService.STATE_PAUSE_RECORDING == mCurrentState) {
            return (long) (mPreviousTime);
        }
        return 0;
    }

    /**
     * M: set Recorder to initial state.
     *
     * @return reset result
     */
    public boolean reset() {
        /** M:modified for stop recording failed. @{ */
        boolean result = true;
        synchronized (this) {
            if (null != mRecorder) {
                try {
                    /**M: To avoid NE while mCurrentState is not prepared.@{**/
                    if (mCurrentState == SoundRecorderService.STATE_PAUSE_RECORDING
                            || mCurrentState == SoundRecorderService.STATE_RECORDING) {
                        mRecorder.stop();
                    }
                    /**@}**/
                } catch (RuntimeException exception) {
                    exception.printStackTrace();
                    result = false;
                    LogUtils.e(TAG,
                            "<stopRecording> recorder illegalstate exception in recorder.stop()");
                } finally {
                    mRecorder.reset();
                    mRecorder.release();
                    mRecorder = null;
                }
            }
        }
        mPreviousTime = 0;
        mSampleLength = 0;
        mSampleStart = 0;
        /**
         * M: add for some error case for example pause or goon recording
         * failed. @{
         */
        mCurrentState = SoundRecorderService.STATE_IDLE;
        /** @} */
        return result;
    }

    /**
     * Start recording.
     *
     * @param context the context
     * @param params the recording parameters.
     * @param fileSizeLimit the file size limitation
     * @param fd the file descriptor of the recording file
     * @return the record result
     */
    public boolean startRecording(Context context, RecordParams params, int fileSizeLimit,
            FileDescriptor fd) {
        LogUtils.i(TAG, "<startRecording> begin");
        if (SoundRecorderService.STATE_IDLE != mCurrentState) {
            return false;
        }
        reset();

        if (!initAndStartMediaRecorder(context, params, fileSizeLimit, fd)) {
            LogUtils.i(TAG, "<startRecording> initAndStartMediaRecorder return false");
            return false;
        }
        mSampleStart = SystemClock.elapsedRealtime();
        setState(SoundRecorderService.STATE_RECORDING);
        LogUtils.i(TAG, "<startRecording> end");
        return true;
    }

    /**
     * Pause the recording.
     *
     * @return the pause result
     */
    public boolean pauseRecording() {
        if ((SoundRecorderService.STATE_RECORDING != mCurrentState) || (null == mRecorder)) {
            mListener.onError(this, SoundRecorderService.STATE_ERROR_CODE);
            return false;
        }
        try {
            mRecorder.pause();
        } catch (RuntimeException e) {
            LogUtils.e(TAG, "<pauseRecording> IllegalArgumentException");
            handleException(e);
            mListener.onError(this, ErrorHandle.ERROR_RECORDING_FAILED);
            return false;
        }
        mPreviousTime += SystemClock.elapsedRealtime() - mSampleStart;
        setState(SoundRecorderService.STATE_PAUSE_RECORDING);
        return true;
    }

    /**
     * Resume the recording.
     *
     * @return the resume result
     */
    public boolean goonRecording() {
        if ((SoundRecorderService.STATE_PAUSE_RECORDING != mCurrentState) || (null == mRecorder)) {
            return false;
        }
        // Handle RuntimeException if the recording couldn't start
        try {
            mRecorder.resume();
        } catch (RuntimeException exception) {
            LogUtils.e(TAG, "<goOnRecording> IllegalArgumentException");
            exception.printStackTrace();
            mRecorder.reset();
            mRecorder.release();
            mRecorder = null;
            mListener.onError(this, ErrorHandle.ERROR_RECORDING_FAILED);
            return false;
        }

        mSampleStart = SystemClock.elapsedRealtime();
        setState(SoundRecorderService.STATE_RECORDING);
        return true;
    }

    /**
     * Stop recording.
     *
     * @return the stop result
     */
    public boolean stopRecording() {
        LogUtils.i(TAG, "<stopRecording> start");
        if (((SoundRecorderService.STATE_PAUSE_RECORDING != mCurrentState) &&
             (SoundRecorderService.STATE_RECORDING != mCurrentState)) || (null == mRecorder)) {
            LogUtils.i(TAG, "<stopRecording> end 1");
            mListener.onError(this, SoundRecorderService.STATE_ERROR_CODE);
            return false;
        }
        boolean isAdd = (SoundRecorderService.STATE_RECORDING == mCurrentState) ? true : false;
        synchronized (this) {
            try {
                if (mCurrentState != SoundRecorderService.STATE_IDLE) {
                    mRecorder.stop();
                }
            } catch (RuntimeException exception) {
                /** M:modified for stop recording failed. @{ */
                handleException(exception);
                mListener.onError(this, ErrorHandle.ERROR_RECORDING_FAILED);
                LogUtils.e(TAG,
                        "<stopRecording> recorder illegalstate exception in recorder.stop()");
            } finally {
                if (null != mRecorder) {
                    mRecorder.reset();
                    mRecorder.release();
                    mRecorder = null;
                }
                if (isAdd) {
                    mPreviousTime += SystemClock.elapsedRealtime() - mSampleStart;
                }
                mSampleLength = mPreviousTime;
                LogUtils.i(TAG, "<stopRecording> mSampleLength in ms is " + mPreviousTime);
                LogUtils.i(TAG, "<stopRecording> mSampleLength in s is = " + mSampleLength);
                setState(SoundRecorderService.STATE_IDLE);
            }
            /** @} */
        }
        LogUtils.i(TAG, "<stopRecording> end 2");
        return true;
    }

    public int getCurrentState() {
        return mCurrentState;
    }

    private void setState(int state) {
        mCurrentState = state;
        mListener.onStateChanged(this, state);
    }

    private boolean initAndStartMediaRecorder(
            Context context, RecordParams recordParams, int fileSizeLimit,
            FileDescriptor fd) {
        LogUtils.i(TAG, "<initAndStartMediaRecorder> start");
        try {
            /**
             * M:Changed to catch the IllegalStateException and NullPointerException.
             * And the IllegalStateException will be caught and handled in RuntimeException
             * .@{
             */
            mSelectEffect = recordParams.mAudioEffect;
            IRecordingTimeCalculationExt ext =
                    ExtensionHelper.getRecordingTimeCalculationExt(context);
            mRecorder = ext.getMediaRecorder();
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setOutputFormat(recordParams.mOutputFormat);
            mRecorder.setOutputFile(fd);
            mRecorder.setAudioEncoder(recordParams.mAudioEncoder);
            mRecorder.setAudioChannels(recordParams.mAudioChannels);
            mRecorder.setAudioEncodingBitRate(recordParams.mAudioEncodingBitRate);
            mRecorder.setAudioSamplingRate(recordParams.mAudioSamplingRate);
            if (fileSizeLimit > 0) {
                mRecorder.setMaxFileSize(fileSizeLimit);
            }
            mRecorder.setOnErrorListener(this);
            /**@}**/
            mRecorder.prepare();
            mRecorder.start();
        } catch (IOException exception) {
            LogUtils.e(TAG, "<initAndStartMediaRecorder> IO exception");
            // M:Add for when error ,the tmp file should been delete.
            handleException(exception);
            mListener.onError(this, ErrorHandle.ERROR_RECORDING_FAILED);
            return false;
        } catch (NullPointerException exception) {
            /**
             * M: used to catch the null pointer exception in ALPS01226113,
             * and never show any toast or dialog to end user. Because this
             * error just happened when fast tapping the file list button
             * after tapping record button(which triggered by tapping the
             * recording button in audio play back view).@{
             */
            mListener.onError(this, ErrorHandle.ERROR_RECORDING_FAILED);
            handleException(exception);
            return false;
        } catch (IllegalStateException exception) {
            LogUtils.e(TAG, "<initAndStartMediaRecorder> RuntimeException");
            // M:Add for when error ,the tmp file should been delete.
            handleException(exception);
            mListener.onError(this, ErrorHandle.ERROR_RECORDER_OCCUPIED);
            return false;
        } catch (RuntimeException exception) {
            LogUtils.e(TAG, "<initAndStartMediaRecorder> RuntimeException");
            handleException(exception);
            mListener.onError(this, ErrorHandle.ERROR_RECORDING_FAILED);
            return false;
        }
        LogUtils.i(TAG, "<initAndStartMediaRecorder> end");
        return true;
    }

    /**
     * M: Handle Exception when call the function of MediaRecorder.
     *
     * @param exception the exception info
     */
    public void handleException(Exception exception) {
        LogUtils.i(TAG, "<handleException> the exception is: " + exception);
        exception.printStackTrace();
        try {
            if (mRecorder != null) {
                mRecorder.reset();
            }
        } catch (IllegalStateException ex) {
            LogUtils.i(TAG, "Excpetion " + ex);
        } finally {
            if (mRecorder != null) {
                mRecorder.release();
                mRecorder = null;
            }
        }
    }
}
