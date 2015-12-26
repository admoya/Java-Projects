/*
 * Generator class will do the work. 
 * Read from header table, generate data, write to detail table.
 */

import java.util.ArrayList;
import java.util.Random;
import java.io.IOException;
import jxl.write.*;
import jxl.write.biff.RowsExceededException;
import jxl.read.biff.BiffException;

import java.sql.*;
public class Generator {

	public static void main(String[] args) throws BiffException, IOException, RowsExceededException, WriteException, SQLException {
		ArrayList<Distribution> distributions = new ArrayList<Distribution>();
		int size = 0;
		//Reading Header Table and saving parameters
		 // JDBC driver name and database URL
		   String JDBC_DRIVER = "com.mysql.jdbc.Driver";  
		   String DB_URL = "jdbc:mysql://localhost/gct";
		//  Database credentials
		   String USER = "root";
		   String PASS = "root";
		   
		   Connection conn = null;
		   Statement stmt = null;
		   try{
			      //STEP 1: Register JDBC driver
			      Class.forName(JDBC_DRIVER);

			      //STEP 2: Open a connection
			      System.out.println("Connecting to database...");
			      conn = DriverManager.getConnection(DB_URL,USER,PASS);
			      
			      //STEP 3: Execute a query
			      System.out.println("Creating statement...");
			      stmt = conn.createStatement();
			      String sql;
			      sql = "SELECT * FROM import_data_header";
			      ResultSet rs = stmt.executeQuery(sql);

			      //STEP 4: Extract data from result set
			      while(rs.next()){
			         //Retrieve by column name
			         int id  = rs.getInt("Dist_ID");
			         String name = rs.getString("Name");
			         String distType = rs.getString("Dist_Type");
			         size = rs.getInt("Size");
			         int corID = rs.getInt("Cor_ID");
			         double mean = rs.getDouble("Mean");
			         double sd = rs.getDouble("Std_Dev");
			         double min = rs.getDouble("Min");
			         double max = rs.getDouble("Max");
			         double topPercent = rs.getDouble("Top_Percent");
			         double corPercent = rs.getDouble("Cor_Percent");
			         double normPercent = rs.getDouble("Norm_Percent");
			         double topMean = rs.getDouble("Top_Mean");
			         double topSD = rs.getDouble("Top_Std_Dev");
			         double normMean = rs.getDouble("Norm_Mean");
			         double normSD = rs.getDouble("Norm_Std_Dev");
			         double falseChance = rs.getDouble("False_Chance");
			         double falseVal = rs.getDouble("False_Value");
			         double trueChance = rs.getDouble("True_Chance");
			         double trueMean = rs.getDouble("True_Mean");
			         double trueSD = rs.getDouble("True_Std_Dev");

			         //Display values
			         System.out.print("ID: " + id);
			         System.out.print(", Name: " + name);
			         System.out.print(", Type: " + distType);
			         System.out.print(", Size: " + size);
			         System.out.print(", CorID: " + corID);
			         System.out.print(", Mean: " + mean);
			         System.out.print(", SD: " + sd);
			         System.out.print(", Min: " + min);
			         System.out.print(", Max: " + max);
			         System.out.print(", TopPercent: " + topPercent);
			         System.out.print(", CorPercent: " + corPercent);
			         System.out.print(", NormPercent: " + normPercent);
			         System.out.print(", TopMean: " + topMean);
			         System.out.print(", TopSD: " + topSD);
			         System.out.print(", normMean: " + normMean);
			         System.out.print(", normSD: " + normSD);
			         System.out.print(", falseChance: " + falseChance);
			         System.out.print(", falseVal: " + falseVal);
			         System.out.print(", trueChance: " + trueChance);
			         System.out.print(", trueMean: " + trueMean);
			         System.out.println(", trueSD: " + trueSD);
			         
			         switch(distType) //Determine the distribution and generate data accordingly
			         {
			         case ("Normal"):
			        	 distributions.add(newBinaryDistribution(id, size, name, mean, sd));
			         	break;
			         case ("SBinCor"):
			        	 distributions.add(TopPercentBinaryCorrelation(id, name, topPercent, corPercent, normPercent, getDistFromID(corID, distributions)));
			         	break;
			         case ("SNumCor"):
			        	 distributions.add(TopPercentNumericalCorrelation(id, name, topPercent, topMean, topSD, normMean, normSD, getDistFromID(corID, distributions)));
			         	break;
			         case ("Bounded"):
			        	 distributions.add(newBoundedDistribution(id, size, name, normMean, normSD, min, max));
			         	break;
			         case ("Dynamic"):
			        	 distributions.add(newDynamicBinaryCorrelation(id, name, falseChance, falseVal, trueChance, trueMean, trueSD, getDistFromID(corID, distributions)));
			         	break;
			         }
			      }
			      //Populate detail table
			      /* Old Method
			      for (Distribution d : distributions)
					{
						for(Data data : d.getValues())
						{
						String query = "Insert into import_data_detail (Dist_ID, Subset_ID, Value)" + " values (?, ?, ?)";
					      PreparedStatement preparedStmt = conn.prepareStatement(query);
					      preparedStmt.setInt(1, d.getID());
					      preparedStmt.setInt(2, data.ID);
					      preparedStmt.setDouble(3, data.value);
					      preparedStmt.execute();
						}
					      
					}*/
			      
			      //Clear Athlete Table
			      String clear = "TRUNCATE athlete";
			      PreparedStatement truncate = conn.prepareStatement(clear);
			      truncate.execute();
			      
			      //Populate athlete table
			      for (int i = 0; i < size; i++)
			      {
			    	  String query = "Insert into athlete (Athlete_ID, Dist_1, Dist_2, Dist_3, Dist_4, Dist_5, Dist_6, Dist_7, Dist_8, Dist_9, Dist_10)" + " values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
			    	  PreparedStatement preparedStmt = conn.prepareStatement(query);
			    	  preparedStmt.setInt(1, i);
			    	  int j;
			    	  for (j = 0; j < distributions.size(); j++)
			    	  {
			    		  double value = distributions.get(j).getData(i);
			    		  preparedStmt.setDouble(j+2, value);
			    	  }
			    	  for (int k = j + 2; k < 12; k++)
			    	  {
			    		  preparedStmt.setNull(k, java.sql.Types.DOUBLE);
			    	  }
			    	  preparedStmt.execute();
			      }
			      
			      //STEP 6: Clean-up environment
			      rs.close();
			      stmt.close();
			      conn.close();
			   }catch(SQLException se){
			      //Handle errors for JDBC
			      se.printStackTrace();
			   }catch(Exception e){
			      //Handle errors for Class.forName
			      e.printStackTrace();
			   }finally{
			      //finally block used to close resources
			      try{
			         if(stmt!=null)
			            stmt.close();
			      }catch(SQLException se2){
			      }// nothing we can do
			      try{
			         if(conn!=null)
			            conn.close();
			      }catch(SQLException se){
			         se.printStackTrace();
			      }//end finally try
			   }//end try
		   System.out.println("Done.");
		
	}
	
