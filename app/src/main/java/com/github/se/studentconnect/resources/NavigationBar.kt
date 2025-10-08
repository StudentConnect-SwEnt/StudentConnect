package com.github.se.studentconnect.resources

import android.graphics.drawable.shapes.Shape
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.AddBox
import androidx.compose.material.icons.outlined.ConfirmationNumber
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.NearMe
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.se.studentconnect.ui.theme.*

@Preview
@Composable
fun NavigationBar() {
    val buttonCol = ButtonColors(
        PurpleGrey80,
        Color.Black,
        PurpleGrey80,
        Color.Black)
    val width = LocalConfiguration.current.screenWidthDp
    val height = 64
    val paddingH = 3
    val paddingV = 0
    val barRoundingPercentage = 100
    val buttonRounding = 0
    val buttonPadding = 0
    val buttonSize = Modifier.size(((width-2*paddingH)/5).dp)
    val iconSize = 56
    val fontSize = 12f.sp

    BottomAppBar(
        actions = {
            Button(
                onClick = {},
                shape = RoundedCornerShape(buttonRounding.dp),
                colors = buttonCol,
                contentPadding = PaddingValues(buttonPadding.dp),
                modifier = buttonSize
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Outlined.Home,
                        contentDescription = "Home"
                    )
                    Text(
                        "Home",
                        fontSize = fontSize
                    )
                }
            }
            Button(
                onClick = {},
                shape = RoundedCornerShape(buttonRounding.dp),
                colors = buttonCol,
                contentPadding = PaddingValues(buttonPadding.dp),
                modifier = buttonSize
            ){
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Outlined.NearMe,
                        contentDescription = "Map"
                    )
                    Text(
                        "Map",
                        fontSize = fontSize
                    )
                }
            }
            IconButton(
                onClick = { /* do something */ },
                modifier = buttonSize
            ) {
                Icon(
                    Icons.Outlined.AddBox,
                    contentDescription = "Create Event",
                    modifier = Modifier.size(iconSize.dp),
                    tint = Purple40
                    )
            }
            Button(
                onClick = {},
                shape = RoundedCornerShape(buttonRounding.dp),
                colors = buttonCol,
                contentPadding = PaddingValues(buttonPadding.dp),
                modifier = buttonSize
            ){
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    IconButton(
                        onClick = {},
                        modifier = Modifier.padding(paddingH.dp,paddingV.dp)
                    ){
                    Icon(
                        Icons.Outlined.ConfirmationNumber,
                        contentDescription = "Activities"
                    )
                    }
                    Text(
                        "Activities",
                        fontSize = fontSize
                    )
                }
            }
            Button(
                onClick = {},
                shape = RoundedCornerShape(buttonRounding.dp),
                colors = buttonCol,
                contentPadding = PaddingValues(buttonPadding.dp),
                modifier = buttonSize
            ){
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Outlined.AccountCircle,
                        contentDescription = "Profile",
                    )
                    Text(
                        "Profile",
                        fontSize = fontSize
                    )
                }
            }
        },
        containerColor = PurpleGrey80,
        contentPadding = PaddingValues(paddingH.dp,paddingV.dp),
        modifier = Modifier.clip(RoundedCornerShape(barRoundingPercentage))
            .size(width.dp, height.dp)
    )
}