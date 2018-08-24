kernel void hello(global float* a, int numElements) {

	  // Get index into global data array
        int iGID = get_global_id(0);

        // Bound check, equivalent to the limit on a 'for' loop
        if (iGID >= numElements)  {
            return;
        }

        // Add the vector elements
        a[iGID] = 3.14f;
}