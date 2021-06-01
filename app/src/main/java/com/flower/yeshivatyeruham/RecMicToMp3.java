/*
 * Copyright (c) 2011-2012 Yuichi Hirano
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.flower.yeshivatyeruham;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidParameterException;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.media.audiofx.AcousticEchoCanceler;
import android.media.audiofx.AutomaticGainControl;
import android.media.audiofx.NoiseSuppressor;
import android.os.Build;
import android.os.Handler;
import android.os.Process;
import android.util.Log;

import static com.flower.yeshivatyeruham.DataClass.scanFile;
//import static com.flower.yeshivatyeruham.RecordingActivity.curVol;
import static com.flower.yeshivatyeruham.RecordingActivity.amplitude;

/**
 * �}�C�N����擾����������MP3�ɕۑ�����
 *
 * �ʃX���b�h�Ń}�C�N����̘^���AMP3�ւ̕ϊ����s��
 */
public class RecMicToMp3 {

	static {
		System.loadLibrary("mp3lame");
	}

	/**
	 * MP3�t�@�C����ۑ�����t�@�C���p�X
	 */
	private String mFilePath;

	/**
	 * �T���v�����O���[�g
	 */
	private int mSampleRate;

	/**
	 * �^������
	 */
	private boolean mIsRecording = false, isPause = false;

	private short[][] allData;

    Thread recThrd;

	/**
	 * �^���̏�ԕω���ʒm����n���h��
	 *
	 * @see RecMicToMp3#MSG_REC_STARTED
	 * @see RecMicToMp3#MSG_REC_STOPPED
	 * @see RecMicToMp3#MSG_ERROR_GET_MIN_BUFFERSIZE
	 * @see RecMicToMp3#MSG_ERROR_CREATE_FILE
	 * @see RecMicToMp3#MSG_ERROR_REC_START
	 * @see RecMicToMp3#MSG_ERROR_AUDIO_RECORD
	 * @see RecMicToMp3#MSG_ERROR_AUDIO_ENCODE
	 * @see RecMicToMp3#MSG_ERROR_WRITE_FILE
	 * @see RecMicToMp3#MSG_ERROR_CLOSE_FILE
	 */
	private Handler mHandler;
//	private AudioRecord audioRecord = null;


	/**
	 * �^�����J�n����
	 */
	public static final int MSG_REC_STARTED = 0;

	/**
	 * �^�����I������
	 */
	public static final int MSG_REC_STOPPED = 1;

	/**
	 * �o�b�t�@�T�C�Y���擾�ł��Ȃ��B�T���v�����O���[�g���̐ݒ��[�����T�|�[�g���Ă��Ȃ��\��������B
	 */
	public static final int MSG_ERROR_GET_MIN_BUFFERSIZE = 2;

	/**
	 * �t�@�C���������ł��Ȃ�
	 */
	public static final int MSG_ERROR_CREATE_FILE = 3;

	/**
	 * �^���̊J�n�Ɏ��s����
	 */
	public static final int MSG_ERROR_REC_START = 4;

	/**
	 * �^�����ł��Ȃ��B�^�����J�n��̂ݔ��s����B
	 */
	public static final int MSG_ERROR_AUDIO_RECORD = 5;

	/**
	 * �G���R�[�h�Ɏ��s�����B�^�����J�n��̂ݔ��s����B
	 */
	public static final int MSG_ERROR_AUDIO_ENCODE = 6;

	/**
	 * �t�@�C���̏����o���Ɏ��s�����B�^�����J�n��̂ݔ��s����B
	 */
	public static final int MSG_ERROR_WRITE_FILE = 7;

	/**
	 * �t�@�C���̃N���[�Y�Ɏ��s�����B�^�����J�n��̂ݔ��s����B
	 */
	public static final int MSG_ERROR_CLOSE_FILE = 8;

	/**
	 * �R���X�g���N�^
	 *
	 * @param filePath
	 *            �ۑ�����t�@�C���p�X
	 * @param sampleRate
	 *            �^������T���v�����O���[�g�iHz�j
	 */
	public RecMicToMp3(String filePath, int sampleRate) {
		if (sampleRate <= 0) {
			throw new InvalidParameterException(
					"Invalid sample rate specified.");
		}
		this.mFilePath = filePath;
		this.mSampleRate = sampleRate;
	}

	public void pause()
	{
		isPause = true;
	}

	public void reStart()
	{
		isPause = false;
	}

