package com.deft.sarcasm.postprocess;

import org.apache.log4j.Logger;

import java.io.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PRF1Measure {

	@SuppressWarnings("rawtypes")
	// for each category - maintain a hash of ref/target pairs
	private Map<Double, List<RefTargetPair>> eachCategoryMap;

	/**
	 * |selected| = true positives + false positives <br>
	 * the count of selected (or retrieved) items
	 */
	private long selected;

	/**
	 * |target| = true positives + false negatives <br>
	 * the count of target (or correct) items
	 */
	private long target;
    private static final Logger logger = Logger.getLogger(PRF1Measure.class);
	private long truePositive;

	public void setPrecRecallObject(
			HashMap<Double, List<RefTargetPair>> eachCategoryMap) {
		// TODO Auto-generated method stub
		this.eachCategoryMap = eachCategoryMap;
		
		System.out.println("length of the map is1 " + this.eachCategoryMap.keySet().size()) ;
		System.out.println("length of the map is2 " + this.eachCategoryMap.keySet().size()) ;
		
		
	}

	public String calculatePRF1() 
	{
		// TODO Auto-generated method stub
		Set<Double> categories = eachCategoryMap.keySet();
		
		StringBuffer sb = new StringBuffer();
		DecimalFormat dec = new DecimalFormat("###.##") ;
		
		
		for (Double category : categories) 
		{
			@SuppressWarnings("rawtypes")
			List<RefTargetPair> refPairList = eachCategoryMap.get(category);
			updateScores(refPairList, category);
			for (Double c : eachCategoryMap.keySet()) 
			{
				refPairList = eachCategoryMap.get(c);
				updateSelected(refPairList, category);
			}

			String out = "category:" + " " + category + " " + "precision:" + " " + dec.format(precision()) + " " + "recall:"
					+ " " + dec.format(recall()) + " " +"FMeasure:" + " " + dec.format(FMeasure());
			sb.append(out) ;
			sb.append("\n") ;
	
			selected = 0 ;
		}
		
		//macro level - 

		return sb.toString() ;
	}

	public void updateSelected(List<RefTargetPair> refPredList, double category) {
		for (RefTargetPair pair : refPredList) 
		{
			Double s =  (Double) pair.getRight() ;
			if (s.equals(category)) 
			{
				selected++;
			}
		}
	}

	public void updateScores(List<RefTargetPair> refPredList, Double category) {

		truePositive = countTruePositives(refPredList);
		target = refPredList.size();
	}

	public double precision() 
	{
		double precision =  selected > 0 ? (double) truePositive / (double) selected : 0;
		precision *= 100d ;
		return precision ;
	}

	public double recall() 
	{
		double recall =  target > 0 ? (double) truePositive / (double) target : 0;
		recall *= 100d ;
		return recall ;
	}

	public double FMeasure() 
	{
		
		if (precision() + recall() > 0) 
		{
			double f1 = 2 * (precision() * recall()) / (precision() + recall());
			return f1;
		}
		else
		{
			// cannot divide by zero, return error code
			return -1;
		}
	}

	static int countTruePositives(List<RefTargetPair> refPredList) 
	{
		int truePositives = 0;

		// Note: Maybe a map should be used to improve performance
		for (int referenceIndex = 0; referenceIndex < refPredList.size(); referenceIndex++) 
		{
			RefTargetPair refObject = refPredList.get(referenceIndex);

			Double referenceName = (Double) refObject.getLeft();
			Double predName = (Double) refObject.getRight() ;
		//		System.out.println(" ref " + referenceName + " " + "pred " + predName) ;
			if (referenceName.equals(predName)) 
			{
					truePositives++;
			}
			
			
		//	
		}
		
		
		
		return truePositives;
	}
	
	public void loadFiles ( String testFile, String opFile) throws IOException
	{
		BufferedReader reader1 = new BufferedReader ( new FileReader ( testFile)) ;
		BufferedReader reader2 = new BufferedReader ( new FileReader ( opFile)) ;
		
		
		List<RefTargetPair> refTargetPairList = null ;
		eachCategoryMap = new HashMap<Double,List<RefTargetPair>>() ;
		RefTargetPair pair = null ;


		
		while (true )
		{
			String line1 = reader1.readLine();
			String line2 = reader2.readLine();
			
			if ( line1 == null || line2 == null )
			{
				break ;
			}
			
			double reference = Double.valueOf(line1.split("\\s")[0].trim()).doubleValue() ;
			double target = Double.valueOf(line2.trim()).doubleValue();
			
			//for p/r/f1
			refTargetPairList = eachCategoryMap.get(reference);
			if ( null == refTargetPairList )
			{
				refTargetPairList = new ArrayList<RefTargetPair>();
			}
		
			pair = new RefTargetPair(reference,target);
			refTargetPairList.add(pair);
			eachCategoryMap.put((double)reference, refTargetPairList);
	
		}
		
	}
	
	public static void main (String[] args ) throws IOException
	{

		String testFile = "reextractionresults/release/data/output/temp/test_week_%d.dat.binary.svm.TESTING.txt" ;
		String svmOp  = "reextractionresults/release/data/output/new_pred/train%dtest%d.op" ;
		for(int i=1; i<=1; i++)
        {
            for(int j=1; j<=6; j++)
            {
                String testFilePath = String.format(testFile, j);
                String svmOpPath = String.format(svmOp, i, j);
                logger.info("Trained on Week "+i+", Tested on Week "+j+":");
                PRF1Measure prf1CalcObj = new PRF1Measure();
                prf1CalcObj.loadFiles(testFilePath, svmOpPath);
                String op = prf1CalcObj.calculatePRF1();
                logger.info("\t"+op);
            }
        }

	}
	

}
