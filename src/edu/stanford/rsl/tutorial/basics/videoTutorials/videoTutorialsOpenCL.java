package edu.stanford.rsl.tutorial.basics.videoTutorials;

import java.io.IOException;
import java.nio.FloatBuffer;

import com.jogamp.opencl.CLBuffer;
import com.jogamp.opencl.CLCommandQueue;
import com.jogamp.opencl.CLContext;
import com.jogamp.opencl.CLDevice;
import com.jogamp.opencl.CLKernel;
import com.jogamp.opencl.CLProgram;
import com.jogamp.opencl.CLMemory.Mem;

import edu.stanford.rsl.conrad.opencl.OpenCLUtil;


public class videoTutorialsOpenCL {
	public static void main(String[] args) {
		
		// Another Java OpenCL example - vector addition: 
		// https://jogamp.org/wiki/index.php/JOCL_Tutorial
						
		
		// Set up (uses default CLPlatform and creates context for all devices)
		CLContext context = OpenCLUtil.getStaticContext();
		System.out.println("created "+context);
		
		try {	
			// Select fastest device
			CLDevice device = context.getMaxFlopsDevice();			
			
			// Create command queue on device.
			CLCommandQueue queue = device.createCommandQueue();
					
			int elementCount = 1000000;  // Length of arrays to process
			int localWorkSize = Math.min(device.getMaxWorkGroupSize(), 256);  // Local work size dimensions
			int globalWorkSize = OpenCLUtil.roundUp(localWorkSize, elementCount);   // rounded up to the nearest multiple of the localWorkSize		
			
			// Create the float buffer
			CLBuffer<FloatBuffer> clBufferA  = context.createFloatBuffer(globalWorkSize, Mem.READ_WRITE);	
			
			// Load sources, create and build program	
			CLProgram program = context.
					createProgram(videoTutorialsOpenCL.class.getResourceAsStream("hello.cl")).build();				

					
			// Get a reference to the kernel function with the name 'hello'
			// and map the buffers to its input parameters.
			CLKernel kernel = program.createCLKernel("hello");
			kernel.putArgs(clBufferA).putArg(elementCount);
		

			long time1 = System.nanoTime();
									
			// Asynchronous write of data to GPU device,
			// followed by blocking read to get the computed results back.
			queue.putWriteBuffer(clBufferA, false).put1DRangeKernel(kernel,0,globalWorkSize,localWorkSize).putReadBuffer(clBufferA, true);
				
			
			
			time1 = System.nanoTime() - time1;
			
			long time2 = System.nanoTime();
			float a[] = new float [elementCount];
			for (int i = 0; i < elementCount; ++i) {				
				a[i] = 3.14f;
			}
			time2 = System.nanoTime() - time2;
			
			// Print first few elements of the resulting buffer to the console.
			System.out.println("String is: ");
			for(int i = 0; i < 10; i++) {
				System.out.print(clBufferA.getBuffer().get() + " ");
			}
			System.out.println("\ncomputation took (OpenCL): "+(time1/1000000)+"ms");
			System.out.println("\ncomputation took (Naive): "+(time2/1000000)+"ms");
		
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}