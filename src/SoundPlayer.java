package be.regisandre.synth;

import java.io.File;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;

public class SoundPlayer {
	public void play(File sound) throws Exception {
		AudioInputStream ais = AudioSystem.getAudioInputStream(sound);
		AudioFormat format = ais.getFormat();
		DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);

		SourceDataLine sourcedataline;

		sourcedataline = (SourceDataLine) AudioSystem.getLine(info);
		sourcedataline.open(format);
		sourcedataline.start();

		int nBytesRead = 0;
		byte[] abData = new byte[524288];
		while (nBytesRead != -1) {
			nBytesRead = ais.read(abData, 0, abData.length);
			if (nBytesRead >= 0) {
				sourcedataline.write(abData, 0, nBytesRead);
			}
		}

		sourcedataline.drain();
		sourcedataline.close();
	}

	public void getMixers() {
		Mixer.Info[] mixerInfo = AudioSystem.getMixerInfo();
		System.out.println("Available mixers:");
		Mixer mixer;
		System.out.println(mixerInfo.length);
		for (int cnt = 0; cnt < mixerInfo.length; cnt++) {
			System.out.println(cnt + " " + mixerInfo[cnt].getName());
			mixer = AudioSystem.getMixer(mixerInfo[cnt]);

			Line.Info[] lineInfos = mixer.getTargetLineInfo();
			if (lineInfos.length >= 1 && lineInfos[0].getLineClass().equals(TargetDataLine.class)) {
				System.out.println("Microphone is supported");
				break;
			}
		}
	}
}