/*
 * Created on 30.09.2006
 */

package voidchess.helper;

import org.testng.annotations.Test;

import static org.testng.Assert.fail;

public class RuntimeFacadeTest {
    @Test
    public void testAssertJavaVersion() {
        try {
            RuntimeFacade.assertJavaVersion("1.7");
            RuntimeFacade.assertJavaVersion("1.7.0");
            RuntimeFacade.assertJavaVersion("1.8");
            RuntimeFacade.assertJavaVersion("8.0");
        } catch (RuntimeException e) {
            fail(e.toString());
        }

        try {
            RuntimeFacade.assertJavaVersion("1.0");
            fail("Should have thrown Exception:1.0");
        } catch (RuntimeException e) {
        }
        try {
            RuntimeFacade.assertJavaVersion("1.3.0_14");
            fail("Should have thrown Exception:1.3.0_14");
        } catch (RuntimeException e) {
        }
    }

}
