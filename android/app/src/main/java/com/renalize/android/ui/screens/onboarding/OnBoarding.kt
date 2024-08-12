package com.renalize.android.ui.screens.onboarding

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.renalize.android.ui.components.SteppedProgressBar
import com.renalize.android.ui.screens.onboarding.steps.identity.IdentityVerificationScreen
import com.renalize.android.ui.screens.onboarding.steps.personal_details.PersonalDetailScreen
import kotlinx.coroutines.launch

@Composable
fun OnBoarding(
    onFinished: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState (
        pageCount = {2}
    )
    val coroutineScope = rememberCoroutineScope()

    Scaffold { innerPadding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .imePadding()
        ) {
            Row(
                modifier = Modifier.padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Back",
                    modifier = Modifier.clickable { onBack() })
                Text(
                    text = "Step ${pagerState.currentPage+1}/${pagerState.pageCount}",
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }
            
            SteppedProgressBar(
                totalSteps = pagerState.pageCount,
                currentStep = pagerState.currentPage+1,
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            HorizontalPager(
                state = pagerState,
                userScrollEnabled = false
            ) { pageIndex ->
                when(pageIndex) {
                    0 -> IdentityVerificationScreen(
                        onProceed = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage+1)
                            }
                        }
                    )
                    1 -> PersonalDetailScreen(
                        onProceed = { onFinished() }
                    )
                }
            }
        }
    }

}