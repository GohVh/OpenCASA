package spermAnalysis;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import javax.swing.JFileChooser;

import data.Params;
import data.Spermatozoon;
import data.Trial;
import gui.MainWindow;
import ij.IJ;
import ij.ImagePlus;
import ij.measure.ResultsTable;
import plugins.AVI_Reader;
import utils.ImageProcessing;
import utils.Output;
import utils.TrackFilters;

public class ChemotaxisAnalysis {
	
	private File directory;
	
	/**
	 * Constructor
	 */
	public ChemotaxisAnalysis(){}
	
	/**
	 * 
	 * @return File[]
	 */
	public File[] getFileNames(){
		JFileChooser chooser = new JFileChooser();
		chooser.setCurrentDirectory(new java.io.File("F:\\VIDEOS QUIMIOTAXIS\\Validacion Quiron"));
		chooser.setDialogTitle("choosertitle");
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setAcceptAllFileFilterUsed(false);
		if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
		  //System.out.println("Directory: "+chooser.getSelectedFile());
		  File folder = chooser.getSelectedFile();
		  File[] listOfFiles = folder.listFiles();
		  directory = chooser.getSelectedFile();
		  return listOfFiles;	
		}
		return null;
	}	
	
	/**
	 * 
	 * @param ImagePlus imp
	 */
	public List analyze(ImagePlus imp,String filename){
		
		System.out.println("converToGrayScale...");
 		ImageProcessing.convertToGrayscale(imp);
		//************************************************************ 
		// * Auto-Threshold Video
		//************************************************************
 		System.out.println("thresholdStack...");
		ImageProcessing.thresholdStack(imp);
		//************************************************************ 
		// * Record particle positions for each frame in an ArrayList
		//************************************************************
		System.out.println("detectSpermatozoa...");
		List[] theParticles = ImageProcessing.detectSpermatozoa(imp);
		//************************************************************ 
		// * Now assemble tracks out of the particle lists
		// * Also record to which track a particle belongs in ArrayLists
		//************************************************************
		System.out.println("identifyTracks...");
		List theTracks = ImageProcessing.idenfityTracks(theParticles,imp.getStackSize());
		//************************************************************ 
		// * Filter the tracks list
		// * (We have to filter the tracks list because not all of them are valid)
		//************************************************************
		System.out.println("filterTracks...");
		theTracks = TrackFilters.filterTracks(theTracks);
		//************************************************************
		// * Average the tracks 
		//************************************************************
		System.out.println("averageTracks...");
		List avgTracks = TrackFilters.averageTracks(theTracks);	
		//************************************************************ 
		// * Calculate output
		//************************************************************
		// XY Coordinates
		String xyCoordsOutput = ""; 
		if(Params.printXY){
			xyCoordsOutput = Output.printXYCoords(theTracks);
			IJ.saveString(xyCoordsOutput,"");		
		}
		
		switch(ChFunctions.getTrialType(filename)){
		
			case 0://control
					Params.controlTracks.addAll(theTracks);
					System.out.println("theTracks size: "+theTracks.size()+"; controlTracks size: "+Params.controlTracks.size());
				break;
			case 1: //10pM
					Params.conditionTracks.addAll(theTracks);
					System.out.println("theTracks size: "+theTracks.size()+"; conditionTracks size: "+Params.conditionTracks.size());
				break;
//			case 2: //100pM
//					Params.conditionTracks.addAll(theTracks);
//				break;
//			case 3:
//					Params.conditionTracks.addAll(theTracks);
//				break;
		}
		
		
		
		
		//To calculate Ratio-Q we have to decimate all tracks
		System.out.println("decimateTracks...");
		List decimatedTracks = TrackFilters.decimateTracks(avgTracks,Params.decimationFactor);
		// Chemotaxis ratios
		System.out.println("CalculateRatioQ...");
		float ratioQ = ChFunctions.calculateRatioQ(avgTracks);
		System.out.println("calculateRatioSL...");
		float ratioSL = ChFunctions.calculateRatioSL(avgTracks);
		
		System.out.println("setQtResults...");
		ChFunctions.setQtResults(filename,ratioQ,ratioSL,theTracks.size());
		Params.rTable.show("Ratios Quimiotaxis");
		//************************************************************ 
		// * Draw tracks at each frame
		//************************************************************
		//ImageProcessing.draw(imp,theTracks,avgTracks,ratioQ,ratioSL);
		System.out.println("Analysis finished for video: "+filename);
		
		
//		// Motility results
//		ResultsTable motResults = Output.calculateMotility(theTracks);
//		if(Params.calcMotilityParameters){
//			motResults.show("Motility Resume");
//		}
//		if(Params.calcMeanMotilityParameters){
//			ResultsTable avgMotility = Output.calculateAverageMotility(theTracks.size());
//			avgMotility.show("Motility Average");
//		}
		
		return theTracks;
	}
	
	/**
	 * 
	 * @param String filename
	 */
	public boolean isAVI (String filename){
		String[] parts = filename.split("\\.");
		if(parts[1].equals("avi"))
			return true;
		else 
			return false;
	}
	
	/**
	 * 
	 * @param MainWindow mw
	 */
	public void run(MainWindow mw){
		mw.setVisible(false);
		Map<String, Trial> trials = new HashMap<String, Trial>();	
		
		File[] listOfFiles = getFileNames();
		if(listOfFiles!=null){
			for (int i = 0; i < listOfFiles.length; i++) {
			    if (listOfFiles[i].isFile()) {
			    	final String filename = listOfFiles[i].getName();
					//System.out.println("File " + filename);
					if(isAVI(filename)){
						System.out.println("Loading video...");
						
						int trialType = ChFunctions.getTrialType(filename);
						String trialID = ChFunctions.getID(filename);
						
						switch(trialType){
						case 0: //Control
						case 1: //10pM
//						case 2: //100pM
//						case 3: //10nM
							
							AVI_Reader ar = new  AVI_Reader();
							ar.run(directory+"\\"+listOfFiles[i].getName());
							final ImagePlus imp = ar.getImagePlus();
							List t = analyze(imp,filename);
							
//							if(t==null)
//								IJ.log("analyze devuelve NULL");
							
							Trial tr;
							if(trials.get(trialID)!=null) 
								tr = trials.get(trialID);
							else 
								tr = new Trial();
							switch(trialType){
								case 0: tr.control=t;break;
								case 1: tr.p10pM=t;break;
								case 2: tr.p100pM=t;break;
								case 3: tr.p10nM=t;break;
							}
							trials.put(trialID, tr);

							//new Thread(new Runnable() {public void run() {analyze(imp,filename);}}).start();							
						}

					}else{
						System.out.println("The file format is not AVI. Not analyzed");
					}

			    } else if (listOfFiles[i].isDirectory()) {
			    	System.out.println("Directory " + listOfFiles[i].getName());
			    }
		   }
			
		  double thControl = ChFunctions.calculateORControlThreshold();
		  Set keySet = trials.keySet();
		  for (Iterator k=keySet.iterator();k.hasNext();) {
			  String key= (String)k.next();
			  Trial trial = (Trial)trials.get(key);
			  System.out.println("key: "+key);
			  double OR = ChFunctions.OR(trial,"p10pM");
			  if(OR>thControl)
				  IJ.log("POSITIVO: OR["+OR+"] - thControl["+thControl+"]");
			  else
			  	  IJ.log("NEGATIVO: OR["+OR+"] - thControl["+thControl+"]");
		  }
		}
	}
}