package org.HdrHistogram;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Random;

import org.junit.Test;

public class PerformanceTest {

	private static final int RANGE = 1_000_000_000;
	private static final long MIN = 1000;
	private static final long MAX = MIN*RANGE;
	private static final int PRECISION_DIGITS = 3;
	private static final double PRECISION = Math.pow(10., -PRECISION_DIGITS);
	private static final int NUM_CYCLES = 100;
	private static final int NUM_VALUES = 100_000_000;

	private static final double[] createTestDataDouble() {
		final double values[] = new double[NUM_VALUES];
		final Random random = new Random(0);
		for (int i = 0; i < NUM_VALUES; ++i) {
			values[i] = MIN*Math.pow(RANGE,random.nextDouble());
		}
		return values;
	}
	
	private static final long[] createTestDataLong() {
		final long values[] = new long[NUM_VALUES];
		final Random random = new Random(0);
		for (int i = 0; i < NUM_VALUES; ++i) {
			values[i] = (long) (MIN*Math.pow(RANGE,random.nextDouble()));
		}
		return values;
	}
	
	private static final double[] TEST_DATA_DOUBLE = createTestDataDouble();
	private static final long[] TEST_DATA_LONG = createTestDataLong();

	
	@Test
	public void testPerformanceMemoryEfficientHistogramDoubleData() {
		
		final long  start = System.currentTimeMillis();
		for (int m = 0; m < NUM_CYCLES; ++m) {			
			final MemoryEfficientHistogram histogram =  new MemoryEfficientHistogram(0., MIN, MAX, PRECISION);
			for (int i = 0; i < NUM_VALUES; ++i) {
				histogram.add(TEST_DATA_DOUBLE[i], 1);
			}
			assertEquals(NUM_VALUES, histogram.getSumOfCounts());
			assertEquals(NUM_VALUES, histogram.getTotalCount());
			assertTrue(MIN <= histogram.getMin());
			assertTrue(MAX >= histogram.getMax());
		}
		final long end = System.currentTimeMillis();
		final double avgRecordingTimeNanos = ((end - start)*1e6)/((double)NUM_VALUES*(double)NUM_CYCLES);
		final int countArrayLength = new MemoryEfficientHistogram(0., MIN, MAX, PRECISION).counts.length;
		System.out.println("Avg recording time MemoryEfficientHistogram (for double data) = " + avgRecordingTimeNanos + "ns.");
		System.out.println("Size of count array of MemoryEfficientHistogram = " + countArrayLength);
	}
	
	@Test
	public void testPerformanceDoubleHistogram() {
		
		final long  start = System.currentTimeMillis();
		
		for (int m = 0; m < NUM_CYCLES; ++m) {			
			final DoubleHistogram histogram =  new DoubleHistogram(RANGE, PRECISION_DIGITS);
			for (int i = 0; i < NUM_VALUES; ++i) {
				histogram.recordValue(TEST_DATA_DOUBLE[i]);
			}
			assertEquals(NUM_VALUES, histogram.getTotalCount());
		}
		final long end = System.currentTimeMillis();
		final double avgRecordingTimeNanos = ((end - start)*1e6)/((double)NUM_VALUES*(double)NUM_CYCLES);
		final int countArrayLength = ((Histogram)(new DoubleHistogram(RANGE, PRECISION_DIGITS).integerValuesHistogram)).counts.length;
		System.out.println("Avg recording time DoubleHistogram = " + avgRecordingTimeNanos + "ns.");
		System.out.println("Size of count array of DoubleHistogram = " + countArrayLength);
		
	}
	
	@Test
	public void testPerformanceHistogram() {
		
		final long  start = System.currentTimeMillis();
		
		for (int m = 0; m < NUM_CYCLES; ++m) {	
			final Histogram histogram = new Histogram(MIN, MAX, PRECISION_DIGITS);
			for (int i = 0; i < NUM_VALUES; ++i) {
				histogram.recordValue(TEST_DATA_LONG[i]);
			}
			assertEquals(NUM_VALUES, histogram.getTotalCount());
		}
		final long end = System.currentTimeMillis();
		final double avgRecordingTimeNanos = ((end - start)*1e6)/((double)NUM_VALUES*(double)NUM_CYCLES);
		final int countArrayLength = new Histogram(MIN, MAX, PRECISION_DIGITS).counts.length;
		System.out.println("Avg recording time Histogram = " + avgRecordingTimeNanos + "ns.");
		System.out.println("Size of count array of Histogram = " + countArrayLength);
		
	}
	
	@Test
	public void testPerformanceMemoryEfficientHistogramLongData() {
		
		final long  start = System.currentTimeMillis();
		for (int m = 0; m < NUM_CYCLES; ++m) {			
			final MemoryEfficientHistogram histogram =  new MemoryEfficientHistogram(MIN, 0., MAX, PRECISION);
			for (int i = 0; i < NUM_VALUES; ++i) {
				histogram.add(TEST_DATA_LONG[i], 1);
			}
			assertEquals(NUM_VALUES, histogram.getSumOfCounts());
			assertEquals(NUM_VALUES, histogram.getTotalCount());
			assertTrue(MIN <= histogram.getMin());
			assertTrue(MAX >= histogram.getMax());
		}
		final long end = System.currentTimeMillis();
		final double avgRecordingTimeNanos = ((end - start)*1e6)/((double)NUM_VALUES*(double)NUM_CYCLES);
		final int countArrayLength = new MemoryEfficientHistogramLong(MIN, MAX, PRECISION).counts.length;
		System.out.println("Avg recording time MemoryEfficientHistogram (for long data) = " + avgRecordingTimeNanos + "ns.");
		System.out.println("Size of count array of MemoryEfficientHistogram = " + countArrayLength);
	}
	
	
	
}
