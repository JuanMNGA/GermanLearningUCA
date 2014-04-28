package es.uca.tabu;

import java.io.Serializable;

public class Question implements Serializable {
	int mId;
	String mName;
	String mArticle;
	String mPrepalabra;
	String mPostpalabra;
	String mClueBody;
	boolean mSuccess;
	boolean mClue;
	Integer mPuntuacion;
	short mTries; 
	String mReport;
	boolean mAnswered;

	public Question(int id, String name, String article, String prepalabra, String postpalabra, String body, boolean success) {
		mId = id;
		mName = name;
		mArticle = article;
		mPrepalabra = prepalabra;
		mPostpalabra = postpalabra;
		mClueBody = body;
		mSuccess = success;
		mTries = 0;
		mClue = false;
		mReport = null;
		mPuntuacion = -1;
		mAnswered = false;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + mId;
		result = prime * result + ((mName == null) ? 0 : mName.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Question other = (Question) obj;
		if (mId != other.mId)
			return false;
		if (mName == null) {
			if (other.mName != null)
				return false;
		} else if (!mName.equals(other.mName))
			return false;
		return true;
	}


	public int getId() {
		return mId;
	}

	public void setId(int mId) {
		this.mId = mId;
	}

	public String getName() {
		return mName;
	}

	public void setName(String mName) {
		this.mName = mName;
	}

	public String getArticle() {
		return mArticle;
	}

	public void setArticle(String mArticle) {
		this.mArticle = mArticle;
	}

	public String getPrepalabra() {
		return mPrepalabra;
	}

	public void setPrepalabra(String mPrepalabra) {
		this.mPrepalabra = mPrepalabra;
	}

	public String getPostpalabra() {
		return mPostpalabra;
	}

	public void setPostpalabra(String mPostpalabra) {
		this.mPostpalabra = mPostpalabra;
	}
	
	public boolean isSuccess() {
		return mSuccess;
	}

	public void setSuccess(boolean mSuccess) {
		this.mSuccess = mSuccess;
	}

	public boolean isClue() {
		return mClue;
	}

	public void setClue(boolean mClue) {
		this.mClue = mClue;
	}
	
	public short getTries() {
		return mTries;
	}

	public void increaseTries() {
		this.mTries +=1;
	}


	public Integer getPuntuacion() {
		return mPuntuacion;
	}


	public void setPuntuacion(int i) {
		this.mPuntuacion = i;
	}


	public String getReport() {
		return mReport;
	}


	public void setReport(String mReport) {
		this.mReport = mReport;
	}
	
	public String getClue() {
		return mClueBody;
	}


	public void setClueBody(String body) {
		this.mClueBody = body;
	}


	public boolean isAnswered() {
		return mAnswered;
	}


	public void setAnswered(boolean answered) {
		this.mAnswered = answered;
	}
	
}
