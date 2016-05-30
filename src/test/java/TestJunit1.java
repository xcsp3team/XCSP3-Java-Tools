import org.junit.Test;
import static org.junit.Assert.*;


public class TestJunit1 {
   
   @Test
   public void testPrintMessage() {	
      System.out.println("Inside testPrintMessage()");    
      assertTrue(3==3);     
   }
}
