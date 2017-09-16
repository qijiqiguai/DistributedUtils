package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Created by wangqi on 2017/9/16 上午11:22.
 */
public class FileUtil {
    public static String readFile(String fileName) {
        BufferedReader reader = null;
        StringBuffer sb = new StringBuffer();
        try {
            File file = getFile(fileName);
            reader = new BufferedReader(new FileReader(file));
            String tempString = null;
            int line = 1;
            // 一次读入一行，直到读入null为文件结束
            while ((tempString = reader.readLine()) != null) {
                sb.append(tempString + "\n");
                line++;
            }
            System.out.println(fileName + " total line " + line + " ...");
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                }
            }
        }
        return sb.toString();
    }

    private static File getFile(String fileName) throws URISyntaxException {
        if(fileName.contains("classpath:")){
            ClassLoader classLoader = FileUtil.class.getClassLoader();
            URL url = classLoader.getResource(fileName.replaceAll("classpath:", ""));
            return new File(url.toURI().getPath());
        }else{
            return new File(fileName);
        }
    }
}
