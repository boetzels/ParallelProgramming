package ethz.ch.pp.assignment2;

import java.util.Arrays;
import java.util.Random;

public class Main {

	public static void main(String[] args) {
 		
		// TODO: adjust appropriately for the required experiments
		taskA();

		int[] input1 = generateRandomInput(1000);
		int[] input2 = generateRandomInput(10000);
		int[] input3 = generateRandomInput(100000);
		int[] input4 = generateRandomInput(1000000);
		
		// Sequential version

		boolean seperateThread = false;
		taskB(input1, seperateThread);
		taskB(input2, seperateThread);
		//taskB(input3, seperateThread);
		//taskB(input4, seperateThread);


		// Parallel version
		int numThreads = 4;
		taskE(input1, numThreads);
		taskE(input2, numThreads);
		//taskE(input3, numThreads);
		taskE(input4, numThreads);
		taskE(input4, numThreads/2);

		long threadOverhead = taskC();
		int cores = Runtime.getRuntime().availableProcessors();
		System.out.format("Thread overhead on current system is: %d nano-seconds\n", threadOverhead);
		System.out.println("Cores: "+cores);
	}
	
	private final static Random rnd = new Random(42);

	public static int[] generateRandomInput() {
		return generateRandomInput(rnd.nextInt(10000) + 1);
	}
	
	public static int[] generateRandomInput(int length) {	
		int[] values = new int[length];		
		for (int i = 0; i < values.length; i++) {
			values[i] = rnd.nextInt(99999) + 1;				
		}		
		return values;
	}
	
	public static int[] computePrimeFactors(int[] values) {		
		int[] factors = new int[values.length];	
		for (int i = 0; i < values.length; i++) {
			factors[i] = numPrimeFactors(values[i]);
		}		
		return factors;
	}
	
	public static int numPrimeFactors(int number) {
		int primeFactors = 0;
		int n = number;		
		for (int i = 2; i <= n; i++) {
			while (n % i == 0) {
				primeFactors++;
				n /= i;
			}
		}
		return primeFactors;
	}
	
	public static class ArraySplit {
		public final int startIndex;
		public final int length;
		
		ArraySplit(int startIndex, int length) {
			this.startIndex = startIndex;
			this.length = length;
		}
	}
	
	// TaskD
	public static ArraySplit[] PartitionData(int length, int numPartitions) {
		//TODO: implement
		ArraySplit[] result = new ArraySplit[numPartitions];
		int dataLength = length / numPartitions;
		int overhead = length % numPartitions;

		int startIndex = 0;
		for (int i = 0; i < numPartitions; i++) {
			int thisLength = overhead-- > 0 ? dataLength + 1 : dataLength;
			result[i] = new ArraySplit(startIndex, thisLength);
			startIndex += thisLength;
		}

		return result;
	}
	
	public static void taskA() {
		//TODO: implement
		try {
			Thread t = new Thread() {
				public void run() {
					System.out.println("Hello Thread! (A)");
				}
			};
			t.start();
			t.join();
		}
		catch (InterruptedException e) {
			System.out.println("Thread interrupted! (A)");
		}
	}
	
	public static int[] taskB(final int[] values, boolean sep) {
		//TODO: implement
		long startTime = System.nanoTime();
		int[] primeFactors = computePrimeFactors(values);
		long endTime = System.nanoTime();
		long elapsedNs = endTime - startTime;
		double elapsedMs = elapsedNs / 1.0e6;

		System.out.println("MainThread took: "+elapsedMs+"ms");

		if (sep) {

			try {
				Thread t = new Thread() {
					public void run() {
						long startTime = System.nanoTime();
						computePrimeFactors(values);
						long endTime = System.nanoTime();
						long elapsedNs = endTime - startTime;
						double elapsedMs = elapsedNs / 1.0e6;

						System.out.println("Seperate Thread took: " + elapsedMs + "ms");
					}
				};
				t.start();
				t.join();
			} catch (InterruptedException e) {
				System.out.println("Thread interrupted! ");
			}
		}

		return primeFactors;
	}
	
	// Returns overhead of creating thread in nano-seconds
	public static long taskC() {		
		//TODO: implement
		long startTime = System.nanoTime();
		try {
			Thread t = new Thread() {
				public void run() {}
			};
			t.start();
			t.join();
		} catch (InterruptedException e) {
			System.out.println("Thread interrupted! ");
		}
		long endTime = System.nanoTime();
		long elapsedNs = endTime - startTime;

		return elapsedNs;
	}
	
	public static int[] taskE(final int[] values, final int numThreads) {
		//TODO: implement
		long startTime = System.nanoTime();

		int valLength = values.length;
		int[] primeFactors = new int[valLength];

		ArraySplit[] partitionData = PartitionData(valLength, numThreads);

		Thread[] threads = new Thread[numThreads];
		ParallelComputePrimeFactors[] pcpf = new ParallelComputePrimeFactors[numThreads];

		for (int i = 0; i < numThreads; i++) {
			int[] partition = Arrays.copyOfRange(values, partitionData[i].startIndex, partitionData[i].startIndex + partitionData[i].length + 1);

			pcpf[i] = new ParallelComputePrimeFactors(partition);

			threads[i] = new Thread(pcpf[i]);
			threads[i].start();
		}

		for (int i = 0; i < numThreads; i++) {
			try {
				threads[i].join();
				int[] primes = pcpf[i].getPrimeFactors();
				//System.out.println(i+" startIndex: "+partitionData[i].startIndex+" length: "+partitionData[i].length);
				System.arraycopy(primes, 0, primeFactors, partitionData[i].startIndex, partitionData[i].length);

			} catch (InterruptedException e) {
				System.out.println("Thread "+i+" interrupted! ");
			}
		}

		long endTime = System.nanoTime();
		long elapsedNs = endTime - startTime;
		double elapsedMs = elapsedNs / 1.0e6;

		System.out.println("Parallel took: " + elapsedMs + "ms");

		return primeFactors;
	}

	public static class ParallelComputePrimeFactors implements Runnable {
		private final int[] values;
		private int[] primeFactors;

		public ParallelComputePrimeFactors(int[] values) {
			this.values = values;
		}
		public void run() {
			this.primeFactors = computePrimeFactors(this.values);
		}

		public int[] getPrimeFactors() {
			return primeFactors;
		}
	}
}
