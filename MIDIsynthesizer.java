package cbsp;

import javax.sound.midi.*;

public class MIDIsynthesizer {
	protected Synthesizer synthesizer;
	//protected MidiChannel channel;

	MIDIsynthesizer() {
		// Create MIDI Synthesizer
		try {
			synthesizer = MidiSystem.getSynthesizer();
			synthesizer.open();
		} catch (MidiUnavailableException e) {
			e.printStackTrace();
		}
	}

	public void playNote(int ch, int program, Note t) {
		try {
			// add tempo 120

			// Set the current MIDI channel.
			MidiChannel channel = synthesizer.getChannels()[ch];
			//Set the current MIDI instrument.
			channel.programChange(program);

			channel.noteOn(t.pitch, t.intensity);
			Thread.sleep(t.duration);
			channel.noteOff(t.pitch, t.intensity);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void playChord(int ch, int program, Note[] chord) {
		try {
			// add tempo 120

			// Set the current MIDI channel.
			MidiChannel channel = synthesizer.getChannels()[ch];
			//Set the current MIDI instrument.
			channel.programChange(program);

			for(int i = 0; i < chord.length; i ++){
				Note n = chord[i];
				channel.noteOn(n.pitch, n.intensity);
			}
			Thread.sleep(chord[0].duration);
			for(int i = 0; i < chord.length; i ++) {
				Note n = chord[i];
				channel.noteOff(n.pitch, n.intensity);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
