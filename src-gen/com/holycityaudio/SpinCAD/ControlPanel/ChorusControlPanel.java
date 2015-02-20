/* SpinCAD Designer - DSP Development Tool for the Spin FV-1 
 * ChorusControlPanel.java
 * Copyright (C) 2015 - Gary Worsham 
 * Based on ElmGen by Andrew Kilpatrick 
 * 
 *   This program is free software: you can redistribute it and/or modify 
 *   it under the terms of the GNU General Public License as published by 
 *   the Free Software Foundation, either version 3 of the License, or 
 *   (at your option) any later version. 
 * 
 *   This program is distributed in the hope that it will be useful, 
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of 
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the 
 *   GNU General Public License for more details. 
 * 
 *   You should have received a copy of the GNU General Public License 
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 *     
 */ 
		package com.holycityaudio.SpinCAD.ControlPanel;
		import javax.swing.JFrame;
		import javax.swing.SwingUtilities;
		import javax.swing.event.ChangeEvent;
		import javax.swing.event.ChangeListener;
		import java.awt.event.WindowEvent;
		import java.awt.event.WindowListener;
		import java.awt.event.ItemEvent;
		import javax.swing.BoxLayout;
		import javax.swing.JSlider;
		import javax.swing.JLabel;
		import javax.swing.JCheckBox;
		
		import com.holycityaudio.SpinCAD.CADBlocks.ChorusCADBlock;

		public class ChorusControlPanel {
		private JFrame frame;

		private ChorusCADBlock gCB;
		// declare the controls
			JSlider delayLengthSlider;
			JLabel  delayLengthLabel;	
			JSlider tap1CenterSlider;
			JLabel  tap1CenterLabel;	
			JSlider tap2CenterSlider;
			JLabel  tap2CenterLabel;	
			JSlider rateSlider;
			JLabel  rateLabel;	
			JSlider widthSlider;
			JLabel  widthLabel;	

		public ChorusControlPanel(ChorusCADBlock genericCADBlock) {
		
		gCB = genericCADBlock;

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {

				frame = new JFrame();
				frame.setTitle("Chorus");
				frame.setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));

			
			delayLengthSlider = new JSlider(JSlider.HORIZONTAL, (int)(0 * 1),(int) (1024 * 1), (int) (gCB.getdelayLength() * 1));
			delayLengthSlider.addChangeListener(new ChorusSliderListener());
			delayLengthLabel = new JLabel();
			updatedelayLengthLabel();
			frame.getContentPane().add(delayLengthLabel);
			frame.getContentPane().add(delayLengthSlider);		
			
			tap1CenterSlider = new JSlider(JSlider.HORIZONTAL, (int)(0.0 * 1000.0),(int) (1.0 * 1000.0), (int) (gCB.gettap1Center() * 1000.0));
			tap1CenterSlider.addChangeListener(new ChorusSliderListener());
			tap1CenterLabel = new JLabel();
			updatetap1CenterLabel();
			frame.getContentPane().add(tap1CenterLabel);
			frame.getContentPane().add(tap1CenterSlider);		
			
			tap2CenterSlider = new JSlider(JSlider.HORIZONTAL, (int)(0.0 * 1000.0),(int) (1.0 * 1000.0), (int) (gCB.gettap2Center() * 1000.0));
			tap2CenterSlider.addChangeListener(new ChorusSliderListener());
			tap2CenterLabel = new JLabel();
			updatetap2CenterLabel();
			frame.getContentPane().add(tap2CenterLabel);
			frame.getContentPane().add(tap2CenterSlider);		
			
			rateSlider = new JSlider(JSlider.HORIZONTAL, (int)(0.0 * 100.0),(int) (20.0 * 100.0), (int) (gCB.getrate() * 100.0));
			rateSlider.addChangeListener(new ChorusSliderListener());
			rateLabel = new JLabel();
			updaterateLabel();
			frame.getContentPane().add(rateLabel);
			frame.getContentPane().add(rateSlider);		
			
			widthSlider = new JSlider(JSlider.HORIZONTAL, (int)(0.0 * 100.0),(int) (100.0 * 100.0), (int) (gCB.getwidth() * 100.0));
			widthSlider.addChangeListener(new ChorusSliderListener());
			widthLabel = new JLabel();
			updatewidthLabel();
			frame.getContentPane().add(widthLabel);
			frame.getContentPane().add(widthSlider);		
				frame.addWindowListener(new MyWindowListener());
				frame.setVisible(true);		
				frame.pack();
				frame.setResizable(false);
				frame.setLocation(gCB.getX() + 100, gCB.getY() + 100);
				frame.setAlwaysOnTop(true);
			}
		});
		}

		// add change listener for Sliders 
		class ChorusSliderListener implements ChangeListener { 
		public void stateChanged(ChangeEvent ce) {
			if(ce.getSource() == delayLengthSlider) {
				gCB.setdelayLength((double) (delayLengthSlider.getValue()/1));
				updatedelayLengthLabel();
			}
			if(ce.getSource() == tap1CenterSlider) {
				gCB.settap1Center((double) (tap1CenterSlider.getValue()/1000.0));
				updatetap1CenterLabel();
			}
			if(ce.getSource() == tap2CenterSlider) {
				gCB.settap2Center((double) (tap2CenterSlider.getValue()/1000.0));
				updatetap2CenterLabel();
			}
			if(ce.getSource() == rateSlider) {
				gCB.setrate((double) (rateSlider.getValue()/100.0));
				updaterateLabel();
			}
			if(ce.getSource() == widthSlider) {
				gCB.setwidth((double) (widthSlider.getValue()/100.0));
				updatewidthLabel();
			}
			}
		}

		// add item listener 
		class ChorusItemListener implements java.awt.event.ItemListener { 
		public void stateChanged(ChangeEvent ce) {
			}

			@Override
			public void itemStateChanged(ItemEvent arg0) {
				// TODO Auto-generated method stub
			}
		}
		private void updatedelayLengthLabel() {
		delayLengthLabel.setText("Chorus_Time " + String.format("%4.0f", (1000 * gCB.getdelayLength())/gCB.getSamplerate()));		
		}		
		private void updatetap1CenterLabel() {
		tap1CenterLabel.setText("Tap_1_Center " + String.format("%4.2f", gCB.gettap1Center()));		
		}		
		private void updatetap2CenterLabel() {
		tap2CenterLabel.setText("Tap_2_Center " + String.format("%4.2f", gCB.gettap2Center()));		
		}		
		private void updaterateLabel() {
		rateLabel.setText("LFO_Rate " + String.format("%4.2f", gCB.getrate()));		
		}		
		private void updatewidthLabel() {
		widthLabel.setText("LFO_Width " + String.format("%4.1f", gCB.getwidth()));		
		}		
		
		class MyWindowListener implements WindowListener
		{
		@Override
			public void windowActivated(WindowEvent arg0) {
			}

		@Override
			public void windowClosed(WindowEvent arg0) {
			}

		@Override
			public void windowClosing(WindowEvent arg0) {
				gCB.clearCP();
			}

		@Override
			public void windowDeactivated(WindowEvent arg0) {
			}

		@Override
		public void windowDeiconified(WindowEvent arg0) {
		}

		@Override
		public void windowIconified(WindowEvent arg0) {

		}

			@Override
			public void windowOpened(WindowEvent arg0) {
			}
		}
		
	}