	public short[] getBuffer(short[][] allData)
	{
		int length = 0, count = 0;
		for(short myLength[] : allData)
		{
			length += myLength.length;
		}
		short[] newData = new short[length];
		for (int i = 0; i < allData.length; i++)
			for(int j = 0; j < allData[i].length; j++)
			{
				newData[count] = allData[i][j];
				count++;
			}
		return newData;
	}

	/**
	 * �^�����J�n����
	 */
	public void start() {
		// �^�����̏ꍇ�͉������Ȃ�
		if (mIsRecording) {
			return;
		}

		final int minBufferSize = AudioRecord.getMinBufferSize(
				mSampleRate, AudioFormat.CHANNEL_IN_MONO,
				AudioFormat.ENCODING_PCM_16BIT);
		final AudioRecord audioMp3Record  = new AudioRecord(
				MediaRecorder.AudioSource.MIC, mSampleRate,
				AudioFormat.CHANNEL_IN_MONO,
				AudioFormat.ENCODING_PCM_16BIT, minBufferSize * 2);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			//4.1+
			if (AcousticEchoCanceler.isAvailable()){
				AcousticEchoCanceler.create(audioMp3Record.getAudioSessionId());
				Log.d("test", "aeg work");

			}
			if (NoiseSuppressor.isAvailable()){
				NoiseSuppressor.create(audioMp3Record.getAudioSessionId());
				Log.d("test", "ns work");
			}
			if (AutomaticGainControl.isAvailable()){
				AutomaticGainControl.create(audioMp3Record.getAudioSessionId());
				Log.d("test", "agc work");
			}
		}

