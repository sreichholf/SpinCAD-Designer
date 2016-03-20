/* SpinCAD Designer - DSP Development Tool for the Spin FV-1 
 * HPF_RDFXControlPanel.java
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
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.event.ItemEvent;
import javax.swing.BoxLayout;
import javax.swing.JSlider;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JLabel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.Box;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.BorderFactory;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import java.awt.Dimension;
import java.text.DecimalFormat;
import com.holycityaudio.SpinCAD.SpinCADBlock;
import com.holycityaudio.SpinCAD.spinCADControlPanel;
import com.holycityaudio.SpinCAD.CADBlocks.HPF_RDFXCADBlock;

public class HPF_RDFXControlPanel extends spinCADControlPanel {
	private JFrame frame;

	private HPF_RDFXCADBlock gCB;
	// declare the controls
	JSlider freqSlider;
	JLabel  freqLabel;	

public HPF_RDFXControlPanel(HPF_RDFXCADBlock genericCADBlock) {
		
		gCB = genericCADBlock;

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {

				frame = new JFrame();
				frame.setTitle("HPF 1P");
				frame.setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));

			
			freqSlider = gCB.LogFilterSlider(40,3500,gCB.getfreq());
				freqSlider.addChangeListener(new HPF_RDFXListener());
				freqLabel = new JLabel();
				Border freqBorder1 = BorderFactory.createBevelBorder(BevelBorder.LOWERED);
				freqLabel.setBorder(freqBorder1);
				updatefreqLabel();
				
				Border freqborder2 = BorderFactory.createBevelBorder(BevelBorder.RAISED);
				JPanel freqinnerPanel = new JPanel();
					
				freqinnerPanel.setLayout(new BoxLayout(freqinnerPanel, BoxLayout.Y_AXIS));
				freqinnerPanel.add(Box.createRigidArea(new Dimension(5,4)));			
				freqinnerPanel.add(freqLabel);
				freqinnerPanel.add(Box.createRigidArea(new Dimension(5,4)));			
				freqinnerPanel.add(freqSlider);		
				freqinnerPanel.setBorder(freqborder2);
			
				frame.add(freqinnerPanel);
				frame.addWindowListener(new MyWindowListener());
				frame.pack();
				frame.setResizable(false);
				frame.setLocation(gCB.getX() + 100, gCB.getY() + 100);
				frame.setAlwaysOnTop(true);
				frame.setVisible(true);		
			}
		});
		}

		// add change listener for Sliders, Spinners 
		class HPF_RDFXListener implements ChangeListener { 
		public void stateChanged(ChangeEvent ce) {
			if(ce.getSource() == freqSlider) {
			gCB.setfreq((double) gCB.freqToFilt(gCB.sliderToLogval((int)(freqSlider.getValue()), 100.0)));
				updatefreqLabel();
			}
			}
		}

		// add item state changed listener for Checkbox
		class HPF_RDFXItemListener implements java.awt.event.ItemListener { 
			
		@Override
			public void itemStateChanged(ItemEvent arg0) {
			}
		}
		
		// add action listener for Combo Box
		class HPF_RDFXActionListener implements java.awt.event.ActionListener { 
			@Override
			public void actionPerformed(ActionEvent arg0) {
			}
		}
		private void updatefreqLabel() {
		//				kflLabel.setText("HF damping freq 1:" + String.format("%4.1f", gCB.filtToFreq(gCB.getkfl())) + " Hz");		
						freqLabel.setText("Frequency (Hz) " + String.format("%4.1f", gCB.filtToFreq(gCB.getfreq())) + " Hz");		
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
