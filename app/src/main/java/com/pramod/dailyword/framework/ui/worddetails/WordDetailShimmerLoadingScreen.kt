package com.pramod.dailyword.framework.ui.worddetails

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pramod.dailyword.framework.ui.common.shimmer

@Preview
@Composable
fun WordDetailShimmerLoadingScreen() {


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {

        Spacer(Modifier.height(16.dp))

        Spacer(
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .height(30.dp)
                .shimmer(16.dp)
        )

        Spacer(Modifier.height(16.dp))

        Spacer(
            modifier = Modifier
                .fillMaxWidth(0.4f)
                .height(60.dp)
                .shimmer(4.dp)
        )

        Spacer(Modifier.height(16.dp))

        Row {
            Spacer(
                modifier = Modifier
                    .width(60.dp)
                    .height(35.dp)
                    .shimmer(4.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Spacer(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(35.dp)
                    .shimmer(4.dp)
            )

        }

        Spacer(Modifier.height(16.dp))

        Spacer(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .height(30.dp)
                .shimmer(4.dp)
        )


        Spacer(Modifier.height(32.dp))


        Spacer(
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .height(35.dp)
                .shimmer(4.dp)
        )

        Spacer(Modifier.height(12.dp))

        Column {
            repeat(2) {

                Spacer(
                    modifier = Modifier
                        .fillMaxWidth(if (it % 2 == 0) 0.8f else 0.9f)
                        .height(60.dp)
                        .shimmer(4.dp)
                )

                Spacer(Modifier.height(4.dp))

            }
        }


        Spacer(Modifier.height(32.dp))

        Spacer(
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .height(35.dp)
                .shimmer(4.dp)
        )

        Spacer(Modifier.height(12.dp))

        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(4) {
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth(0.2f)
                        .height(30.dp)
                        .shimmer(20.dp)
                )
            }
        }


        Spacer(Modifier.height(32.dp))

        Spacer(
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .height(35.dp)
                .shimmer(4.dp)
        )

        Spacer(Modifier.height(12.dp))

        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(4) {
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth(0.2f)
                        .height(30.dp)
                        .shimmer(20.dp)
                )
            }
        }


        Spacer(Modifier.height(32.dp))

        Spacer(
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .height(35.dp)
                .shimmer(4.dp)
        )

        Spacer(Modifier.height(12.dp))

        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .shimmer(4.dp)
        )

        Spacer(Modifier.height(16.dp))
    }

}