package tech.qiwang;


import cn.hutool.core.util.IdUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * TODO String 的测试不准确
 */
@RunWith(SpringRunner.class)
public class ObjectSizeTest {

    @Test
    public void basicOps() throws Exception {
        StringWrapper stringWrapper = new StringWrapper();
        System.out.println(stringWrapper.size() + " Bytes");
    }

    class StringWrapper extends SizeOf{
        @Override
        protected Object newInstance() {
            return IdUtil.simpleUUID();
        }
    }
}
