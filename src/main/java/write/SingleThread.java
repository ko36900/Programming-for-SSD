package write;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Created by lds on 2019-11-14.
 */
public class SingleThread {
    public void write(String fileName,int bufferSize,long fileSize) throws FileNotFoundException {
        //创建文件通道
        FileOutputStream outputStream = new FileOutputStream(fileName);
        FileChannel out = outputStream.getChannel();
        //初始化写入缓冲区
        byte[] bytes = new byte[bufferSize];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = 127;
        }
        /*使用堆外内存做缓存区可以减少一次数据拷贝，且不会影响GC*/
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(bytes.length);
        byteBuffer.put(bytes);
        byteBuffer.flip();
        //计算写入次数
        int writeCount = (int)(fileSize / bufferSize);
        //执行写入
        long start = System.currentTimeMillis();

        for (int i = 0; i < writeCount; i++) {
            try {
                out.write(byteBuffer);
                byteBuffer.flip();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        long end = System.currentTimeMillis();
        System.out.println(end-start);
    }

    public static void main(String[] args) {
        SingleThread singleThread = new SingleThread();
        try {
            singleThread.write("singleThread_test",4*1024,8*1024*1024*1024);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