		// �^����ʃX���b�h�ŊJ�n����
		recThrd = new Thread() {
			@Override
			public void run() {
				Process
						.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO);
				// �Œ���̃o�b�t�@�T�C�Y
//				final int minBufferSize = AudioRecord.getMinBufferSize(
//						mSampleRate, AudioFormat.CHANNEL_IN_MONO,
//						AudioFormat.ENCODING_PCM_16BIT);
				// �o�b�t�@�T�C�Y���擾�ł��Ȃ��B�T	���v�����O���[�g���̐ݒ��[�����T�|�[�g���Ă��Ȃ��\��������B
				if (minBufferSize < 0) {
					if (mHandler != null) {
						mHandler.sendEmptyMessage(MSG_ERROR_GET_MIN_BUFFERSIZE);
					}
					return;
				}
				// getMinBufferSize�Ŏ擾�����l�̏ꍇ
				// "W/AudioFlinger(75): RecordThread: buffer overflow"����������悤�ł��邽�߁A�����傫�߂̒l�ɂ��Ă���
//				if (1==2 && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
//					//3.0+
//					audioRecord = new AudioRecord(
//                            MediaRecorder.AudioSource.VOICE_RECOGNITION, mSampleRate,
//                            AudioFormat.CHANNEL_IN_MONO,
//                            AudioFormat.ENCODING_PCM_16BIT, minBufferSize * 2);
//				}
//				else

//				AudioManager audioManager = null;
//				audioManager.setMode(AudioManager.MODE_IN_CALL);
//				audioManager.setParameters("noise_suppression=on");


				//allData = new short[1][mSampleRate * (16 / 8) * 1 * 5];

				// PCM buffer size (5sec)
				short[] buffer = new short[mSampleRate * (16 / 8) * 1 * 5]; // SampleRate[Hz] * 16bit * Mono * 5sec
				byte[] mp3buffer = new byte[(int) (7200 + buffer.length * 2 * 1.25)];

				FileOutputStream output = null;
				try {
					output = new FileOutputStream(new File(mFilePath));
					scanFile(new File(mFilePath));
				} catch (FileNotFoundException e) {
					// �t�@�C���������ł��Ȃ�
					if (mHandler != null) {
						mHandler.sendEmptyMessage(MSG_ERROR_CREATE_FILE);
					}
					return;
				}

				// Lame init
				SimpleLame.init(mSampleRate, 1, mSampleRate, 32);

				mIsRecording = true; // �^���̊J�n�t���O�𗧂Ă�
				try {
					try {
						audioMp3Record.startRecording(); // �^�����J�n����
					} catch (IllegalStateException e) {
						// �^���̊J�n�Ɏ��s����
						if (mHandler != null) {
							mHandler.sendEmptyMessage(MSG_ERROR_REC_START);
						}
						return;
					}

					try {
						// �^�����J�n����
						if (mHandler != null) {
							mHandler.sendEmptyMessage(MSG_REC_STARTED);
						}

						int readSize = 0;
						while (mIsRecording) {

							if (isPause) continue;

							readSize = audioMp3Record.read(buffer, 0, minBufferSize);

							// Calculate volume
//							Message msg = new Message();
//							msg.arg1 =
							calVolume(buffer, readSize);
//							msg.what = MSG_REC_VOLUME;
//							// Store volume info into handler
//							if (mHandler != null) {
//								mHandler.sendMessage(msg);
//							}

							if (readSize < 0) {
								// �^�����ł��Ȃ�
								if (mHandler != null) {
									mHandler.sendEmptyMessage(MSG_ERROR_AUDIO_RECORD);
								}
								continue;
							}
							// �f�[�^���ǂݍ��߂Ȃ������ꍇ�͉������Ȃ�
							else if (readSize == 0) {
								;
							}
							// �f�[�^�������Ă���ꍇ
							else {
								int encResult = SimpleLame.encode(buffer,
										buffer, readSize, mp3buffer);
								if (encResult < 0) {
									// �G���R�[�h�Ɏ��s����
									if (mHandler != null) {
										mHandler.sendEmptyMessage(MSG_ERROR_AUDIO_ENCODE);
									}
									continue;
								}
								if (encResult != 0) {
									try {
										output.write(mp3buffer, 0, encResult);
									} catch (IOException e) {
										// �t�@�C���̏����o���Ɏ��s����
										if (mHandler != null) {
											mHandler.sendEmptyMessage(MSG_ERROR_WRITE_FILE);
										}
										continue;
									}
								}
							}
						}

						int flushResult = SimpleLame.flush(mp3buffer);
						if (flushResult < 0) {
							// �G���R�[�h�Ɏ��s����
							if (mHandler != null) {
								mHandler.sendEmptyMessage(MSG_ERROR_AUDIO_ENCODE);
							}
						}
						if (flushResult != 0) {
							try {
								output.write(mp3buffer, 0, flushResult);
							} catch (IOException e) {
								// �t�@�C���̏����o���Ɏ��s����
								if (mHandler != null) {
									mHandler.sendEmptyMessage(MSG_ERROR_WRITE_FILE);
								}
							}
						}

						try {
							output.close();
						} catch (IOException e) {
							// �t�@�C���̃N���[�Y�Ɏ��s����
							if (mHandler != null) {
								mHandler.sendEmptyMessage(MSG_ERROR_CLOSE_FILE);
							}
						}
					} finally {
						audioMp3Record.stop(); // �^�����~����
						audioMp3Record.release();
					}
				} finally {
					SimpleLame.close();
					mIsRecording = false; // �^���̊J�n�t���O��������
				}

				// �^�����I������
				if (mHandler != null) {
					mHandler.sendEmptyMessage(MSG_REC_STOPPED);
				}
			}
		};
		recThrd.start();
	}

	/**
	 * �^�����~����
	 */
	public void stop() {
		mIsRecording = false;
	}

	/**
	 * �^���������擾����
	 *
	 * @return true�̏ꍇ�͘^�����A����ȊO��false
	 */
	public boolean isRecording() {
		return mIsRecording;
	}

	/**
	 * �^���̏�ԕω���ʒm����n���h����ݒ肷��
	 *
	 * @param handler
	 *            �^���̏�ԕω���ʒm����n���h��
	 *
	 * @see RecMicToMp3#MSG_REC_STARTED
	 * @see RecMicToMp3#MSG_REC_STOPPED
	 * @see RecMicToMp3#MSG_ERROR_GET_MIN_BUFFERSIZE
	 * @see RecMicToMp3#MSG_ERROR_CREATE_FILE
	 * @see RecMicToMp3#MSG_ERROR_REC_START
	 * @see RecMicToMp3#MSG_ERROR_AUDIO_RECORD
	 * @see RecMicToMp3#MSG_ERROR_AUDIO_ENCODE
	 * @see RecMicToMp3#MSG_ERROR_WRITE_FILE
	 * @see RecMicToMp3#MSG_ERROR_CLOSE_FILE
	 */
	public void setHandle(Handler handler) {
		this.mHandler = handler;
	}

	private void calVolume(short[] buffer, int readSize) {
		int volume = 0;
		for (int i = 0; i < buffer.length; i++) {
			volume += buffer[i] * buffer[i];
		}
		amplitude = (volume / readSize);
//		curVol =  (int) (Math.abs((int)(volume /(float)readSize)/10000) >> 1);
	}
}
