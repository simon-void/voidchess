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
    
    FigureImage figureImage = new FigureImageMock(10,20,30);
    King king = new King( figureImage,true,initial );
    
    assertEquals( king.getInitialPosition(),initial );
    king.figureMoved( Move.get( initial,second ) );
    assertEquals( king.getInitialPosition(),initial );
  }
}
