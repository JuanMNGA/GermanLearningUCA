package es.uca.tabu;

import android.content.Context;

public class StatisticsManager {
	
	private double IRatio;
	private String IWorstCat;
	private int IMostPlayedLevel;
	private String IBestCat;
	private int IPlayed;
	private double IAVGDefRating;
	private double IDefRate;
	private int INumOfReports;
	
	private static StatisticsManager instance = null;
	
	private static Context c = null;

	public static StatisticsManager getInstance() {
		if(instance == null)
			instance =  new StatisticsManager();
		return instance;
	}
	
	public static StatisticsManager getInstance(Context c2) {
		if(instance == null) {
			instance = new StatisticsManager();
		}
		c = c2;
		return instance;
	}
	
	public static void reset() {
		instance = null;
	}
	
	public static boolean isAlive() {
		return instance != null;
	}
	
	private StatisticsManager() {}

	public double getIRatio() {
		return IRatio;
	}

	public void setIRatio(double iRatio) {
		IRatio = iRatio;
	}

	public String getIWorstCat() {
		return IWorstCat;
	}

	public void setIWorstCat(String iWorstCat) {
		IWorstCat = iWorstCat;
	}

	public int getIMostPlayedLevel() {
		return IMostPlayedLevel;
	}

	public void setIMostPlayedLevel(int iMostPlayedLevel) {
		IMostPlayedLevel = iMostPlayedLevel;
	}

	public String getIBestCat() {
		return IBestCat;
	}

	public void setIBestCat(String iBestCat) {
		IBestCat = iBestCat;
	}

	public int getIPlayed() {
		return IPlayed;
	}

	public void setIPlayed(int iPlayed) {
		IPlayed = iPlayed;
	}

	public double getIAVGDefRating() {
		return IAVGDefRating;
	}

	public void setIAVGDefRating(double iAVGDefRating) {
		IAVGDefRating = iAVGDefRating;
	}

	public double getIDefRate() {
		return IDefRate;
	}

	public void setIDefRate(double iDefRate) {
		IDefRate = iDefRate;
	}

	public int getINumOfReports() {
		return INumOfReports;
	}

	public void setINumOfReports(int iNumOfReports) {
		INumOfReports = iNumOfReports;
	}
	
}
