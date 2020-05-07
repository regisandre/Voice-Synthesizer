package be.regisandre.synth;

import java.io.File;

import javax.swing.filechooser.FileSystemView;

public class Main {
	static TextToSpeech tts = new TextToSpeech();
	static SoundPlayer sp = new SoundPlayer();

	public static void main(String[] args) throws Exception {
		tts.speak("Hi I'm ", "cmu-rms-hsmm", 1.0f);
		tts.save(tts.getTTS(), FileSystemView.getFileSystemView().getHomeDirectory() + "\\sound\\", 5, false);

		//sp.getMixers();
		sp.play(new File(FileSystemView.getFileSystemView().getHomeDirectory() + "\\sound\\1.fcs"));
	}
}