	/* ************************************** Generator Methods ******************************* */
	
	private static Distribution getDistFromID(int ID, ArrayList<Distribution> dists) //Search for a particular distribution
	{
		for (Distribution d : dists)
			if (d.getID() == ID)
				return d;
		return null;
	}
	
	 private static Distribution newBinaryDistribution(int ID, int size, String name, double mean, double sd)//Create a new normal distribution
	{
		Distribution retVal = new Distribution(ID, name, "Normal", size, mean, sd);
		//ArrayList<Double> retList = new ArrayList<Double>();
		Random generator = new Random();
		for(int i = 0; i < size; i++)
		{
			retVal.addData(i, (generator.nextGaussian()*sd+mean));
		}
		
		return retVal;
	}

	 private static Distribution TopPercentBinaryCorrelation(int ID, String name, double topPercent, double corPercent, double normPercent, Distribution inData)//Create a correlated binary (as in 1/0, not statistically normal) distribution
	 {
		 Distribution retVal = new Distribution(ID, name, "BCor", inData.getSize(), topPercent, corPercent, normPercent);
		 //ArrayList<Integer> retList = new ArrayList<Integer>();
		 inData.sort();
		 int topN = (int) (inData.getSize() * topPercent);
		 
		 int i = 0;
		 Random generator = new Random();
		 for (Data d : inData.getValues())
		 {
			 if (i < inData.getSize() - topN)
			 {
				 if (generator.nextDouble()*1 <= normPercent)
					 retVal.addData(d.ID, 1);
				 else retVal.addData(d.ID, 0);
			 }
			 else
			 {
				 if (generator.nextDouble()*1 <= corPercent)
					 retVal.addData(d.ID, 1);
				 else retVal.addData(d.ID, 0);
			 }
			 i++;
		 }
		 
		 
		 return retVal;
	 }
	 
