/*
 * Select the drum sounds for the VirtualDrummer.
 * @author Jun
 */
package ch.unizh.ini.jaer.projects.gesture.virtualdrummer;

import javax.swing.BoxLayout;

/**
 * Allows selection of drum sounds for virtual drummer demo.
 * @author Jun Haeng Lee
 */
public class BeatBoxSetting extends javax.swing.JFrame {

    public BeatBoxSetting(DrumSounds drumSounds) {
        setTitle("VirtualDrummer.BeatBoxSetting");
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        for (DrumSound d : drumSounds.drums) {
            getContentPane().add(new DrumSelector(d));
        }
        pack();
    }
}
