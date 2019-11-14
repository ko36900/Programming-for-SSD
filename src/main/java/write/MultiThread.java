package write;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by lds on 2019-11-14.
 */
public class MultiThread {
    private static ThreadLocal<ByteBuffer> byteBufferThreadLocal = new ThreadLocal<>();
    private static ThreadLocal<FileChannel> fileChannelThreadLocal = new ThreadLocal<>();

    private void write(String fileName,int bufferSize,int writeCount,int threadCount) throws FileNotFoundException {

        //初始化线程池
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);

        //初始化写入缓冲区
        byte[] bytes = new byte[bufferSize];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = 127;
        }
        //初始化countDownLatch
        CountDownLatch countDownLatch = new CountDownLatch(writeCount);
        //执行任务分发
        long start = System.currentTimeMillis();

        for (int i = 0; i < writeCount; i++) {
           executorService.execute(()->{
               ByteBuffer byteBuffer = byteBufferThreadLocal.get();
               if(byteBuffer == null){
                   /*使用堆外内存做缓存区可以减少一次数据拷贝，且不会影响GC*/
                   byteBuffer = ByteBuffer.allocateDirect(bytes.length);
                   byteBuffer.put(bytes);
                   byteBuffer.flip();
               }
               FileChannel out = fileChannelThreadLocal.get();
               if(out==null){
                   //创建文件通道
                   try {
                       FileOutputStream outputStream = new FileOutputStream(fileName+"_"+Thread.currentThread().getId());
                       out = outputStream.getChannel();
                       fileChannelThreadLocal.set(out);
                   } catch (Exception e) {
                       e.printStackTrace();
                   }
               }

               //执行写入
               try {
                   out.write(byteBuffer);
               } catch (IOException e) {
                   e.printStackTrace();
               }
               countDownLatch.countDown();
           });
        }

        //等待任务执行完毕
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        long end = System.currentTimeMillis();
        System.out.println(end-start);
        executorService.shutdown();
    }

    public static void main(String[] args) {
        MultiThread multiThread = new MultiThread();
        try {
            multiThread.write("file\\write\\singleThread_test",4*1024,2*1024*1024,4);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
