package org.HdrHistogram;

public class MemoryEfficientHistogram {
	final long[] counts;
	long underFlowCount = 0;
	long overFlowCount = 0;	
	final double factor;
	final double offset;
	final double minExpectedQuantileValue;
	
	public MemoryEfficientHistogram(final double minExpectedQuantileValue, final double maxExpectedQuantileValue, final double maxRelativeError) {
		
		assert minExpectedQuantileValue >= Double.MIN_NORMAL; // bit operations below are not correct for subnormals
		// TODO range checks

		this.minExpectedQuantileValue = minExpectedQuantileValue;
		factor = 0.25/Math.log(1.+ maxRelativeError);
		offset = getIndexHelper(minExpectedQuantileValue);		
		final int arraySize = getIndex(maxExpectedQuantileValue) + 1;
		counts = new long[arraySize];
	}
		
	int getIndex(final double value) {
		return (int) (getIndexHelper(value) - offset);
	}
	
	double getIndexHelper(final double value) {
		final long valueBits = Double.doubleToRawLongBits(value);
		final long exponent = (valueBits & 0x7ff0000000000000L) >>> 52;
		final double exponentMul3 = exponent + (exponent << 1);
		final double mantissaPlus1 = Double.longBitsToDouble((valueBits & 0x800fffffffffffffL) | 0x3ff0000000000000L);
		return factor*((mantissaPlus1-1.)*(5.-mantissaPlus1)+exponentMul3);
	}

	public void add(final double value, final long count) {
		final int idx = getIndex(value);
		if (value >= minExpectedQuantileValue) {	
			if (idx < counts.length) {
				counts[idx] += count;
			}
			else {
				overFlowCount += count;
			}
		}
		else {
			underFlowCount += count;
		}
	}
	
	public long getCount() {
		long sum = underFlowCount + overFlowCount;
		for (final long c : counts) {
			sum += c;
		}
		return sum;
	}
}
