package it.unitn.disi.logcompliance.prediction.tools.objects;

public class Valutation {
	short truePositive,trueNegative,falsePositive,falseNegative,wrongAnnotation;

	
	public Valutation(short TP,short TN,short FP, short FN, short wrongAnnotation) {
		truePositive = TP;
		trueNegative = TN;
		falsePositive = FP;
		falseNegative = FN;
		this.wrongAnnotation = wrongAnnotation;
	}
	public Valutation() {
		truePositive = 0;
		trueNegative = 0;
		falsePositive = 0;
		falseNegative = 0;
		wrongAnnotation = 0;
	}
	public void plus(Valutation toAdd) {
		truePositive+=toAdd.getTruePositive();
		trueNegative+=toAdd.getTrueNegative();
		falsePositive+=toAdd.getFalsePositive();
		falseNegative+=toAdd.getFalseNegative();
		wrongAnnotation+=toAdd.getWrongAnnotation();
	}
	@Override
	public String toString() {
		return "TP = "+truePositive+"\nTN = "+trueNegative+"\nFP = "+falsePositive+"\nFN = "+falseNegative+"\nSensitivity = "+getSensitivity()+"\nPrecision = "+getPrecision()+"\nAccuracy = "+getAccuracy()+"\nF1-score = "+getF1Score()+"\nMMC = "+getMcc();
	}
	public void setTP() {
		truePositive++;
	}
	public void setTN() {
		trueNegative++;
	}
	public void setFP() {
		falsePositive++;
	}
	public void setFN() {
		falseNegative++;
	}
	public void setWrongAnnotation() {
		wrongAnnotation++;
	}
	public short getTruePositive() {
		return truePositive;
	}
	public short getTrueNegative() {
		return trueNegative;
	}
	public short getFalsePositive() {
		return falsePositive;
	}
	public short getFalseNegative() {
		return falseNegative;
	}
	public short getWrongAnnotation() {
		return wrongAnnotation;
	}
	public float getAccuracy() {
		return (truePositive + trueNegative)/(truePositive + trueNegative + falsePositive + falseNegative);
	}
	public float getPrecision() {
		return truePositive/(truePositive+falsePositive);
	}
	public float getSensitivity() {
		return truePositive/(truePositive+falseNegative);
	}
	public float getF1Score() {
		return (2*getSensitivity()*getPrecision())/(getSensitivity()+getPrecision());
	}
	public float getMcc() {
		return (float) (((truePositive*trueNegative)-(falsePositive*falseNegative))/Math.sqrt((truePositive+falsePositive)*(truePositive+falseNegative)*(trueNegative+falsePositive)*(trueNegative+falseNegative)));
	}
	
}
