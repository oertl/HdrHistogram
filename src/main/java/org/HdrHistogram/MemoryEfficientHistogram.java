package org.HdrHistogram;

public class MemoryEfficientHistogram {
	
	public static final boolean THROW_EXCEPTION_IF_OUT_OF_RANGE = true;
	
	final long[] counts;
	long underFlowCount = 0;
	long overFlowCount = 0;	
	final double factor1;
	final double factor2;
	final double offset;
	final double subNormalBinLimit;
	final double minTrackableValue;
	double min = Double.POSITIVE_INFINITY;
	double max = Double.NEGATIVE_INFINITY;
	long totalCount = 0;
	
	public MemoryEfficientHistogram(final double minimumBinSize, final double minTrackableValue, final double maxTrackableValue, final double maxRelativeError) {
		
		this.minTrackableValue = minTrackableValue;
		 
		final int numberOfSubnormalBins = (int) Math.ceil(Math.max(1./maxRelativeError-minTrackableValue/minimumBinSize, 0.));
		
		this.subNormalBinLimit = minTrackableValue + numberOfSubnormalBins*minimumBinSize;
		assert this.subNormalBinLimit >= Double.MIN_NORMAL; // otherwise, bit operations below are not correct
		
		factor1 = 0.25/Math.log(1.+ maxRelativeError);
		factor2 = 1./minimumBinSize;
		offset = getIndexHelper(subNormalBinLimit) - numberOfSubnormalBins;		
		final int arraySize = numberOfSubnormalBins + (int)(getIndexHelper(maxTrackableValue)-getIndexHelper(subNormalBinLimit)) + 1;
		counts = new long[arraySize];
	}
	
	private double getIndexHelper(final double value) {
		final long valueBits = Double.doubleToRawLongBits(value);
		final long exponent = (valueBits & 0x7ff0000000000000L) >>> 52;
		final double exponentMul3 = exponent + (exponent << 1);
		final double mantissaPlus1 = Double.longBitsToDouble((valueBits & 0x800fffffffffffffL) | 0x3ff0000000000000L);
		return factor1*((mantissaPlus1-1.)*(5.-mantissaPlus1)+exponentMul3);
	}
	
	public void add(final double value, final long count) {
		if (value >= subNormalBinLimit) {	
			final int idx = (int) (getIndexHelper(value) - offset);
			if (idx < counts.length) {
				counts[idx] += count;
			}
			else {
				if (THROW_EXCEPTION_IF_OUT_OF_RANGE) {
					throw new RuntimeException("Value too large!");
				}
				else {
					overFlowCount += count;
				}
			}
		}
		else if (value>=minTrackableValue) {
			final int idx = (int) ((value-minTrackableValue)*factor2);
			counts[idx] += count;
		}
		else {
			if (THROW_EXCEPTION_IF_OUT_OF_RANGE) {
				throw new RuntimeException("Value too small!");
			}
			else {
				underFlowCount += count;
			}
		}
		if (value<min) {
			min = value;
		}
		if (value>max) {
			max = value;
		}
		totalCount += count;
	}
	
	public long getSumOfCounts() {
		long sum = underFlowCount + overFlowCount;
		for (final long c : counts) {
			sum += c;
		}
		return sum;
	}
	
	public long getTotalCount() {
		return totalCount;
	}

	public double getMin() {
		return min;
	}

	public double getMax() {
		return max;
	}
	
	
}
