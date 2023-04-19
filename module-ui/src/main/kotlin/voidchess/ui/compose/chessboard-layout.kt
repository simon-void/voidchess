package voidchess.ui.compose

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.withSave
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.loadSvgPainter
import androidx.compose.ui.res.useResource
import androidx.compose.ui.unit.dp
import voidchess.common.board.move.Move
import voidchess.common.board.move.Position

@Composable
@Preview
fun myCanvas() {
    // see https://github.com/JetBrains/compose-multiplatform/tree/master/tutorials/Image_And_Icons_Manipulations

    //    fun loadSvgPainter(filePath: String, density: Density): Painter =
//        File(filePath).inputStream().buffered().use { loadSvgPainter(it, density) }
    val density = LocalDensity.current // to calculate the intrinsic size of vector images (SVG, XML)
    val chessBoardPainter = remember {
        useResource("image/chessboard.svg") { loadSvgPainter(it, density) }
    }
    val count = remember { mutableStateOf(0) }
    Canvas(
        modifier = Modifier.size(1060.dp, 1060.dp),
    ) {
        drawIntoCanvas { canvas ->
            canvas.withSave {
//                with(chessBoardPainter) {
//                    draw(chessBoardPainter.intrinsicSize)
//                }
//                canvas.translate(chessBoardPainter.intrinsicSize.width, 0f)
                with(chessBoardPainter) {
                    draw(Size(1060f, 1060f))
                }
            }
        }
    }
}

interface ChessboardState {
    val figuresByPosition: Map<Position, Paintable>
    val whiteViewpoint: Boolean
    val latestMove: Move?
    val nextMove: NextMove?

    fun positionClicked(pos: Position)
}

sealed class NextMove(val fromPos: Position) {
    class OnlyFromMove(fromPos: Position): NextMove(fromPos)
    class AlsoToMove(fromPos: Position, val possibleToPos: Position): NextMove(fromPos)
}

enum class Paintable {
    WhitePawn, WhiteRook, WhiteKnight, WhiteBishop, WhiteQueen, WhiteKing,
    BlackPawn, BlackRook, BlackKnight, BlackBishop, BlackQueen, BlackKing;
}

class ChessboardAdapter: ChessboardState {
    private var currentFiguresByPosition: Map<Position, Paintable> = buildMap {
        for(column in 0..7) {
            put(Position[1, column], Paintable.WhitePawn)
            put(Position[6, column], Paintable.BlackPawn)
        }
    }
    override val figuresByPosition: Map<Position, Paintable>
        get() = currentFiguresByPosition
    override val whiteViewpoint: Boolean
        get() = true
    override val latestMove: Move?
        get() = null //TODO("Not yet implemented")
    override val nextMove: NextMove?
        get() = null //TODO("Not yet implemented")

    override fun positionClicked(pos: Position) {
        //TODO("Not yet implemented")
    }

}