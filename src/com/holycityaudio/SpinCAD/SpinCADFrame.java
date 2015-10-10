/* SpinCAD Designer - DSP Development Tool for the Spin FV-1
 * SpinCADFrame.java
 * Copyright (C) 2013 - 2014 - Gary Worsham
 * Based on ElmGen by Andrew Kilpatrick.  Modified by Gary Worsham 2013 - 2014.  Look for GSW in code.
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

package com.holycityaudio.SpinCAD;

import java.awt.BorderLayout;

import javax.sound.sampled.UnsupportedAudioFileException;
// import javax.sound.sampled.spi.AudioFileReader;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.JButton;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JProgressBar;
import javax.swing.JMenu;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import org.andrewkilpatrick.elmGen.Debug;
import org.andrewkilpatrick.elmGen.ElmProgram;
import org.andrewkilpatrick.elmGen.simulator.AudioFileReader;
import org.andrewkilpatrick.elmGen.simulator.SpinSimulator;

import com.holycityaudio.SpinCAD.CADBlocks.FBInputCADBlock;
import com.holycityaudio.SpinCAD.CADBlocks.FBOutputCADBlock;
import com.holycityaudio.SpinCAD.SpinCADPanel.dragModes;

import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.prefs.Preferences;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.SystemColor;
import java.awt.Toolkit;

public class SpinCADFrame extends JFrame {
	/**
	 * 
	 */

	int buildNum = 956;
	private static final long serialVersionUID = -123123512351241L;

	// Swing things
	private JPanel contentPane;
	// pb shows instructions, registers, and RAM used.
	// I should add LFOs to it also
	private final ModelResourcesToolBar pb = new ModelResourcesToolBar();
	// etb is used to show the pin name when you hover
	public final EditResourcesToolBar etb = new EditResourcesToolBar();
	private final simControlToolBar sctb = new simControlToolBar();
	private commentBlockPatch cb = new commentBlockPatch();
	private final JPanel controlPanels = new JPanel();
	// 
	// topPanel holds bankPanel and simPanel
	private final JPanel topPanel = new JPanel();
	private final JPanel simPanel = new JPanel();
	private final JPanel bankPanel = new bankPanel();
	private JPanel loggerPanel = new JPanel();		// see if we can display the logger panel within the main frame

	SpinSimulator sim;
	private JPanel levelMonitor = new JPanel();

	private boolean simRunning = false;
	private boolean loggerIsVisible = false;
	private static double pot0Level = 0;
	private static double pot1Level = 0;
	private static double pot2Level = 0;

	// stuff to do with working on a bank of 8 vs. just one patch
	boolean bankMode = false;
	int bankIndex;
	// ========================================================
	private static SpinCADModel model = new SpinCADModel();
	private static SpinCADModel[] bank = new SpinCADModel[8];

	// following things are saved in the SpinCAD preferences
	private Preferences prefs;
	private RecentFileList recentBankFileList = null;
	private RecentFileList recentPatchFileList = null;
	// this next one is specific to file open, needs to be here for MRU file list operations
	private JFileChooser fc;
	// simulator input file
	private static String spcFileName = "Untitled";
	// simulator output file
	private String outputFile = null; // play out through the sound card


	// modelSave is used to undo deletes
	ByteArrayOutputStream modelSave;
	private int canUndo = 0;

	// ------------------------------------------------------------
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
					SpinCADFrame dspFrame = new SpinCADFrame();
					dspFrame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */

	@SuppressWarnings("unused")
	public SpinCADFrame() {
		setTitle("SpinCAD Designer - Untitled");
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		setBounds(100, 100, 800, 600);

		final SpinCADPanel panel = new SpinCADPanel(this);
		panel.setBackground(SystemColor.inactiveCaption);

		// create a Preferences instance (somewhere later in the code)
		prefs = Preferences.userNodeForPackage(this.getClass());

		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
				} finally {
					loadRecentPatchFileList();			
					loadRecentBankFileList();			
				}
			}
		});

		WindowListener exitListener = window();
		addWindowListener(exitListener);

		// ==========================================================================================
		// ======================= main panel
		// =========================================================


		contentPane = new JPanel();
		contentPane.setBackground(Color.LIGHT_GRAY);
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);

		JScrollPane scrollPane = new JScrollPane(panel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		setPreferredSize(new Dimension(450, 1200));

		contentPane.add(scrollPane, BorderLayout.CENTER);

		// =========================================================
		// ======================= toolbars ========================
		// =========================================================

		JPanel toolBarPanel = new JPanel();
		toolBarPanel.setLayout(new BoxLayout(toolBarPanel, BoxLayout.Y_AXIS));
		contentPane.add(toolBarPanel, BorderLayout.SOUTH);

		etb.setFloatable(false);
		toolBarPanel.add(etb, BorderLayout.SOUTH);

		pb.setFloatable(false);
		toolBarPanel.add(pb, BorderLayout.SOUTH);

		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));

		//--------------------------------------
		// patch selector buttons in bank toolbar

		Border border = BorderFactory.createBevelBorder(BevelBorder.RAISED);

		bankPanel.setLayout(new GridLayout(1,8));
		bankPanel.setVisible(true);
		//		simControlToolBar);	// start up with bank panel hidden
		bankPanel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(Color.green),
				bankPanel.getBorder()));

		topPanel.add(bankPanel, BorderLayout.NORTH);
		//----------------------------------------

		sctb.setFloatable(false);
		sctb.setBorder(border);
		simPanel.setLayout(new BoxLayout(simPanel, BoxLayout.Y_AXIS));
		simPanel.add(sctb);
		simPanel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(Color.blue),
				simPanel.getBorder()));
		topPanel.add(simPanel, BorderLayout.NORTH);

		contentPane.add(topPanel, BorderLayout.NORTH);

		contentPane.add(levelMonitor, BorderLayout.WEST);

		simPanel.add(loggerPanel);
		loggerPanel.setVisible(false);

		// controlPanels.setFloatable(false);
		contentPane.add(controlPanels, BorderLayout.EAST);
		controlPanels.setLayout(new BoxLayout(controlPanels, BoxLayout.Y_AXIS));
		// Then on your component(s)
		// ======================================================
		// ; ==================== menu bar and items ==========

		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		JMenu mnFileMenu = new JMenu("File");
		menuBar.add(mnFileMenu);

		JMenu mnNewMenu = new JMenu("New");

		JMenuItem mntmNewFile = new JMenuItem("Patch");
		fileNewPatch(panel, mntmNewFile);
		mnNewMenu.add(mntmNewFile);

		JMenuItem mntmNewBank = new JMenuItem("Bank");
		fileNewBank(panel, mntmNewBank);
		mnNewMenu.add(mntmNewBank);
		mnFileMenu.add(mnNewMenu);

		JMenu mnOpenMenu = new JMenu("Open");

		JMenuItem mntmOpenPatch = new JMenuItem("Patch");
		mntmOpenPatch.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				fileOpenPatch(panel);
				bank[bankIndex] = model;
			}
		});

		mnOpenMenu.add(mntmOpenPatch);

		JMenuItem mntmOpenBank = new JMenuItem("Bank");
		mntmOpenBank.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				fileOpenBank(panel);
			}
		});
		mnOpenMenu.add(mntmOpenBank);
		mnFileMenu.add(mnOpenMenu);

		JMenu mnSaveMenu = new JMenu("Save");
		JMenuItem mntmSavePatch = new JMenuItem("Patch");
		fileSave(mntmSavePatch);
		mntmSavePatch.setAccelerator(KeyStroke.getKeyStroke("ctrl s"));
		mnSaveMenu.add(mntmSavePatch);

		JMenuItem mntmSaveBank = new JMenuItem("Bank");
		fileSave(mntmSaveBank);
		mnSaveMenu.add(mntmSaveBank);
		mnFileMenu.add(mnSaveMenu);

		JMenu mnSaveAsMenu = new JMenu("Save As");
		JMenuItem mntmSavePatchAs = new JMenuItem("Patch");
		mntmSavePatchAs.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				fileSavePatchAs();
			}
		});
		mnSaveAsMenu.add(mntmSavePatchAs);

		JMenuItem mntmSaveBankAs = new JMenuItem("Bank");
		mntmSaveBankAs.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				fileSaveBankAs();
			}
		});
		mnSaveAsMenu.add(mntmSaveBankAs);
		mnFileMenu.add(mnSaveAsMenu);

		JMenu mnExport = new JMenu("Export to...");

		JMenuItem mntmSaveAsm = new JMenuItem("Spin ASM");
		mntmSaveAsm.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				getModel().sortAlignGen();
				fileSaveAsm();
			}
		});
		mnExport.add(mntmSaveAsm);

		JMenuItem mntmSaveHex = new JMenuItem("Hex");
		mntmSaveHex.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				getModel().sortAlignGen();
				fileSaveHex();
			}
		});
		mnExport.add(mntmSaveHex);
		mnFileMenu.add(mnExport);

		mnFileMenu.addSeparator();

		JMenuItem mntmCopyToClipboard = new JMenuItem("Copy to Clipboard");
		mntmCopyToClipboard.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				getModel().sortAlignGen();
				StringSelection stringSelection = new StringSelection (cb.getComments() + SpinCADModel.getRenderBlock()
						.getProgramListing(1));
				Clipboard clpbrd = Toolkit.getDefaultToolkit ().getSystemClipboard ();
				clpbrd.setContents (stringSelection, null);
				//				fileSaveAsm();
			}
		});
		mnFileMenu.add(mntmCopyToClipboard);

		JMenuItem mntmBatch = new JMenuItem("Batch Convert");
		fileBatch(panel, mntmBatch);
		mnFileMenu.add(mntmBatch);

		mnFileMenu.addSeparator();

		JMenuItem mntmInfo = new JMenuItem("Information");
		mntmInfo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				cb.show();
			}
		});
		mnFileMenu.add(mntmInfo);

		mnFileMenu.addSeparator();

		JMenuItem mntmExit = new JMenuItem("Exit");
		if(bankMode == false) {
			fileSavePatchAs(panel, mntmExit);		
		} else {
			fileSaveBankAs(panel, mntmExit);				
		}
		mnFileMenu.add(mntmExit);

		JMenu mn_edit = new JMenu("Edit");
		menuBar.add(mn_edit);

		final JMenuItem mntm_Undo = new JMenuItem("Undo");
		mntm_Undo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				undo();
			}
		});
		mn_edit.add(mntm_Undo);

		JMenu mn_io_mix = new JMenu("Loop");
		menuBar.add(mn_io_mix);

		final JMenuItem mntm_AddFB = new JMenuItem("Add");
		mntm_AddFB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int i =  getModel().getIndexFB();
				FBInputCADBlock pcB = new FBInputCADBlock(50, 100, i);
				dropBlock(panel, pcB);

				FBOutputCADBlock pcB1 = new FBOutputCADBlock(50, 300, i);
				dropBlock(panel, pcB1);
				getModel().setIndexFB(i + 1);
			}
		});
		mn_io_mix.add(mntm_AddFB);

		// most of the menu is generated right here.
		// standardmenu is generated by the spincadmenu DSL
		new standardMenu(this, panel, menuBar);

		final JMenu mnSimulator = new JMenu("Simulator");
		menuBar.add(mnSimulator);

		final JMenuItem mntmSimLogger = new JCheckBoxMenuItem("Enable Level Viewer");
		mntmSimLogger.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(loggerIsVisible == true) {
					loggerIsVisible = false;
				}
				else {
					loggerIsVisible = true;
				}
			}
		});
		mnSimulator.add(mntmSimLogger);

		mnSimulator.addSeparator();
		JMenuItem mntmSimSendToFile = new JRadioButtonMenuItem("Simulator->File");
		mntmSimSendToFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				outputFile = prefs.get("SIMULATOR_OUT_FILE", "");
				sim.setLoopMode(false);
			}
		});
		mnSimulator.add(mntmSimSendToFile);

		JMenuItem mntmSimSendToSound = new JRadioButtonMenuItem("Simulator->Sound Card", true);
		mntmSimSendToSound.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				outputFile = null;
				sim.setLoopMode(true);
			}
		});
		mnSimulator.add(mntmSimSendToSound);

		ButtonGroup bg = new ButtonGroup();
		bg.add(mntmSimSendToFile);
		bg.add(mntmSimSendToSound);
		mnSimulator.addSeparator();

		JMenuItem mntmSimOutFile = new JMenuItem("Set Simulator Output File");
		mntmSimOutFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				setSimulatorOutputFile();
			}
		});
		mnSimulator.add(mntmSimOutFile);

		JMenuItem mntmSourceFile = new JMenuItem("Set Simulator Source file");
		mntmSourceFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					getSimulatorFile();
				} catch (UnsupportedAudioFileException e) {
					MessageBox("Simulator File Error", "Make sure that your simulator source\n"
							+ "file is a stereo 16 bit WAV file sampled \nat 32768, 44100, or 48000 Hz.");
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		mnSimulator.add(mntmSourceFile);		

		mnSimulator.addSeparator();
		JMenuItem mntmSetSampleRate = new JMenuItem("Set Sample Rate");
		mntmSetSampleRate.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				SampleRateComboBox srCB = new SampleRateComboBox();
				srCB.setLocation(mnSimulator.getLocation());
			}
		});
		mnSimulator.add(mntmSetSampleRate);

		if(Debug.DEBUG == true) {
			mnSimulator.addSeparator();
			JMenuItem mntmDebugFile = new JMenuItem("Set Simulator Debug file");
			mntmDebugFile.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					try {
						setSimulatorDebugFile();
					} catch (IOException e) {
						MessageBox("Simulator Debug File Error", "Uhmmmm....");
					}
				}
			});
			mnSimulator.add(mntmDebugFile);
		}

		JMenu mnHelp = new JMenu("Help");
		menuBar.add(mnHelp);

		JMenuItem mntmHelp = new JMenuItem("Help");
		mntmHelp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					openWebpage(new URL("http://holycityaudio.com/spincad-designer-2/spincad-designer-help-pages/"));
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
			}
		});
		mnHelp.add(mntmHelp);

		JMenuItem mntmAbout = new JMenuItem("About");
		mntmAbout.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				MessageBox("About SpinCAD Designer", "Version 0.97 Build " + buildNum + "\n"
						+ "Copyright 2015 Gary Worsham, Holy City Audio\n" + 
						" This program is distributed in the hope that it will be useful," +
						"\nbut WITHOUT ANY WARRANTY; without even the implied warranty of\n" + 
						"MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.");
			}
		});
		mnHelp.add(mntmAbout);
	}

	/**
	 * @param panel
	 * @param mntmExit
	 */

	void updateFrameTitle() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() { 	
				setTitle("SpinCAD Designer - " + spcFileName + (getModel().changed ? " * " : ""));			
			}
		});
	}

	private void fileSavePatchAs(final SpinCADPanel panel, JMenuItem mntmExit) {
		mntmExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (getModel().getChanged() == true) {
					int dialogResult = yesNoBox(panel, "Warning!", 
							"You have unsaved changes!  Save first?");				

					if (dialogResult == JOptionPane.YES_OPTION) {
						File fileToBeSaved = new File(spcFileName);
						if (fileToBeSaved.exists()) {
							String filePath = fileToBeSaved.getPath();
							SpinCADFile.fileSave(cb, getModel(), filePath);
							saveMRUPatchFolder(fileToBeSaved.getPath());
							spcFileName = fileToBeSaved.getName();
							getModel().setChanged(false);
							updateFrameTitle();
						} else {
							fileSavePatchAs();
							spcFileName = fileToBeSaved.getName();
							saveMRUPatchFolder(fileToBeSaved.getPath());
							getModel().setChanged(false);
							updateFrameTitle();
						}
					}
					System.exit(0);
				}
			}
		});
	}

	private void fileSaveBankAs(final SpinCADPanel panel, JMenuItem mntmExit) {
		mntmExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (getModel().getChanged() == true) {
					int dialogResult = yesNoBox(panel, "Warning!", 
							"You have unsaved changes!  Save first?");				

					if (dialogResult == JOptionPane.YES_OPTION) {
						File fileToBeSaved = new File(spcFileName);
						if (fileToBeSaved.exists()) {
							String filePath = fileToBeSaved.getPath();
							//							SpinCADFile.fileSave(cb, getModel(), filePath);
							saveMRUBankFolder(fileToBeSaved.getPath());
							spcFileName = fileToBeSaved.getName();
							//							getModel().setChanged(false);
							updateFrameTitle();
						} else {
							fileSaveBankAs();
							spcFileName = fileToBeSaved.getName();
							saveMRUBankFolder(fileToBeSaved.getPath());
							//							getModel().setChanged(false);
							updateFrameTitle();
						}
					}
					System.exit(0);
				}
			}
		});
	}

	private void saveMRUPatchFolder(String path) {
		Path pathE = Paths.get(path);

		String pathS = pathE.getParent().toString();
		String nameS = pathE.getFileName().toString();

		prefs.put("MRUPatchFolder", pathS);
		prefs.put("MRUPatchFileName", nameS);
	}

	private void saveMRUBankFolder(String path) {
		Path pathE = Paths.get(path);

		String pathS = pathE.getParent().toString();
		String nameS = pathE.getFileName().toString();

		prefs.put("MRUBankFolder", pathS);
		prefs.put("MRUBankFileName", nameS);
	}

	private void saveMRUSpnFolder(String path) {
		Path pathE = Paths.get(path);
		prefs.put("MRUSpnFolder", pathE.toString());
	}

	private void saveMRUHexFolder(String path) {
		Path pathE = Paths.get(path);
		prefs.put("MRUHexFolder", pathE.toString());
	}

	private void saveRecentPatchFileList() {
		StringBuilder sb = new StringBuilder(128);
		if(recentPatchFileList != null) {
			int k = recentPatchFileList.listModel.getSize() - 1;
			for (int index = 0; index <= k; index++) {
				File file = recentPatchFileList.listModel.getElementAt(k - index);
				if (sb.length() > 0) {
					sb.append(File.pathSeparator);
				}
				sb.append(file.getPath());
			}
			Preferences p = Preferences.userNodeForPackage(RecentFileList.class);
			p.put("RecentFileList.fileList", sb.toString());
		}
	}

	private void saveRecentBankFileList() {
		StringBuilder sb = new StringBuilder(128);
		if(recentBankFileList != null) {
			int k = recentBankFileList.listModel.getSize() - 1;
			for (int index = 0; index <= k; index++) {
				File file = recentBankFileList.listModel.getElementAt(k - index);
				if (sb.length() > 0) {
					sb.append(File.pathSeparator);
				}
				sb.append(file.getPath());
			}
			Preferences p = Preferences.userNodeForPackage(RecentFileList.class);
			p.put("RecentBankFileList.fileList", sb.toString());
		}
	}

	private void loadRecentPatchFileList() {
		Preferences p = Preferences.userNodeForPackage(RecentFileList.class);
		String listOfFiles = p.get("RecentFileList.fileList", null);
		if (fc == null) {
			String savedPath = prefs.get("MRUPatchFolder", "");
			File MRUFolder = new File(savedPath);
			fc = new JFileChooser(MRUFolder);
			recentPatchFileList = new RecentFileList(fc);
			if (listOfFiles != null) {
				String[] files = listOfFiles.split(File.pathSeparator);
				for (String fileRef : files) {
					File file = new File(fileRef);
					if (file.exists()) {
						recentPatchFileList.listModel.add(file);
					}
				}
			}
			fc.setAccessory(recentPatchFileList);
			fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		}
	}

	private void loadRecentBankFileList() {
		Preferences p = Preferences.userNodeForPackage(RecentFileList.class);
		String listOfFiles = p.get("RecentBankFileList.fileList", null);
		if (fc == null) {
			String savedPath = prefs.get("MRUBankFolder", "");
			File MRUBankFolder = new File(savedPath);
			fc = new JFileChooser(MRUBankFolder);
			recentBankFileList = new RecentFileList(fc);
			if (listOfFiles != null) {
				String[] files = listOfFiles.split(File.pathSeparator);
				for (String fileRef : files) {
					File file = new File(fileRef);
					if (file.exists()) {
						recentBankFileList.listModel.add(file);
					}
				}
			}
			fc.setAccessory(recentBankFileList);
			fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		}
	}

	/**
	 * @param mntmSave
	 */
	private void fileSave(JMenuItem mntmSave) {
		mntmSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(spcFileName != "Untitled") {
					File fileToBeSaved = new File(prefs.get("MRUPatchFolder",  "") + "/" + spcFileName);
					String filePath = fileToBeSaved.getPath();
					try {
						SpinCADFile.fileSave(cb, getModel(), filePath);
						prefs.put("MRUPatchFolder", filePath);
						saveMRUPatchFolder(filePath);
						getModel().setChanged(false);
						recentPatchFileList.add(fileToBeSaved);
						updateFrameTitle();
					} finally {
					}

				} else {
					fileSavePatchAs();
				}
			}
		});
	}

	/**
	 * @param panel
	 * @param mntmFile
	 */
	private void fileOpenMenu(final SpinCADPanel panel, JMenuItem mntmFile) {
	}

	void fileOpenPatch(JPanel panel) {
		if (getModel().getChanged() == true) {
			int dialogResult = yesNoBox(panel, "Warning!",
					"You have unsaved changes!  Continue?");
			if (dialogResult == 0) {
				getModel().newModel();
				repaint();
			}
		}

		// debug, want to open recent file list at program init.
		// TODO set most recently used folder
		loadRecentPatchFileList();

		final String newline = "\n";
		// In response to a button click:
		FileNameExtensionFilter filter = new FileNameExtensionFilter(
				"SpinCAD Files", "spcd");
		fc.setFileFilter(filter);

		int returnVal = fc.showOpenDialog(SpinCADFrame.this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			// This is where a real application would open the file.
			System.out.println("Opening: " + file.getName() + "."
					+ newline);
			try {
				String filePath = file.getPath();
				model = SpinCADFile.fileRead(cb, getModel(), filePath );
				spcFileName = file.getName();
				getModel().getIndexFB();
				getModel().setChanged(false);						
				getModel().presetIndexFB();
				saveMRUPatchFolder(filePath);
				recentPatchFileList.add(file);
				updateFrameTitle();
			} catch (Exception e) {	// thrown over in SpinCADFile.java
				//						e.printStackTrace();
				MessageBox("File open failed!", "This spcd file may be from\nan incompatible version of \nSpinCAD Designer.");
				spcFileName = "Untitled";
				updateFrameTitle();
				getModel().newModel();
			}
		} else {
			System.out.println("Open command cancelled by user."
					+ newline);
		}
		pb.update();
		panel.repaint();
	}

	void fileOpenBank(JPanel panel) {
		if (getModel().getChanged() == true) {
			int dialogResult = yesNoBox(panel, "Warning!",
					"You have unsaved changes!  Continue?");
			if (dialogResult == 0) {
				getModel().newModel();
				repaint();
			}
		}

		// debug, want to open recent file list at program init.
		// TODO set most recently used folder
		loadRecentBankFileList();

		final String newline = "\n";
		// In response to a button click:
		FileNameExtensionFilter filter = new FileNameExtensionFilter(
				"SpinCAD Bank Files", "spbk");
		fc.setFileFilter(filter);

		int returnVal = fc.showOpenDialog(SpinCADFrame.this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			// This is where a real application would open the file.
			System.out.println("Opening: " + file.getName() + "."
					+ newline);
			try {
				// first, open bank, then open patch 0
				String filePath = file.getPath();
				//				model = SpinCADFile.fileRead(cb, getModel(), filePath );
				//				spcFileName = file.getName();
				//				getModel().getIndexFB();
				//				getModel().setChanged(false);						
				//				getModel().presetIndexFB();
				saveMRUBankFolder(filePath);
				recentBankFileList.add(file);
				updateFrameTitle();
			} catch (Exception e) {	// thrown over in SpinCADFile.java
				//						e.printStackTrace();
				MessageBox("File open failed!", "This spbk file may be from\nan incompatible version of \nSpinCAD Designer.");
				//				spcFileName = "Untitled";
				updateFrameTitle();
				getModel().newModel();
			}
		} else {
			System.out.println("Open command cancelled by user."
					+ newline);
		}
		pb.update();
		panel.repaint();
	}


	private void fileBatch(final SpinCADPanel panel, JMenuItem mntmFile) {
		mntmFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				// Create a file chooser
				if (getModel().getChanged() == true) {
					int dialogResult = yesNoBox(panel, "Warning!",
							"You have unsaved changes!  Continue?");
					if (dialogResult == 0) {
						getModel().newModel();
						repaint();
						// System.out.println("Yes option");
					}
				}
				String savedPath = prefs.get("MRUFolder", "");

				final JFileChooser fc = new JFileChooser(savedPath);
				fc.setDialogTitle("Choose files to convert...");
				fc.setMultiSelectionEnabled(true);

				final String newline = "\n";
				// In response to a button click:
				FileNameExtensionFilter filter = new FileNameExtensionFilter(
						"SpinCAD Patch Files", "spcd");
				fc.setFileFilter(filter);

				int returnVal = fc.showOpenDialog(SpinCADFrame.this);

				// returnVal is from the first file open dialog
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					// now ask user to enter converted file destination
					savedPath = prefs.get("MRUSpnFolder", "");

					final JFileChooser fc2 = new JFileChooser(savedPath); 
					fc2.setDialogTitle("Choose destination...");
					fc2.setMultiSelectionEnabled(false);
					fc2.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
					fc2.setAcceptAllFileFilterUsed(false);
					// In response to a button click:
					filter = new FileNameExtensionFilter("Spin ASM Files", "spn");
					fc2.setFileFilter(filter);

					int retVal2 = fc2.showSaveDialog(SpinCADFrame.this);

					if(retVal2 == JFileChooser.APPROVE_OPTION) {
						File myFile = fc2.getSelectedFile();
						if(myFile.isDirectory() == true) {
							saveMRUSpnFolder(myFile.getAbsolutePath());
						}

						int index = 0;
						int failed = 0;

						File files[] = fc.getSelectedFiles();
						// This is where a real application would open the file.
						saveMRUPatchFolder(files[0].getPath());
						while(index < files.length) {
							System.out.println("Opening: " + files[index].getName() + "."
									+ newline);
							try {
								String filePath = files[index].getPath();
								model = SpinCADFile.fileRead(cb, getModel(), filePath );
								//cb.line2text.setText()
								getModel().getIndexFB();
								getModel().setChanged(false);						
								getModel().sortAlignGen();

								spcFileName = files[index].getName();
								String spnPath  = prefs.get("MRUSpnFolder", "") + "/" + spcFileName + ".spn";
								SpinCADFile.fileSaveAsm(cb, SpinCADModel.getRenderBlock().getProgramListing(1), spnPath.replace(".spcd.spn",  ".spn"));
								updateFrameTitle();
							} catch (Exception e) {	// thrown over in SpinCADFile.java
								spcFileName = "Untitled";
								//						e.printStackTrace();
								failed++;
								MessageBox("File convert failed! " + spcFileName, spcFileName + " may be from\nan incompatible version of \nSpinCAD Designer.");
							}
							index++;
						}
						getModel().newModel();
						spcFileName = "Untitled";
						pb.update();
						repaint();
						updateFrameTitle();
						MessageBox("Conversion completed", (index - failed) + " files were converted.\n" + failed + " files failed.");
					} else {
						System.out.println("Open command cancelled by user."
								+ newline);
					}
				}
			}
		});
	}

	public void fileSavePatchAs() {
		// Create a file chooser
		String savedPath = prefs.get("MRUFolder", "");
		final JFileChooser fc = new JFileChooser(savedPath);
		FileNameExtensionFilter filter = new FileNameExtensionFilter(
				"SpinCAD Files", "spcd");
		fc.setFileFilter(filter);
		fc.setSelectedFile(new File(spcFileName));
		int returnVal = fc.showSaveDialog(SpinCADFrame.this);
		// need to process user canceling box right here
		if (returnVal == JFileChooser.APPROVE_OPTION) {

			// In response to a button click:
			File fileToBeSaved = fc.getSelectedFile();

			if (!fc.getSelectedFile().getAbsolutePath().endsWith(".spcd")) {
				fileToBeSaved = new File(fc.getSelectedFile() + ".spcd");
			}
			int n = JOptionPane.YES_OPTION;
			if (fileToBeSaved.exists()) {
				JFrame frame = new JFrame();
				n = JOptionPane.showConfirmDialog(frame,
						"Would you like to overwrite it?", "File already exists!",
						JOptionPane.YES_NO_OPTION);
			}
			if (n == JOptionPane.YES_OPTION) {
				try {
					SpinCADFile.fileSave(cb, getModel(), fileToBeSaved.getPath());
					spcFileName = fileToBeSaved.getName();
					getModel().setChanged(false);
					recentPatchFileList.add(fileToBeSaved);
					saveMRUPatchFolder(fileToBeSaved.getPath());
					updateFrameTitle();
					cb.updateFileName();
				} finally {
				}
			}
		}
	}

	public void fileSaveBankAs() {
		// Create a file chooser
		String savedPath = prefs.get("MRUBankFolder", "");
		final JFileChooser fc = new JFileChooser(savedPath);
		FileNameExtensionFilter filter = new FileNameExtensionFilter(
				"SpinCAD Bank Files", "spbk");
		fc.setFileFilter(filter);
		fc.setSelectedFile(new File(spcFileName));
		int returnVal = fc.showSaveDialog(SpinCADFrame.this);
		// need to process user canceling box right here
		if (returnVal == JFileChooser.APPROVE_OPTION) {

			// In response to a button click:
			File fileToBeSaved = fc.getSelectedFile();

			if (!fc.getSelectedFile().getAbsolutePath().endsWith(".spbk")) {
				fileToBeSaved = new File(fc.getSelectedFile() + ".spbk");
			}
			int n = JOptionPane.YES_OPTION;
			if (fileToBeSaved.exists()) {
				JFrame frame = new JFrame();
				n = JOptionPane.showConfirmDialog(frame,
						"Would you like to overwrite it?", "File already exists!",
						JOptionPane.YES_NO_OPTION);
			}
			if (n == JOptionPane.YES_OPTION) {
				try {
					SpinCADFile.fileSave(cb, getModel(), fileToBeSaved.getPath());
					//					spcFileName = fileToBeSaved.getName();
					//					getModel().setChanged(false);
					recentBankFileList.add(fileToBeSaved);
					saveMRUPatchFolder(fileToBeSaved.getPath());
					updateFrameTitle();
					cb.updateFileName();
				} finally {
				}
			}
		}
	}

	public void fileSaveAsm() {
		// Create a file chooser
		String savedPath = prefs.get("MRUSpnFolder", "");

		final JFileChooser fc = new JFileChooser(savedPath);
		// In response to a button click:
		FileNameExtensionFilter filter = new FileNameExtensionFilter(
				"Spin ASM Files", "spn");
		fc.setFileFilter(filter);
		fc.showSaveDialog(SpinCADFrame.this);
		File fileToBeSaved = fc.getSelectedFile();

		if (!fc.getSelectedFile().getAbsolutePath().endsWith(".spn")) {
			fileToBeSaved = new File(fc.getSelectedFile() + ".spn");
		}
		int n = JOptionPane.YES_OPTION;
		if (fileToBeSaved.exists()) {
			JFrame frame = new JFrame();
			n = JOptionPane.showConfirmDialog(frame,
					"Would you like to overwrite it?", "File already exists!",
					JOptionPane.YES_NO_OPTION);
		}
		if (n == JOptionPane.YES_OPTION) {
			String filePath;
			try {
				filePath = fileToBeSaved.getPath();
				fileToBeSaved.delete();
				getModel();
			} finally {
			}
			try {
				SpinCADFile.fileSaveAsm(cb, SpinCADModel.getRenderBlock()
						.getProgramListing(1), filePath);
			} catch (IOException e) {
				JOptionPane.showOptionDialog(null,
						"File save error!", "Error",
						JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE, null, null, null);

				e.printStackTrace();
			}
			getModel().setChanged(false);
			saveMRUSpnFolder(filePath);
		}
	}

	public void fileSaveHex() {
		// Create a file chooser
		String savedPath = prefs.get("MRUHexFolder", "");

		final JFileChooser fc = new JFileChooser(savedPath);
		// In response to a button click:
		FileNameExtensionFilter filter = new FileNameExtensionFilter(
				"Hex Files", "hex");
		fc.setFileFilter(filter);
		fc.showSaveDialog(SpinCADFrame.this);
		File fileToBeSaved = fc.getSelectedFile();

		if (!fc.getSelectedFile().getAbsolutePath().endsWith(".hex")) {
			fileToBeSaved = new File(fc.getSelectedFile() + ".hex");
		}
		int n = JOptionPane.YES_OPTION;
		if (fileToBeSaved.exists()) {
			JFrame frame = new JFrame();
			n = JOptionPane.showConfirmDialog(frame,
					"Would you like to overwrite it?", "File already exists!",
					JOptionPane.YES_NO_OPTION);
		}
		if (n == JOptionPane.YES_OPTION) {
			String filePath;
			try {
				filePath = fileToBeSaved.getPath();
				fileToBeSaved.delete();
				getModel();
			} finally {
			}
			try {
				SpinCADFile.fileSaveHex(SpinCADModel.getRenderBlock()
						.generateHex(), filePath);
			} catch (IOException e) {
				JOptionPane.showOptionDialog(null,
						"File save error!", "Error",
						JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE, null, null, null);

				e.printStackTrace();
			}
			getModel().setChanged(false);
			saveMRUHexFolder(filePath);
		}
	}

	/**
	 * @param panel
	 * @param mntmNew
	 */
	private void fileNewPatch(final SpinCADPanel panel, JMenuItem mntmNew) {
		mntmNew.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {

				if (getModel().getChanged() == true) {
					int dialogButton = JOptionPane.YES_NO_OPTION;
					int dialogResult = JOptionPane.showConfirmDialog(panel,
							"You have unsaved changes!  Continue?", "Warning!",
							dialogButton);
					if (dialogResult == JOptionPane.NO_OPTION) {
						return;
					}
				}
				spcFileName = "Untitled";
				bankPanel.setVisible(false);
				updateFrameTitle();
				getModel().newModel();
				cb.clearComments();
				repaint();
			}
		});
	}

	private void fileNewBank(final SpinCADPanel panel, JMenuItem mntmNew) {
		mntmNew.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {

				if (getModel().getChanged() == true) {
					int dialogButton = JOptionPane.YES_NO_OPTION;
					int dialogResult = JOptionPane.showConfirmDialog(panel,
							"You have unsaved changes!  Continue?", "Warning!",
							dialogButton);
					if (dialogResult == JOptionPane.NO_OPTION) {
						return;
					}
				}
				spcFileName = "Untitled";
				bankPanel.setVisible(true);
				updateFrameTitle();
				getModel().newModel();
				cb.clearComments();
				repaint();
			}
		});
	}

	private WindowListener window() {
		WindowListener exitListener = new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				int confirm = JOptionPane.showOptionDialog(null,
						"Do you wish to exit SpinCAD?", "Exit Confirmation",
						JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE, null, null, null);
				if (confirm == JOptionPane.YES_OPTION) {
					saveRecentPatchFileList();
					saveRecentBankFileList();
					System.exit(0);
				}
			}
		};
		return exitListener;
	}

	public String checkSimulatorFile() {
		File f = null;
		String testWavFileName = prefs.get("SIMULATOR_FILE", "");
		if(testWavFileName == "") {
			System.out.println("Simulator file name blank!");
			return "Not found!";
		} else {
			try {
				f = new File(testWavFileName);
			} 
			catch(Exception e) {
				System.out.println("Exception opening file!");
				return "Not found!";
			}
			finally {
			}
			if(f.exists() && !f.isDirectory()) {
				return testWavFileName;	
			}
		}
		return "Not found!";
	}

	public void getSimulatorFile() throws UnsupportedAudioFileException, IOException {
		String testWavFileName = prefs.get("SIMULATOR_FILE", "");
		final JFileChooser fc = new JFileChooser(testWavFileName);
		FileNameExtensionFilter filter = new FileNameExtensionFilter(
				"WAV files", "wav");
		fc.setSelectedFile(new File(testWavFileName));
		fc.setFileFilter(filter);

		final String newline = "\n";

		int returnVal = fc.showOpenDialog(SpinCADFrame.this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			testWavFileName = fc.getSelectedFile().getPath();
			prefs.put("SIMULATOR_FILE", testWavFileName);
			System.out.println("Opening: " + testWavFileName + "." + newline);
			// do this just to check the file format, exceptions will be thrown if any
			new AudioFileReader(testWavFileName, false);
		} else {
			System.out.println("Command cancelled by user." + newline);
		}
	}

	public void setSimulatorDebugFile() throws IOException {
		String debugFileName = prefs.get("SIMULATOR_DEBUG_FILE", "");
		final JFileChooser fc = new JFileChooser(debugFileName);
		FileNameExtensionFilter filter = new FileNameExtensionFilter(
				"txt files", "txt");
		fc.setSelectedFile(new File(debugFileName));
		fc.setFileFilter(filter);

		final String newline = "\n";

		int returnVal = fc.showSaveDialog(SpinCADFrame.this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			debugFileName = fc.getSelectedFile().getPath();
			prefs.put("SIMULATOR_DEBUG_FILE", debugFileName);
		} else {
			System.out.println("Command cancelled by user." + newline);
		}
	}

	public void setSimulatorOutputFile() {
		String simWavOutFileName = prefs.get("SIMULATOR_OUT_FILE", "");
		final JFileChooser fc = new JFileChooser(simWavOutFileName);
		FileNameExtensionFilter filter = new FileNameExtensionFilter(
				"WAV files", "wav");
		fc.setSelectedFile(new File(simWavOutFileName));

		fc.setFileFilter(filter);

		final String newline = "\n";

		int returnVal = fc.showSaveDialog(SpinCADFrame.this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			simWavOutFileName = fc.getSelectedFile().getPath();
			prefs.put("SIMULATOR_OUT_FILE", simWavOutFileName);

			System.out.println("Simulator output file: " + simWavOutFileName);
		} else {
			System.out.println("Command cancelled by user." + newline);
		}
	}

	public void dropBlock(SpinCADPanel p, SpinCADBlock b) {
		getModel().addBlock(b);
		getModel().setChanged(true);
		p.unselectAll(this);
		p.dropBlockPanel(b);

	}

	public boolean isSimRunning() {
		return simRunning;
	}

	public boolean setSimRunning(boolean simRunning) {
		this.simRunning = simRunning;
		return simRunning;
	}

	public SpinCADModel getModel() {
		return model;
	}

	public void saveModel() {
		try { 
			modelSave = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(modelSave); 
			oos.writeObject(model); 
			oos.flush(); 
			oos.close(); 
		} 
		catch(Exception e) { 
			System.out.println("Exception during serialization: " + e); 
		} 
		canUndo = 1;
	}

	public void undo() {
		if(canUndo == 1) {
			try { 
				ByteArrayInputStream bais = new ByteArrayInputStream(modelSave.toByteArray());
				ObjectInputStream is = new ObjectInputStream(bais);
				model = (SpinCADModel) is.readObject();
				is.close(); 
				contentPane.repaint();
				// System.out.println("m: " + m); 
			} 
			catch(Exception e) { 
				System.out.println("Exception during deserialization: " + 
						e); 
				System.exit(0); 
			} 
			canUndo = 0;		
		}
	}

	public static void setModelCurrentBlock(SpinCADModel model) {
		SpinCADFrame.model = model;
	}

	// Swing dialog boxes.

	void MessageBox(String title, String message) {
		JFrame frame = new JFrame();
		JOptionPane.showMessageDialog(frame,
				message, title,
				JOptionPane.DEFAULT_OPTION);

	}

	int yesNoBox(JPanel panel, String title, String question) {
		int dialogButton = JOptionPane.YES_NO_OPTION;
		int dialogResult = JOptionPane.showConfirmDialog(panel,
				question,
				title, dialogButton);
		return dialogResult;
	}

	// ======================================================================================================
	class commentBlockPatch {
		String line1 = "Patch: " + spcFileName;
		String line2 = "SpinCAD Designer version: " + buildNum ;
		String line3 = "Pot 0: ";
		String line4 = "Pot 1: ";
		String line5 = "Pot 2: ";

		JFrame commentFrame = new JFrame("Patch Information");

		JTextField line1text = new JTextField(line1, 64);
		JTextField line2text = new JTextField(line2, 64);
		JTextField line3text = new JTextField(line3, 64);
		JTextField line4text = new JTextField(line4, 64);
		JTextField line5text = new JTextField(line5, 64);
		JTextField line6text = new JTextField("", 64);
		JTextField line7text = new JTextField("", 64);

		public commentBlockPatch() {
			commentFrame.setLayout(new BoxLayout(commentFrame.getContentPane(), BoxLayout.Y_AXIS));

			line1text.setEditable(false);
			commentFrame.add(line1text);
			line2text.setEditable(false);
			commentFrame.add(line2text);
			commentFrame.add(line3text);
			commentFrame.add(line4text);
			commentFrame.add(line5text);
			commentFrame.add(line6text);
			commentFrame.add(line7text);
		}

		private void show() {
			commentFrame.setAlwaysOnTop(true);
			commentFrame.pack();
			commentFrame.setLocation(200, 150);
			commentFrame.setResizable(false);
			commentFrame.setVisible(true);
		}

		public void updateFileName() {
			line1text.setText("Patch Name: " + spcFileName);
		}

		private void clearComments() {
			line1text.setText("Patch: untitled");
			line3text.setText(line3);
			line4text.setText(line4);
			line5text.setText(line5);
			line6text.setText("");
			line7text.setText("");
		}

		// for writing out to clipboard, etc.
		public String getComments() {
			return 	"; " + line1text.getText() + "\n" +
					"; " + line2text.getText() + "\n" +
					"; " + line3text.getText() + "\n" +
					"; " + line4text.getText() + "\n" +
					"; " + line5text.getText() + "\n" +
					"; " + line6text.getText() + "\n" +
					"; " + line7text.getText() + "\n";
		}
	}

	// ======================================================================================================
	class commentBlockBank {
		String line1 = "Bank: " + spcFileName;
		String line2 = "SpinCAD Designer version: " + buildNum ;
		String line3 = "Pot 0: ";
		String line4 = "Pot 1: ";
		String line5 = "Pot 2: ";

		JFrame commentFrame = new JFrame("Patch Information");

		JTextField line1text = new JTextField(line1, 64);
		JTextField line2text = new JTextField(line2, 64);
		JTextField line3text = new JTextField("", 64);
		JTextField line4text = new JTextField("", 64);
		JTextField line5text = new JTextField("", 64);
		JTextField line6text = new JTextField("", 64);
		JTextField line7text = new JTextField("", 64);

		public commentBlockBank() {
			commentFrame.setLayout(new BoxLayout(commentFrame.getContentPane(), BoxLayout.Y_AXIS));

			line1text.setEditable(false);
			commentFrame.add(line1text);
			line2text.setEditable(false);
			commentFrame.add(line2text);
			commentFrame.add(line3text);
			commentFrame.add(line4text);
			commentFrame.add(line5text);
			commentFrame.add(line6text);
			commentFrame.add(line7text);
		}

		private void show() {
			commentFrame.setAlwaysOnTop(true);
			commentFrame.pack();
			commentFrame.setLocation(200, 150);
			commentFrame.setResizable(false);
			commentFrame.setVisible(true);
		}

		public void updateFileName() {
			line1text.setText("Patch Name: " + spcFileName);
		}

		private void clearComments() {
			line1text.setText("Bank: untitled");
			line3text.setText("");
			line4text.setText("");
			line5text.setText("");
			line6text.setText("");
			line7text.setText("");
		}

		// for writing out to clipboard, etc.
		public String getComments() {
			return 	"; " + line1text.getText() + "\n" +
					"; " + line2text.getText() + "\n" +
					"; " + line3text.getText() + "\n" +
					"; " + line4text.getText() + "\n" +
					"; " + line5text.getText() + "\n" +
					"; " + line6text.getText() + "\n" +
					"; " + line7text.getText() + "\n";
		}
	}

	// ======================================================================================================
	class simControlToolBar extends JToolBar implements ActionListener,
	ChangeListener {

		/**
		 * 
		 */
		private static final long serialVersionUID = -4298199583629847984L;
		final JButton btnStartSimulation = new JButton("Start Simulation");
		final JButton btnSigGen = new JButton("Sig Gen Sim");

		final JSlider pot0Slider = new JSlider(0, 100, 1);
		final JSlider pot1Slider = new JSlider(0, 100, 1);
		final JSlider pot2Slider = new JSlider(0, 100, 1);

		public simControlToolBar() {
			super();
			this.add(btnStartSimulation);
			btnStartSimulation.addActionListener(this);
			// this.add(btnSigGen);
			// btnSigGen.addActionListener(this);

			this.add(pot0Slider);
			pot0Slider.addChangeListener(this);
			pot0Slider.setToolTipText("Pot 0");

			this.add(pot1Slider);
			pot1Slider.addChangeListener(this);
			pot1Slider.setToolTipText("Pot 1");

			this.add(pot2Slider);
			pot2Slider.addChangeListener(this);
			pot2Slider.setToolTipText("Pot 2");

			// this.setVisible(true);
			if (sim != null) {
				pot0Slider.setValue((int) Math.round((sim.getPot(0) * 100.0)));
				pot1Slider.setValue((int) Math.round((sim.getPot(1) * 100.0)));
				pot2Slider.setValue((int) Math.round((sim.getPot(2) * 100.0)));
			}
		}

		public void stateChanged(ChangeEvent e) {
			if (e.getSource() == pot0Slider) {
				pot0Level = (double) pot0Slider.getValue() / 100.0;
				pot0Slider.setToolTipText("Pot 0: " + pot0Level);
				//				System.out.println("Pot 0: " + pot0Level);
				if (sim != null)
					sim.setPot(0, pot0Level);
			} else if (e.getSource() == pot1Slider) {
				pot1Level = (double) pot1Slider.getValue() / 100.0;
				pot1Slider.setToolTipText("Pot 1: " + pot1Level);
				//				System.out.println("Pot 1: " + pot1Level);
				if (sim != null)
					sim.setPot(1, pot1Level);
			} else if (e.getSource() == pot2Slider) {
				pot2Level = (double) pot2Slider.getValue() / 100.0;
				pot2Slider.setToolTipText("Pot 2: " + pot2Level);
				if (sim != null)
					sim.setPot(2, pot2Level);
			}
		}

		public void actionPerformed(ActionEvent arg0) {
			if (arg0.getSource() == btnStartSimulation) {
				if (isSimRunning() == true) {
					setSimRunning(false);
					loggerPanel.setVisible(false);
					levelMonitor.setVisible(false);;
					btnStartSimulation.setText("Start Simulator");
					sim.stopSimulator();
				} else {
					String testWavFileName = checkSimulatorFile();
					if(testWavFileName != "Not found!") {
						setSimRunning(true);
						// create file
						btnStartSimulation.setText("Stop Simulator");
						pb.update();
						sim = new SpinSimulator(SpinCADModel.getRenderBlock(),
								testWavFileName, outputFile, pot0Level, pot1Level,
								pot2Level);
						// loggerPanel.setVisible(loggerIsVisible);
						if(loggerIsVisible) {
							sim.showLevelLogger(loggerPanel);
							//							sim.showLevelMeter(levelMonitor);
						}
						//					sim.showLevelMeter();
						sim.start();
					}
					else { 
						MessageBox("Simulator file not found.", "Please set a simulator source file.");
					} 
				}
			} else if (arg0.getSource() == btnSigGen) {
				if (isSimRunning() == true) {
					setSimRunning(false);
					btnSigGen.setText("Start Signal");
					sim.stopSimulator();
				} else {
					//					String outputFile = null; // play out through the sound card
					setSimRunning(true);
					btnSigGen.setText("Stop Signal");
					//					SignalGenerator SigGen = new SignalGenerator();
				}
			}
		} 
	}

	// ======================================================================================================
	class bankPanel extends JPanel implements ActionListener {

		/**
		 * 
		 */
		private static final long serialVersionUID = -4298199583629847984L;
		final JButton btnPatch0 = new JButton("Patch 0");
		final JButton btnPatch1 = new JButton("Patch 1");
		final JButton btnPatch2 = new JButton("Patch 2");
		final JButton btnPatch3 = new JButton("Patch 3");
		final JButton btnPatch4 = new JButton("Patch 4");
		final JButton btnPatch5 = new JButton("Patch 5");
		final JButton btnPatch6 = new JButton("Patch 6");
		final JButton btnPatch7 = new JButton("Patch 7");

		public bankPanel() {
			super();
			Dimension minButtonSize = new Dimension(100, 20);
			Dimension buttonSize = new Dimension(180, 20);

			this.add(btnPatch0);
			btnPatch0.setPreferredSize(buttonSize);
			btnPatch0.setMinimumSize(minButtonSize);
			btnPatch0.addActionListener(this);

			this.add(btnPatch1);
			btnPatch1.setMinimumSize(minButtonSize);
			btnPatch1.setPreferredSize(buttonSize);
			btnPatch1.addActionListener(this);

			this.add(btnPatch2);
			btnPatch2.setMinimumSize(minButtonSize);
			btnPatch2.setPreferredSize(buttonSize);
			btnPatch2.addActionListener(this);

			this.add(btnPatch3);
			btnPatch3.setMinimumSize(minButtonSize);
			btnPatch3.setPreferredSize(buttonSize);
			btnPatch3.addActionListener(this);

			this.add(btnPatch4);
			btnPatch4.setMinimumSize(minButtonSize);
			btnPatch4.setPreferredSize(buttonSize);
			btnPatch4.addActionListener(this);

			this.add(btnPatch5);
			btnPatch5.setMinimumSize(minButtonSize);
			btnPatch5.setPreferredSize(buttonSize);
			btnPatch5.addActionListener(this);

			this.add(btnPatch6);
			btnPatch6.setMinimumSize(minButtonSize);
			btnPatch6.setPreferredSize(buttonSize);
			btnPatch6.addActionListener(this);

			this.add(btnPatch7);
			btnPatch7.setMinimumSize(minButtonSize);
			btnPatch7.setPreferredSize(buttonSize);
			btnPatch7.addActionListener(this);
		}

		public void actionPerformed(ActionEvent arg0) {
			if (arg0.getSource() == btnPatch0) {
				bankIndex = 0;
			}
			else if (arg0.getSource() == btnPatch1) {
				bankIndex = 1;
			}
			else if (arg0.getSource() == btnPatch2) {
				bankIndex = 2;
			}
			else if (arg0.getSource() == btnPatch3) {
				bankIndex = 3;
			}
			else if (arg0.getSource() == btnPatch4) {
				bankIndex = 4;
			}
			else if (arg0.getSource() == btnPatch5) {
				bankIndex = 5;
			}
			else if (arg0.getSource() == btnPatch6) {
				bankIndex = 6;
			}
			else if (arg0.getSource() == btnPatch7) {
				bankIndex = 7;
			}
			
			if(bank[bankIndex] != null) {
				model = bank[bankIndex];
				contentPane.repaint();
			}
			else {
//				fileNewPatch(panel, mntmNewFile);
				bank[bankIndex] = new SpinCADModel();
				bank[bankIndex].newModel();
				contentPane.repaint();
			}
		}
	}


	// ================= used for the status toolbar and simulator start/stop
	// button
	public class EditResourcesToolBar extends JToolBar implements ActionListener {
		/**
		 * 
		 */
		private static final long serialVersionUID = -8905757462245337214L;
		final JLabel pinName = new JLabel("");

		class Task extends SwingWorker<Void, Void> {
			/*
			 * Main task. Executed in background thread.
			 */
			@Override
			public Void doInBackground() {
				// Sleep for at least one second to simulate "startup".
				try {
					Thread.sleep(200);
				} catch (InterruptedException ignore) {
				}
				done();
				return null;
			}
			/*
			 * Executed in event dispatch thread
			 */
			public void done() {
			}
		}

		public EditResourcesToolBar() {
			super();
			add(pinName);
			setVisible(true);
		}

		/**
		 * Invoked when the user presses the start button.
		 */
		public void actionPerformed(ActionEvent evt) {
			System.out.println("Print: ");

		}

		public void update() {
			ActionEvent evt = null;
			actionPerformed(evt);
		}
	}

	// ======================================================================================================
	// ================= used for the status toolbar and simulator start/stop
	// button
	public class ModelResourcesToolBar extends JToolBar implements
	ActionListener {
		private static final long serialVersionUID = -8905757462245337214L;
		final JProgressBar progressBar_2 = new JProgressBar();
		final JProgressBar progressBar_1 = new JProgressBar();
		final JProgressBar progressBar = new JProgressBar();
		final JTextField ramp0Bar = new JTextField("RMP 0", 6);
		final JTextField ramp1Bar = new JTextField("RMP 1", 6);
		final JTextField sine0Bar = new JTextField("SIN 0", 6);
		final JTextField sine1Bar = new JTextField("SIN 1", 6);



		class Task extends SwingWorker<Void, Void> {
			/*
			 * Main task. Executed in background thread.
			 */
			@Override
			public Void doInBackground() {
				// Sleep for at least one second to simulate "startup".
				try {
					Thread.sleep(200);
				} catch (InterruptedException ignore) {
				}
				done();
				return null;
			}

			/*
			 * Executed in event dispatch thread
			 */
			public void done() {
			}
		}

		// ==============================================================
		// == Resources toolbar
		public ModelResourcesToolBar() {
			super();

			// Call setStringPainted now so that the progress bar height
			// stays the same whether or not the string is shown.

			progressBar_2.setToolTipText("Code Length");
			progressBar_2.setMaximum(128);
			progressBar_2.setBackground(Color.CYAN);
			progressBar_2.setString("Instructions Used");
			progressBar_2.setStringPainted(true);
			Border border = BorderFactory.createBevelBorder(BevelBorder.RAISED);
			progressBar_2.setBorder(border);

			progressBar.setToolTipText("Registers");
			progressBar.setMaximum(32);
			progressBar.setBackground(Color.CYAN);
			progressBar.setString("Registers Used");
			progressBar.setStringPainted(true);
			progressBar.setBorder(border);

			progressBar_1.setMaximum(32768);
			progressBar_1.setToolTipText("Delay RAM");
			progressBar_1.setBackground(Color.CYAN);
			progressBar_1.setString("Delay RAM Used");
			progressBar_1.setStringPainted(true);
			progressBar_1.setBorder(border);

			ramp0Bar.setHorizontalAlignment(JTextField.CENTER);
			ramp0Bar.setBackground(Color.GREEN);
			ramp0Bar.setForeground(Color.BLUE);

			ramp1Bar.setHorizontalAlignment(JTextField.CENTER);
			ramp1Bar.setBackground(Color.GREEN);
			ramp1Bar.setForeground(Color.BLUE);

			sine0Bar.setHorizontalAlignment(JTextField.CENTER);
			sine0Bar.setBackground(Color.GREEN);
			sine0Bar.setForeground(Color.BLUE);

			sine1Bar.setHorizontalAlignment(JTextField.CENTER);
			sine1Bar.setBackground(Color.GREEN);
			sine1Bar.setForeground(Color.BLUE);

			Dimension lfoBarDim = sine1Bar.getPreferredSize();

			ramp0Bar.setMaximumSize(lfoBarDim);
			ramp1Bar.setMaximumSize(lfoBarDim);
			sine0Bar.setMaximumSize(lfoBarDim);
			sine1Bar.setMaximumSize(lfoBarDim);

			add(progressBar_2);
			add(progressBar);
			add(progressBar_1);

			add(sine0Bar);
			add(sine1Bar);
			add(ramp0Bar);
			add(ramp1Bar);
		}

		/**
		 * Invoked when the user presses the start button.
		 */

		public void actionPerformed(ActionEvent evt) {
			// progressBar.setIndeterminate(true);
			int codeLength = getModel().sortAlignGen();
			System.out.println("Code: " + codeLength);

			if (codeLength < 80) {
				progressBar_2.setForeground(Color.green);
			} else if (codeLength < 105) {
				progressBar_2.setForeground(Color.yellow);
			} else if (codeLength < 128) {
				progressBar_2.setForeground(Color.orange);
			} else {
				progressBar_2.setForeground(Color.red);
			}

			progressBar_2.setValue(codeLength);

			// getModel();
			SpinCADModel.getRenderBlock();
			int nRegs = SpinFXBlock.getNumRegs() - 32;
			if (nRegs < 20) {
				progressBar.setForeground(Color.green);
			} else if (nRegs < 26) {
				progressBar.setForeground(Color.yellow);
			} else if (nRegs < 32) {
				progressBar.setForeground(Color.orange);
			} else {
				progressBar.setForeground(Color.red);
			}
			progressBar.setValue(nRegs);

			// getModel();
			int ramUsed = SpinCADModel.getRenderBlock().getDelayMemAllocated();
			if (ramUsed < 20000) {
				progressBar_1.setForeground(Color.green);
			} else if (ramUsed < 26000) {
				progressBar_1.setForeground(Color.yellow);
			} else if (ramUsed < 32768) {
				progressBar_1.setForeground(Color.orange);
			} else {
				progressBar_1.setForeground(Color.red);
			}
			progressBar_1.setValue(ramUsed);

			int rampLFO_0 = SpinCADModel.countLFOReferences("LoadRampLFO(0");
			if(rampLFO_0 == 0) {
				ramp0Bar.setBackground(Color.GREEN);
				ramp0Bar.setForeground(Color.black);
			} else if(rampLFO_0 == 1) {
				ramp0Bar.setBackground(Color.YELLOW);
				ramp0Bar.setForeground(Color.black);
			} else { 
				ramp0Bar.setBackground(Color.RED);
				ramp0Bar.setForeground(Color.white);
			}

			int rampLFO_1 = SpinCADModel.countLFOReferences("LoadRampLFO(1");
			if(rampLFO_1 == 0) {
				ramp1Bar.setBackground(Color.GREEN);
				ramp1Bar.setForeground(Color.black);
			} else if(rampLFO_1 == 1) {
				ramp1Bar.setBackground(Color.YELLOW);
				ramp1Bar.setForeground(Color.black);
			} else { 
				ramp1Bar.setBackground(Color.RED);
				ramp1Bar.setForeground(Color.white);
			}

			int sineLFO_0 = SpinCADModel.countLFOReferences("LoadSinLFO(0");
			if(sineLFO_0 == 0) {
				sine0Bar.setBackground(Color.GREEN);
				sine0Bar.setForeground(Color.black);
			} else if(sineLFO_0 == 1) {
				sine0Bar.setBackground(Color.YELLOW);
				sine0Bar.setForeground(Color.black);
			} else { 
				sine0Bar.setBackground(Color.RED);
				sine0Bar.setForeground(Color.white);
			}

			int sineLFO_1 = SpinCADModel.countLFOReferences("LoadSinLFO(1");
			if(sineLFO_1 == 0) {
				sine1Bar.setBackground(Color.GREEN);
				sine1Bar.setForeground(Color.black);
			} else if(sineLFO_1 == 1) {
				sine1Bar.setBackground(Color.YELLOW);
				sine1Bar.setForeground(Color.black);
			} else { 
				sine1Bar.setBackground(Color.RED);
				sine1Bar.setForeground(Color.white);
			}
		}

		public void update() {
			ActionEvent evt = null;
			actionPerformed(evt);
		}
	}

	ModelResourcesToolBar getResourceToolbar() {
		return pb;
	}

	// ===================================================
	// == Sample rate combo box
	public class SampleRateComboBox extends JFrame {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		/**
		 * 
		private static final long serialVersionUID = 1L;
		 */
		JComboBox<Object> rateList = null;

		public SampleRateComboBox() {
			super("Sample Rate");
			createAndShowGUI();
		}

		/** Listens to the combo box. */
		class SampleRateListener implements ActionListener {
			public void actionPerformed(ActionEvent e) {
				JComboBox<String> cb = (JComboBox<String>) e.getSource();
				String rate = (String) cb.getSelectedItem();
				if (rate == "32768") {
					ElmProgram.setSamplerate(32768);
				} else if (rate == "44100") {
					ElmProgram.setSamplerate(44100);
				} else if (rate == "48000") {
					ElmProgram.setSamplerate(48000);
				}
			}
		}

		/**
		 * Create the GUI and show it. For thread safety, this method should be
		 * invoked from the event-dispatching thread.
		 */
		private void createAndShowGUI() {
			// Create and set up the window.
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {

					// Create and set up the content pane.
					JPanel newContentPane = new JPanel(new BorderLayout());
					setContentPane(newContentPane);
					newContentPane.setOpaque(true); // content panes must be
					// opaque
					String[] rateStrings = { "32768", "44100", "48000" };

					// Create the combo box, select the item at index 4.
					// Indices start at 0, so 4 specifies the pig.
					rateList = new JComboBox<Object>(rateStrings);
					if (ElmProgram.SAMPLERATE == 44100) {
						rateList.setSelectedIndex(1);

					} else if (ElmProgram.SAMPLERATE == 48000) {
						rateList.setSelectedIndex(2);

					} else
						rateList.setSelectedIndex(0);
					rateList.addActionListener(new SampleRateListener());

					// Lay out the demo.
					newContentPane.add(rateList, BorderLayout.PAGE_START);
					newContentPane.setBorder(BorderFactory.createEmptyBorder(
							20, 20, 20, 20));

					// Display the window.
					pack();
					setVisible(true);
					setResizable(false);
				}
			});
		}
	}

	public static void openWebpage(URI uri) {
		Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
		if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
			try {
				desktop.browse(uri);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static void openWebpage(URL url) {
		try {
			openWebpage(url.toURI());
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}
}