	 private static Distribution TopPercentNumericalCorrelation (int ID, String name, double topPercent, double topMean, double topSD, double norMean, double norSD, Distribution inData)//Create a correlated numerical distribution
				{
		 			Distribution retVal = new Distribution(ID, name, "NumCor", inData.getSize(), topPercent, norMean, norSD, topMean, topSD);
		 			inData.sort();
		 			int topN = (int) (inData.getSize() * topPercent);
		 			int i = 0;
		 			Random generatorLo = new Random();
		 			Random generatorHi = new Random();
		 			for (Data d : inData.getValues())
		 			{
		 				if (i < inData.getSize() - topN)
		 				{
		 					retVal.addData(d.ID, generatorLo.nextGaussian()*norSD+norMean);
		 				}
		 				else 
		 				{
		 					retVal.addData(d.ID, generatorHi.nextGaussian()*topSD+topMean);
		 				}
		 				i++;
		 			}
		 			
		 			return retVal;
				}

	 private static Distribution newBoundedDistribution(int ID, int size, String name, double mean, double sd, double min, double max) //Create a bounded normal. Can be used to create skewed distributions
		{
			Distribution retVal = new Distribution(ID, name, "Bounded", size, mean, sd);
			//ArrayList<Double> retList = new ArrayList<Double>();
			Random generator = new Random();
			for(int i = 0; i < size; i++)
			{
				double val = (generator.nextGaussian()*sd+mean);
				while (val < min || val > max)
				{
					val = (generator.nextGaussian()*sd+mean);
				}
				retVal.addData(i,  val);
			}
			
			return retVal;
		}
	 
	 private static Distribution newDynamicBinaryCorrelation(int ID, String name, double falseChance, double falseVal, double trueChance, double trueMean, double trueSD, Distribution inData)
	 {
		 Distribution retVal = new Distribution(ID, inData.getSize(), name, "Dynamic", falseChance, falseVal, trueChance, trueMean, trueSD);
		 Random generator = new Random();
		 for (Data d : inData.getValues()){
			 if (d.value == 0)//This row is false
			 {
				if(generator.nextDouble() <= falseChance){ //We have a hit
					double tmpValue = generator.nextGaussian()*inData.getTopSD()+inData.getTopMean(); //Generate a new value with same values as other distribution
					retVal.addData(d.ID, tmpValue);
				}
				else
					retVal.addData(d.ID, falseVal);
			 }
			 else{ //This row is true
				 if(generator.nextDouble() <= falseChance){ //We have a hit
						double tmpValue = generator.nextGaussian()*inData.getSD()+inData.getMean(); //Generate a new value with same values as other distribution
						retVal.addData(d.ID, tmpValue);
					}
				 else if (generator.nextDouble() <= trueChance){ // We have a hit
					 double offset = generator.nextGaussian()*trueSD+trueMean;
					 double tmpValue = d.value + offset;
					 retVal.addData(d.ID, tmpValue);
				 }
				 else
					 retVal.addData(d.ID, falseVal); 
			 }
		 }
		 
		 return retVal;
	 }
}

	