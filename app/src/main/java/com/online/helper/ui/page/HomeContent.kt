package com.online.helper.ui.page

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.online.helper.route.Route
import com.online.helper.ui.theme.appPadding
import com.online.helper.ui.theme.cardRoundedCorner
import com.online.helper.ui.theme.floatingButtonPadding
import com.online.helper.ui.theme.textPadding

@Composable
fun HomeContent(
    appNavController: NavHostController,
    scaffoldNavController: NavHostController,

    ) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = appPadding, end = appPadding)
//            .background(Color(23,32,43))
    ) {
        ElevatedButton(
            onClick = {appNavController.navigate(Route.about.value)},
            shape = RoundedCornerShape(cardRoundedCorner),
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)

        ) {
            Text(
                text = "未启动",
                fontSize = 30.sp,
                modifier = Modifier
            )
        }
        Spacer(modifier = Modifier.height(30.dp))

        OutlinedCard(
            modifier = Modifier
                .padding(bottom = floatingButtonPadding)
                .fillMaxWidth()
                .align(Alignment.CenterHorizontally)


        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
//                    .background(Color.Cyan)

            ) {
                Text(
                    text = "房间列表",
                    modifier = Modifier
                        .padding(
                            top = textPadding,
                            bottom = textPadding
                        )
                        .align(Alignment.Center)
                )

            }

            LazyColumn(
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                item {
                }
                items(5) {
                    ElevatedButton(
                        onClick = { scaffoldNavController.navigate(Route.roomInfo.value) },

                        shape = RoundedCornerShape(cardRoundedCorner),
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(60.dp)
                    ) {
                        Box(
                            modifier = Modifier
//                                .background(Color.Cyan)
                                .fillMaxWidth()
                        ) {
                            Icon(
                                Icons.Filled.Done, contentDescription = null,
                                modifier = Modifier.align(Alignment.CenterStart)
                            )
                            Text(text = "房间A", modifier = Modifier.align(Alignment.Center))
                            Icon(
                                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                modifier = Modifier.align(Alignment.CenterEnd)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(30.dp))
                }
            }
        }

    }
}