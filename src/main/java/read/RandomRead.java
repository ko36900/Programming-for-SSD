package read;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by lds on 2019-11-14.
 */
public class RandomRead {
    private static ThreadLocal<ByteBuffer> byteBufferThreadLocal = new ThreadLocal<>();
    private static ThreadLocal<FileChannel> fileChannelThreadLocal = new ThreadLocal<>();

    private void read(String fileName,int bufferSize,int readCount,int threadCount) throws FileNotFoundException {

        //初始化线程池
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);

        //生成乱序读取序列
        List<Integer> sequenceList = new ArrayList<>(readCount);
        for (int i = 0; i < readCount; i++) {
            sequenceList.add(i);
        }
        Collections.shuffle(sequenceList);

        //初始化countDownLatch
        CountDownLatch countDownLatch = new CountDownLatch(readCount);

        //分发读任务
        long start = System.currentTimeMillis();
        for (int i = 0; i < readCount; i++) {
            final int index = i;
            executorService.execute(()->{
                ByteBuffer byteBuffer = byteBufferThreadLocal.get();
                //线程首次执行时初始化buffer和channel
                if(byteBuffer == null){
                   /*使用堆外内存做缓存区可以减少一次数据拷贝，且不会影响GC*/
                    byteBuffer = ByteBuffer.allocateDirect(bufferSize);
                    byteBufferThreadLocal.set(byteBuffer);
                }
                FileChannel in = fileChannelThreadLocal.get();
                if (in == null) {
                    //创建文件通道
                    try {
                        FileInputStream fileInputStream = new FileInputStream(fileName);
                        in = fileInputStream.getChannel();
                        fileChannelThreadLocal.set(in);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                //执行读取
                try {
                    //指定文件位置读取
                    in.read(byteBuffer, (long)sequenceList.get(index) * bufferSize);
                    byteBuffer.clear();
                } catch (Exception e) {
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
         RandomRead randomRead = new RandomRead();
        try {
            randomRead.read("file\\singleThread_test",4*1024,2*1024*1024,4);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
