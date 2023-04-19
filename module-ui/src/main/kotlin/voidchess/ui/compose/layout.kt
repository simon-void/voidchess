package voidchess.ui.compose

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import voidchess.ui.swing.ChessboardComponent
import voidchess.ui.swing.ComputerPlayerComponent

@Composable
fun fullLayout(
    computerPlayerComponent: ComputerPlayerComponent,
    chessComponent: ChessboardComponent,
    ) {
    Row {
        leftSide(computerPlayerComponent)
        rightSide(chessComponent)
    }
}

@Composable
fun rightSide(chessComponent: ChessboardComponent) {
    Scaffold(
        topBar = {Chess960Panel()},
        bottomBar = { Chess960Panel() },
    ) {
        val size = chessComponent.preferredSize
//        SwingPanel(
//            background = Color.LightGray,
//            modifier = Modifier.size(800.dp, 800.dp),
//            factory = {
//                chessComponent
//            },
//        )
        myCanvas()
    }
}

@Composable
fun leftSide(computerPlayerComponent: ComputerPlayerComponent) {
    Scaffold(
        topBar = {StartResignSwitchButtons()},
        bottomBar = { OptionsPanel() }
    ) {
        val size = computerPlayerComponent.preferredSize
        SwingPanel(
            background = Color.Green,
            modifier = Modifier.size(size.width.dp, size.height.dp),
            factory = {
                computerPlayerComponent
            },
        )
    }
}

@Composable
@Preview
fun StartResignSwitchButtons() {
//    val count = remember { mutableStateOf(0) }
    Row {
        Button(//modifier = Modifier,
            onClick = {

            }) {
            Text("reset")
        }
        Button(//modifier = Modifier,
            onClick = {

            }) {
            Text("switch seats")
        }
    }
//    MaterialTheme {
//        Column(Modifier.fillMaxSize(), Arrangement.spacedBy(5.dp)) {
//            Button(modifier = Modifier.align(Alignment.CenterHorizontally),
//                onClick = {
//                    count.value++
//                }) {
//                Text(if (count.value == 0) "Hello World" else "Clicked ${count.value}!")
//            }
//            Button(modifier = Modifier.align(Alignment.CenterHorizontally),
//                onClick = {
//                    count.value = 0
//                }) {
//                Text("Reset")
//            }
//        }
//    }
}

@Composable
@Preview
fun OptionsPanel() {
    Row {
        Column(verticalArrangement = Arrangement.Center
        ) {
            Text("difficulty:")
            Text("#cores:")
        }
        Column {
            //DropdownMenu()
            Text("Level2")
            Text("4")
        }
    }
}

@Composable
@Preview
fun DropdownBox() {
    var expanded: Boolean by remember { mutableStateOf(false) }
    val suggestions = listOf("Item1","Item2","Item3")
    var selectedText: String by remember { mutableStateOf("") }

    var textfieldSize: Size by remember { mutableStateOf(Size.Zero)}

    val icon = if (expanded)
        Icons.Filled.KeyboardArrowUp
    else
        Icons.Filled.KeyboardArrowDown


    Box() {
        OutlinedTextField(
            value = selectedText,
            onValueChange = { selectedText = it },
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { coordinates ->
                    //This value is used to assign to the DropDown the same width
                    textfieldSize = coordinates.size.toSize()
                },
            label = {Text("Label")},
            trailingIcon = {
                Icon(icon,"contentDescription",
                    Modifier.clickable { expanded = !expanded })
            }
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
//            modifier = Modifier
//                .width(with(LocalDensity.current){textfieldSize.width.toDp()})
        ) {
            suggestions.forEach { label ->
                DropdownMenuItem(onClick = {
                    selectedText = label
                }) {
                    Text(text = label)
                }
            }
        }
    }
}

@Composable
@Preview
fun Chess960Panel() {
    val chess960Index: String by remember { mutableStateOf("518") }
    val spaceBetweenElements = 5.dp
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Button(onClick = {}) {
            Text("classic setup")
        }
        Spacer(Modifier.width(spaceBetweenElements))
        Button(onClick = {}) {
            Text("shuffle setup")
        }
        Spacer(Modifier.width(spaceBetweenElements))
        Text("chess960 index:")
        Spacer(Modifier.width(1.dp))
        OutlinedTextField(
            value = chess960Index,
            onValueChange = {proposedIndex->},
            modifier = Modifier.width(58.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        )
    }
}