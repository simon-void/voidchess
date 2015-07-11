package figures;

import image.*;
import helper.*;

import static org.testng.Assert.*;
import org.testng.annotations.*;

public class RochadeFigureTest
{
  @Test
  public void testGetInitialPosition()
  {
    Position initial = Position.get( "e1" );
    Position second  = Position.get( "d1" );

    King king = new King( true,initial );
    
    assertEquals( king.getInitialPosition(),initial );
    king.figureMoved( Move.get( initial,second ) );
    assertEquals( king.getInitialPosition(),initial );
  }
}
