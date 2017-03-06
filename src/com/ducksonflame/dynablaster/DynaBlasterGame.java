package com.ducksonflame.dynablaster;
import javax.swing.JFrame;
import java.awt.EventQueue;

/**
 * Main game class. Only starts a frame in a separate thread and sets it visible.
 * @version 0.5
 * @author DucksOnFlame
 */
public class DynaBlasterGame {

	public static void main(String[] args) {
		    
		EventQueue.invokeLater(new Runnable() {
		    @Override
		    public void run() {
		        JFrame dyna = new MyFrame();
		        dyna.setVisible(true);
		    }
		});
	}
	
}
