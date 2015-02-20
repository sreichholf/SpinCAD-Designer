/* SpinCAD Designer - DSP Development Tool for the Spin FV-1 
 * ChorusCADBlock.java
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
		package com.holycityaudio.SpinCAD.CADBlocks;
	
		import com.holycityaudio.SpinCAD.SpinCADBlock;
		import com.holycityaudio.SpinCAD.SpinCADPin;
		import com.holycityaudio.SpinCAD.SpinFXBlock;
 		import com.holycityaudio.SpinCAD.ControlPanel.ChorusControlPanel;
		
		public class ChorusCADBlock extends SpinCADBlock {

			private static final long serialVersionUID = 1L;
			private ChorusControlPanel cp = null;
			
			private double rate = 20;
			private double width = 256;
			private int output1;
			private double delayLength = 4096;
			private double tap1Center = 0.5;
			private double delayOffset = -1;

			public ChorusCADBlock(int x, int y) {
				super(x, y);
				setName("Chorus");	
				// Iterate through pin definitions and allocate or assign as needed
				addInputPin(this, "Input");
				addOutputPin(this, "Output");
				addControlInputPin(this, "LFO_Rate");
				addControlInputPin(this, "LFO_Width");
			// if any control panel elements declared, set hasControlPanel to true
						hasControlPanel = true;
						hasControlPanel = true;
						hasControlPanel = true;
						}
		
			// In the event there are parameters editable by control panel
			public void editBlock(){ 
				if(cp == null) {
					if(hasControlPanel == true) {
						cp = new ChorusControlPanel(this);
					}
				}
			}
			
			public void clearCP() {
				cp = null;
			}	
				
			public void generateCode(SpinFXBlock sfxb) {
	
			// Iterate through mem and equ statements, allocate accordingly

			
			sfxb.comment(getName());
			
			SpinCADPin sp = null;
					
			// Iterate through pin definitions and connect or assign as needed
			sp = this.getPin("Input").getPinConnection();
			int input = -1;
			if(sp != null) {
				input = sp.getRegister();
			}
			
			sp = this.getPin("LFO_Rate").getPinConnection();
			int rate = -1;
			if(sp != null) {
				rate = sp.getRegister();
			}
			
			sp = this.getPin("LFO_Width").getPinConnection();
			int width = -1;
			if(sp != null) {
				width = sp.getRegister();
			}
			
			
			// finally, generate the instructions
			output1 = sfxb.allocateReg();
			if(this.getPin("Input").isConnected() == true) {
			int	delayOffset = sfxb.getDelayMemAllocated() + 1;
			sfxb.FXallocDelayMem("delayl", 8193); 
			sfxb.skip(RUN, 1);
			sfxb.loadSinLFO(SIN0, 10, 256);
			sfxb.loadAccumulator(input);
			sfxb.FXwriteDelay("delayl", 0, 0);
			int chorusCenter = (int) (0.5 * tap1Center * delayOffset + 0.25 * tap1Center * delayLength); 
			sfxb.chorusReadDelay(0, SIN|REG|COMPC, chorusCenter );
			sfxb.chorusReadDelay(0, SIN, chorusCenter + 1);
			sfxb.writeRegister(output1, 0);
			this.getPin("Output").setRegister(output1);
			}
			

			}
			
			// create setters and getter for control panel variables
			public void setdelayLength(double __param) {
				delayLength = __param;	
			}
			
			public double getdelayLength() {
				return delayLength;
			}
			public void settap1Center(double __param) {
				tap1Center = __param;	
			}
			
			public double gettap1Center() {
				return tap1Center;
			}
			public void setrate(double __param) {
				rate = __param;	
			}
			
			public double getrate() {
				return rate;
			}
		}	
