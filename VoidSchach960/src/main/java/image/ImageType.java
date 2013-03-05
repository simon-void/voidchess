package image;

public enum ImageType {
  B_PAWN,
  B_KNIGHT,
  B_BISHOP,
  B_ROCK,
  B_QUEEN,
  B_KING,
  W_PAWN,
  W_KNIGHT,
  W_BISHOP,
  W_ROCK,
  W_QUEEN,
  W_KING,
  ICON;
  
  public String getFileName()
  {
    return name().toLowerCase()+".gif";
  }
}
