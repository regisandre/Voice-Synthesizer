package be.regisandre.synth;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

import marytts.util.data.audio.MonoAudioInputStream;
import marytts.util.data.audio.StereoAudioInputStream;

public class AudioPlayer extends Thread {
	public static final int MONO = 0;
	public static final int STEREO = 3;
	public static final int LEFT_ONLY = 1;
	public static final int RIGHT_ONLY = 2;
	private AudioInputStream ais;
	private LineListener lineListener;
	private SourceDataLine line;
	private int outputMode;

	private Status status = Status.WAITING;
	private boolean exitRequested = false;
	private float gain = 1.0f;

	public enum Status {
		WAITING, PLAYING;
	}

	public AudioPlayer(File audioFile) throws IOException, UnsupportedAudioFileException {
		this.ais = AudioSystem.getAudioInputStream(audioFile);
	}

	public AudioPlayer(AudioInputStream ais) {
		this.ais = ais;
	}

	public AudioPlayer(File audioFile, LineListener lineListener) throws IOException, UnsupportedAudioFileException {
		this.ais = AudioSystem.getAudioInputStream(audioFile);
		this.lineListener = lineListener;
	}

	public AudioPlayer(AudioInputStream ais, LineListener lineListener) {
		this.ais = ais;
		this.lineListener = lineListener;
	}

	public AudioPlayer(File audioFile, SourceDataLine line, LineListener lineListener)
			throws IOException, UnsupportedAudioFileException {
		this.ais = AudioSystem.getAudioInputStream(audioFile);
		this.line = line;
		this.lineListener = lineListener;
	}

	public AudioPlayer(AudioInputStream ais, SourceDataLine line, LineListener lineListener) {
		this.ais = ais;
		this.line = line;
		this.lineListener = lineListener;
	}

	public AudioPlayer(File audioFile, SourceDataLine line, LineListener lineListener, int outputMode)
			throws IOException, UnsupportedAudioFileException {
		this.ais = AudioSystem.getAudioInputStream(audioFile);
		this.line = line;
		this.lineListener = lineListener;
		this.outputMode = outputMode;
	}

	public AudioPlayer(AudioInputStream ais, SourceDataLine line, LineListener lineListener, int outputMode) {
		this.ais = ais;
		this.line = line;
		this.lineListener = lineListener;
		this.outputMode = outputMode;
	}

	public void setAudio(AudioInputStream audio) {
		if (status == Status.PLAYING) {
			throw new IllegalStateException("Cannot set audio while playing");
		}

		this.ais = audio;
	}

	public void cancel() {
		if (line != null) {
			line.stop();
		}

		exitRequested = true;
	}

	public SourceDataLine getLine() {
		return line;
	}

	public float getGainValue() {
		return gain;
	}

	public void setGain(float fGain) {
		gain = fGain;

		if (line != null && line.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
			((FloatControl) line.getControl(FloatControl.Type.MASTER_GAIN))
					.setValue((float) (20 * Math.log10(fGain <= 0.0 ? 0.0000 : fGain)));
		}
	}

	@Override
	public void run() {
		status = Status.PLAYING;
		AudioFormat audioFormat = ais.getFormat();

		if (audioFormat.getChannels() == 1) {
			if (outputMode != 0) {
				ais = new StereoAudioInputStream(ais, outputMode);
				audioFormat = ais.getFormat();
			}
		} else {
			assert audioFormat.getChannels() == 2 : "Unexpected number of channels: " + audioFormat.getChannels();

			if (outputMode == 0) {
				ais = new MonoAudioInputStream(ais);
			} else if (outputMode == 1 || outputMode == 2) {
				ais = new StereoAudioInputStream(ais, outputMode);
			} else {
				assert outputMode == 3 : "Unexpected output mode: " + outputMode;
			}
		}

		DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);

		try {
			if (line == null) {
				boolean bIsSupportedDirectly = AudioSystem.isLineSupported(info);

				if (!bIsSupportedDirectly) {
					AudioFormat sourceFormat = audioFormat;
					AudioFormat targetFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
							sourceFormat.getSampleRate(), sourceFormat.getSampleSizeInBits(),
							sourceFormat.getChannels(),
							sourceFormat.getChannels() * (sourceFormat.getSampleSizeInBits() / 8),
							sourceFormat.getSampleRate(), sourceFormat.isBigEndian());

					ais = AudioSystem.getAudioInputStream(targetFormat, ais);
					audioFormat = ais.getFormat();
				}

				info = new DataLine.Info(SourceDataLine.class, audioFormat);
				line = (SourceDataLine) AudioSystem.getLine(info);
			}

			if (lineListener != null) {
				line.addLineListener(lineListener);
			}

			line.open(audioFormat);
		} catch (Exception ex) {
			Logger.getLogger(getClass().getName()).log(Level.WARNING, null, ex);
			return;
		}

		line.start();
		setGain(getGainValue());

		int nRead = 0;
		byte[] abData = new byte[65532];

		while ((nRead != -1) && (!exitRequested)) {
			try {
				nRead = ais.read(abData, 0, abData.length);
			} catch (IOException ex) {
				Logger.getLogger(getClass().getName()).log(Level.WARNING, null, ex);
			}

			if (nRead >= 0) {
				line.write(abData, 0, nRead);
			}
		}

		if (!exitRequested) {
			line.drain();
		}

		line.close();
	}

}
