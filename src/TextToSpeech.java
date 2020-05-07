package be.regisandre.synth;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.sound.sampled.AudioInputStream;

import marytts.LocalMaryInterface;
import marytts.MaryInterface;
import marytts.exceptions.MaryConfigurationException;
import marytts.exceptions.SynthesisException;
import marytts.modules.synthesis.Voice;
import marytts.signalproc.effects.AudioEffect;
import marytts.signalproc.effects.AudioEffects;
import marytts.util.data.audio.MaryAudioUtils;

public class TextToSpeech {

	private AudioPlayer audioplayer;
	private MaryInterface maryinterface;
	private AudioFilesUtils audiofilesutils = new AudioFilesUtils();
	public String voice;
	public String tts;

	public TextToSpeech() {
		try {
			maryinterface = new LocalMaryInterface();
		} catch (MaryConfigurationException ex) {
			Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
		}
	}

	public void speak(String tts, String voice, float gainValue) {
		stop();

		this.tts = tts;

		if (!voice.isEmpty()) {
			setVoice(voice);
			this.voice = voice;
		} else {
			setVoice(getAvailableVoices().stream().findFirst().get().toString());
			this.voice = getAvailableVoices().stream().findFirst().get().toString();
		}

		System.out.println("Voice [" + maryinterface.getLocale() + "]: " + voice);
		System.out.println("Text to speech: " + tts + "\n");

		try (AudioInputStream audio = maryinterface.generateAudio(tts)) {
			audioplayer = new AudioPlayer(audio);
			audioplayer.setGain(gainValue);

			audioplayer.start();
			audioplayer.join();
		} catch (SynthesisException ex) {
			Logger.getLogger(getClass().getName()).log(Level.WARNING, "Error saying phrase.", ex);
		} catch (IOException ex) {
			Logger.getLogger(getClass().getName()).log(Level.WARNING, "IO Exception", ex);
		} catch (InterruptedException ex) {
			Logger.getLogger(getClass().getName()).log(Level.WARNING, "Interrupted ", ex);
			audioplayer.interrupt();
		}
	}

	public void save(String tts, String outputFolder, int maxNumberFile, boolean forcecrush) throws Exception {
		maryinterface = new LocalMaryInterface();
		maryinterface.setVoice(voice);
		AudioInputStream audio = maryinterface.generateAudio(tts);
		
		File folder = new File(outputFolder);
	    if (!folder.exists()) {
	    	folder.mkdirs();
	    }
		
		if(audiofilesutils.countFiles(new File(outputFolder)) > maxNumberFile-1) {
			audiofilesutils.getLastModifiedFile(outputFolder).delete();
		}
		
		if(forcecrush && audiofilesutils.countFiles(new File(outputFolder)) > 0) {
			audiofilesutils.getLastModifiedFile(outputFolder).delete();
		}
		
		int newnumber;
		if(audiofilesutils.countFiles(new File(outputFolder)) > 0) {
			newnumber = Integer.parseInt(audiofilesutils.getMaxNumber(audiofilesutils.sortByNumber(new File(outputFolder)))) + 1;
		} else {
			newnumber = 1;
		}
		
		MaryAudioUtils.writeWavFile(MaryAudioUtils.getSamplesAsDoubleArray(audio), outputFolder + newnumber + ".fcs", audio.getFormat());
	}

	public void stop() {
		if (audioplayer != null) {
			audioplayer.cancel();
		}
	}

	public Collection<Voice> getAvailableVoices() {
		return Voice.getAvailableVoices();
	}

	public List<AudioEffect> getAudioEffects() {
		return StreamSupport.stream(AudioEffects.getEffects().spliterator(), false).collect(Collectors.toList());
	}

	public void setVoice(String voice) {
		maryinterface.setVoice(voice);
	}

	public String getTTS() {
		return tts;
	}
}
