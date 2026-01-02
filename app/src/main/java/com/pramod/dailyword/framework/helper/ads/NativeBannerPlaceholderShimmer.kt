package com.pramod.dailyword.framework.helper.ads

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pramod.dailyword.framework.ui.common.shimmer

@Preview
@Composable
fun NativeBannerPlaceholderShimmer() {
    Row(
        modifier = Modifier.height(60.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(
            modifier = Modifier
                .padding(12.dp)
                .size(24.dp)
                .shimmer(4.dp)
        )

        Column(modifier = Modifier.weight(1f)) {

            Spacer(
                modifier = Modifier
                    .height(20.dp)
                    .fillMaxWidth(0.75f)
                    .shimmer(2.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Spacer(
                modifier = Modifier
                    .height(20.dp)
                    .fillMaxWidth(0.95f)
                    .shimmer(2.dp)
            )

        }

        Spacer(
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .width(60.dp)
                .height(25.dp)
                .shimmer(4.dp)
        )

    }
}
