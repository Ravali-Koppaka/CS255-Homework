import org.jocl.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

import static org.jocl.CL.*;

public class JoclInner {

    private static String programSource =
            "__kernel void "+
                    "innerProductKernel(__global const float *a,"+
                    "             __global const float *b,"+
                    "             __global float *c)"+
                    "{"+
                    "   int gid = get_global_id(0);"+
                    "   c[gid] = a[gid] * b[gid];"+
                    "   barrier(CLK_LOCAL_MEM_FENCE);" +
                    "   int size = get_global_size(0);" +
                    "   int i = 1;" +
                    "   while((i < size)) {" +
                    "       if(gid%2 == 0) {" +
                    "          c[gid] +=  c[gid + i];" +
                    "       }" +
                    "       i = i * 2;" +
                    "       barrier(CLK_LOCAL_MEM_FENCE);" +
                    "   }" +
                    "}";

    public static void main(String args[])
    {
        String fileName = args[0];
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream(fileName)));

        List<Float> A = new LinkedList<>();
        List<Float> B = new LinkedList<>();

        String line;
        try {
            while((line = bufferedReader.readLine()) != null) {
                String[] values = line.split(",");
                A.add(Float.valueOf(values[0]));
                B.add(Float.valueOf(values[1]));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                bufferedReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        int n = A.size();
        float arrayA[] = new float[n];
        float arrayB[] = new float[n];
        float innerProduct[] = new float[n];
        for (int i=0; i<n; i++)
        {
            arrayA[i] = A.get(i);
            arrayB[i] = B.get(i);
        }
        Pointer ptrA = Pointer.to(arrayA);
        Pointer ptrB = Pointer.to(arrayB);
        Pointer ptrProduct = Pointer.to(innerProduct);

        final int platformIndex = 0;
        final long deviceType = CL_DEVICE_TYPE_ALL;
        final int deviceIndex = 0;

        CL.setExceptionsEnabled(true);

        int numPlatformsArray[] = new int[1];
        clGetPlatformIDs(0, null, numPlatformsArray);
        int numPlatforms = numPlatformsArray[0];

        cl_platform_id platforms[] = new cl_platform_id[numPlatforms];
        clGetPlatformIDs(platforms.length, platforms, null);
        cl_platform_id platform = platforms[platformIndex];

        cl_context_properties contextProperties = new cl_context_properties();
        contextProperties.addProperty(CL_CONTEXT_PLATFORM, platform);

        int numDevicesArray[] = new int[1];
        clGetDeviceIDs(platform, deviceType, 0, null, numDevicesArray);
        int numDevices = numDevicesArray[0];

        cl_device_id devices[] = new cl_device_id[numDevices];
        clGetDeviceIDs(platform, deviceType, numDevices, devices, null);
        cl_device_id device = devices[deviceIndex];

        cl_context context = clCreateContext(
                contextProperties, 1, new cl_device_id[]{device},
                null, null, null);

        cl_command_queue commandQueue =
                clCreateCommandQueue(context, device, 0, null);

        cl_mem memObjects[] = new cl_mem[3];
        memObjects[0] = clCreateBuffer(context,
                CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR,
                Sizeof.cl_float * n, ptrA, null);
        memObjects[1] = clCreateBuffer(context,
                CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR,
                Sizeof.cl_float * n, ptrB, null);
        memObjects[2] = clCreateBuffer(context,
                CL_MEM_READ_WRITE,
                Sizeof.cl_float * n, null, null);

        cl_program program = clCreateProgramWithSource(context,
                1, new String[]{ programSource }, null, null);

        clBuildProgram(program, 0, null, null, null, null);

        cl_kernel kernel = clCreateKernel(program, "innerProductKernel", null);

        clSetKernelArg(kernel, 0,
                Sizeof.cl_mem, Pointer.to(memObjects[0]));
        clSetKernelArg(kernel, 1,
                Sizeof.cl_mem, Pointer.to(memObjects[1]));
        clSetKernelArg(kernel, 2,
                Sizeof.cl_mem, Pointer.to(memObjects[2]));

        long global_work_size[] = new long[]{n};
        long local_work_size[] = new long[]{1};

//        long startTime = System.nanoTime();
        clEnqueueNDRangeKernel(commandQueue, kernel, 1, null,
                global_work_size, local_work_size, 0, null, null);
//        long endTime = System.nanoTime();
//        long totalTime = endTime - startTime;

        clEnqueueReadBuffer(commandQueue, memObjects[2], CL_TRUE, 0,
                n * Sizeof.cl_float, ptrProduct, 0, null, null);

        clReleaseMemObject(memObjects[0]);
        clReleaseMemObject(memObjects[1]);
        clReleaseMemObject(memObjects[2]);
        clReleaseKernel(kernel);
        clReleaseProgram(program);
        clReleaseCommandQueue(commandQueue);
        clReleaseContext(context);

//        System.out.println("Time(ns) : " + totalTime);
        System.out.println(innerProduct[0]);
    }

}
