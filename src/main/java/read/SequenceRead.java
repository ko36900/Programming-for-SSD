package read;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Created by lds on 2019-11-14.
 */
public class SequenceRead {
    private void read(String fileName,int bufferSize,int readCount) throws FileNotFoundException {
        //创建文件通道
        FileInputStream fileInputStream = new FileInputStream(fileName);
        FileChannel in = fileInputStream.getChannel();

        //创建堆外缓冲区
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(bufferSize);

//        List<Integer> sequenceList = new ArrayList<>(writeCount);
//        for (int i = 0; i < writeCount; i++) {
//            sequenceList.add(i);
//        }
//        Collections.shuffle(sequenceList);
        //执行读操作
        long start = System.currentTimeMillis();
        for (int i = 0; i < readCount; i++) {
            try {
                in.read(byteBuffer);
                //in.read(byteBuffer,(long)sequenceList.get(i)*bytes.length);
                //byteBuffer.get(bytes);
                byteBuffer.clear();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        long end = System.currentTimeMillis();
        System.out.println(end-start);
    }

    public static void main(String[] args) {
        SequenceRead sequenceRead = new SequenceRead();
        try {
            sequenceRead.read("file\\singleThread_test",4*1024,2*1024*1024);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
