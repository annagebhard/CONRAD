package edu.stanford.rsl.tutorial.basics.videoTutorials;

import edu.stanford.rsl.conrad.data.numeric.Grid2D;
import edu.stanford.rsl.conrad.numerics.DecompositionSVD;
import edu.stanford.rsl.conrad.numerics.SimpleMatrix;
import edu.stanford.rsl.conrad.numerics.SimpleMatrix.MatrixNormType;
import edu.stanford.rsl.conrad.numerics.SimpleVector.VectorNormType;
import edu.stanford.rsl.conrad.utils.ImageUtil;
import edu.stanford.rsl.conrad.utils.VisualizationUtil;
import edu.stanford.rsl.conrad.numerics.SimpleOperators;
import edu.stanford.rsl.conrad.numerics.SimpleVector;

import java.io.IOException;
import java.io.InputStream;
import java.nio.FloatBuffer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.Random;

import com.jogamp.opencl.CLBuffer;
import com.jogamp.opencl.CLCommandQueue;
import com.jogamp.opencl.CLContext;
import com.jogamp.opencl.CLDevice;
import com.jogamp.opencl.CLKernel;
import com.jogamp.opencl.CLProgram;
import com.jogamp.opencl.CLMemory.Mem;

import edu.stanford.rsl.conrad.opencl.OpenCLUtil;
import edu.stanford.rsl.conrad.opencl.TestOpenCL;
import edu.stanford.rsl.tutorial.phantoms.Phantom;
import edu.stanford.rsl.tutorial.phantoms.SheppLogan;
import edu.stanford.rsl.conrad.data.numeric.opencl.OpenCLGrid2D;
import java.io.File;
import java.io.FileInputStream;


// See more details please go to: 
// https://jogamp.org/wiki/index.php/JOCL_Tutorial

public class vectorAdd {
	public static void main(String[] args) {
				
		// set up (uses default CLPlatform and creates context for all devices)
		CLContext context = OpenCLUtil.getStaticContext();
		System.out.println("created "+context);
		
		try {
			// select fastest device
			CLDevice device = context.getMaxFlopsDevice();
			System.out.println("using "+device);

			// create command queue on device.
			CLCommandQueue queue = device.createCommandQueue();

			int elementCount = 1444477;                                  // Length of arrays to process
			int localWorkSize = Math.min(device.getMaxWorkGroupSize(), 256);  // Local work size dimensions
			int globalWorkSize = OpenCLUtil.roundUp(localWorkSize, elementCount);   // rounded up to the nearest multiple of the localWorkSize


			InputStream programFile = TestOpenCL.class.getResourceAsStream("VectorAdd.cl");
			CLProgram program = context.createProgram(programFile).build();


			// A, B are input buffers, C is for the result
			CLBuffer<FloatBuffer> clBufferA = context.createFloatBuffer(globalWorkSize, Mem.READ_ONLY);
			CLBuffer<FloatBuffer> clBufferB = context.createFloatBuffer(globalWorkSize, Mem.READ_ONLY);
			CLBuffer<FloatBuffer> clBufferC = context.createFloatBuffer(globalWorkSize, Mem.WRITE_ONLY);

			// fill input buffers with random numbers
			// (just to have test data; seed is fixed -> results will not change between runs).
			fillBuffer(clBufferA.getBuffer(), 12345);
			fillBuffer(clBufferB.getBuffer(), 67890);

			// get a reference to the kernel function with the name 'VectorAdd'
			// and map the buffers to its input parameters.
			CLKernel kernel = program.createCLKernel("VectorAdd");
			kernel.putArgs(clBufferA, clBufferB, clBufferC).putArg(elementCount);

			// asynchronous write of data to GPU device,
			// followed by blocking read to get the computed results back.
			long time = System.nanoTime();
			queue.putWriteBuffer(clBufferA, false)
			.putWriteBuffer(clBufferB, false)
			.put1DRangeKernel(kernel, 0, globalWorkSize, localWorkSize)
			.putReadBuffer(clBufferC, true);
			time = System.nanoTime() - time;

			// print first few elements of the resulting buffer to the console.
			System.out.println("a+b=c results snapshot: ");
			for(int i = 0; i < 10; i++)
				System.out.print(clBufferC.getBuffer().get() + ", ");
			System.out.println("...; " + clBufferC.getBuffer().remaining() + " more");

			System.out.println("computation took: "+(time/1000000)+"ms");


		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void fillBuffer(FloatBuffer buffer, int seed) {
		Random rnd = new Random(seed);
		while(buffer.remaining() != 0)
			buffer.put(rnd.nextFloat()*100);
		buffer.rewind();
	}



}