kernel void hello(global float* a, int numElements) {
	  // get index into global data array
        int iGID = get_global_id(0);

        // bound check, equivalent to the limit on a 'for' loop
        if (iGID >= numElements)  {
            return;
        }

        // add the vector elements
        a[iGID] = 1311.;
}