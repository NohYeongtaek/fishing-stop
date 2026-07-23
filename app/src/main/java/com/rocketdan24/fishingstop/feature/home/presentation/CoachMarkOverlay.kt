package com.rocketdan24.fishingstop.feature.home.presentation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.rocketdan24.fishingstop.R
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.roundToInt

/** 코치마크가 대상 영역을 뚫어 보여줄 때 쓰는 모양. */
enum class CoachMarkShape { CIRCLE, RECT }

internal data class CoachMarkTargetInfo(
    val shape: CoachMarkShape,
    val bounds: Rect,
    val tooltip: @Composable () -> Unit
)

/**
 * 홈 탭 코치마크 진행 상태. [stepCount]개의 순번(0..stepCount-1)을 순서대로 밟아나간다.
 * 각 타겟은 화면에 실제로 배치될 때 [coachMarkTarget] modifier로 위치/모양/설명을 등록한다.
 */
class CoachMarkState(private val stepCount: Int) {
    var currentIndex by mutableIntStateOf(0)
        private set

    internal val targets = mutableStateMapOf<Int, CoachMarkTargetInfo>()

    internal fun register(index: Int, info: CoachMarkTargetInfo) {
        targets[index] = info
    }

    /** 현재 타겟을 탭했을 때 호출한다. 다음 단계로 넘어가고, 모든 단계가 끝났으면 true를 반환한다. */
    fun advance(): Boolean {
        currentIndex++
        return currentIndex >= stepCount
    }
}

@Composable
fun rememberCoachMarkState(stepCount: Int): CoachMarkState =
    remember(stepCount) { CoachMarkState(stepCount) }

/** Compose 요소를 코치마크 타겟(순번 [index])으로 등록한다. */
fun Modifier.coachMarkTarget(
    state: CoachMarkState,
    index: Int,
    shape: CoachMarkShape,
    tooltip: @Composable () -> Unit
): Modifier = onGloballyPositioned { coordinates ->
    state.register(index, CoachMarkTargetInfo(shape, coordinates.boundsInRoot(), tooltip))
}

/**
 * 코치마크 오버레이. 화면 전체를 검은 반투명으로 덮고, 현재 타겟 부분만 뚫어서 보여준다.
 * CIRCLE 타겟은 원형으로, RECT 타겟은 요소 크기에 맞춘 사각형으로 뚫는다(장식 없이 순수 컷아웃).
 * 뚫린 영역을 탭하면 다음 순서로 넘어가고, 마지막 단계에서 탭하면 [onCompleted]가 호출된다.
 */
@Composable
fun CoachMarkOverlay(
    state: CoachMarkState,
    onCompleted: () -> Unit
) {
    val target = state.targets[state.currentIndex] ?: return
    val density = LocalDensity.current
    var overlayOrigin by remember { mutableStateOf(Offset.Zero) }

    // 다음 타겟으로 실제로 넘어갈 때만 부드럽게 이동시키고, 최초 노출이나 레이아웃이 안정화되며
    // 같은 단계의 좌표가 보정되는 경우(예: 인셋 반영 전후)는 애니메이션 없이 즉시 맞춘다.
    // 그렇지 않으면 처음 잡힌 좌표가 아직 부정확할 때 엉뚱한 위치에서 튀어와 보이는 문제가 생긴다.
    val moveSpec = tween<Float>(durationMillis = 450, easing = FastOutSlowInEasing)
    val holeLeft = remember { Animatable(target.bounds.left) }
    val holeTop = remember { Animatable(target.bounds.top) }
    val holeRight = remember { Animatable(target.bounds.right) }
    val holeBottom = remember { Animatable(target.bounds.bottom) }
    var lastAnimatedIndex by remember { mutableStateOf(-1) }

    LaunchedEffect(state.currentIndex, target.bounds) {
        val isStepAdvance = lastAnimatedIndex != -1 && lastAnimatedIndex != state.currentIndex
        lastAnimatedIndex = state.currentIndex
        if (isStepAdvance) {
            launch { holeLeft.animateTo(target.bounds.left, moveSpec) }
            launch { holeTop.animateTo(target.bounds.top, moveSpec) }
            launch { holeRight.animateTo(target.bounds.right, moveSpec) }
            launch { holeBottom.animateTo(target.bounds.bottom, moveSpec) }
        } else {
            holeLeft.snapTo(target.bounds.left)
            holeTop.snapTo(target.bounds.top)
            holeRight.snapTo(target.bounds.right)
            holeBottom.snapTo(target.bounds.bottom)
        }
    }

    val animatedBounds = Rect(holeLeft.value, holeTop.value, holeRight.value, holeBottom.value)
    val localHoleBounds = animatedBounds.translate(-overlayOrigin.x, -overlayOrigin.y)
    // 설명 문구는 애니메이션 없이 최종 위치에 바로 노출한다(스포트라이트만 부드럽게 이동).
    val localTooltipBounds = target.bounds.translate(-overlayOrigin.x, -overlayOrigin.y)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned { overlayOrigin = it.positionInRoot() }
            .pointerInput(state.currentIndex, target.bounds) {
                detectTapGestures { offset ->
                    if (target.bounds.contains(offset + overlayOrigin)) {
                        if (state.advance()) onCompleted()
                    }
                }
            }
            .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
            .drawWithContent {
                val holePadding = with(density) { 12.dp.toPx() }
                drawRect(color = Color.Black.copy(alpha = 0.75f))
                when (target.shape) {
                    CoachMarkShape.CIRCLE -> {
                        val radius = max(localHoleBounds.width, localHoleBounds.height) / 2f + holePadding
                        drawCircle(
                            color = Color.Black,
                            radius = radius,
                            center = localHoleBounds.center,
                            blendMode = BlendMode.Clear
                        )
                    }
                    CoachMarkShape.RECT -> {
                        drawRoundRect(
                            color = Color.Black,
                            topLeft = Offset(localHoleBounds.left - holePadding, localHoleBounds.top - holePadding),
                            size = Size(localHoleBounds.width + holePadding * 2, localHoleBounds.height + holePadding * 2),
                            cornerRadius = CornerRadius(with(density) { 16.dp.toPx() }),
                            blendMode = BlendMode.Clear
                        )
                    }
                }
                drawContent()
            }
    ) {
        CoachMarkTooltipPosition(targetBounds = localTooltipBounds, content = target.tooltip)
    }
}

/**
 * 타겟 위/아래 중 공간이 있는 쪽에 툴팁을 배치한다(좌우 화면 경계도 벗어나지 않게 보정).
 * 정확한 위치는 실제로 그려본 뒤(크기를 알아야) 계산할 수 있으므로, 그 전까지는 투명하게 감춰서
 * "잘못된 위치에 잠깐 나타났다가 이동"하는 것처럼 보이지 않게 한다.
 */
@Composable
private fun CoachMarkTooltipPosition(targetBounds: Rect, content: @Composable () -> Unit) {
    val density = LocalDensity.current
    var offset by remember(targetBounds) {
        mutableStateOf(IntOffset(targetBounds.left.roundToInt(), targetBounds.bottom.roundToInt()))
    }
    var positioned by remember(targetBounds) { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .offset { offset }
            .alpha(if (positioned) 1f else 0f)
            .onGloballyPositioned { coords ->
                val gap = with(density) { 16.dp.toPx() }
                val w = coords.size.width
                val h = coords.size.height
                val aboveY = targetBounds.top - gap - h
                val y = if (aboveY > 0f) aboveY else targetBounds.bottom + gap
                val screenWidth = coords.parentLayoutCoordinates?.size?.width?.toFloat()
                    ?: (targetBounds.right + gap)
                val maxX = max(gap, screenWidth - w - gap)
                val x = targetBounds.left.coerceIn(gap, maxX)
                offset = IntOffset(x.roundToInt(), y.roundToInt())
                positioned = true
            }
    ) {
        content()
    }
}

/**
 * "홈" 탭과 "직접검사" 탭 코치마크 사이에 끼워 넣는 사진 가이드.
 * 메시지 앱에서 공유 → 피싱멈춰! 로 검사하는 과정을 스크린샷으로 순서대로 보여준다.
 * 다른 코치마크 단계와 동일하게 화면 어디를 탭해도 다음 장으로 넘어가고,
 * 마지막 장에서 탭하면 [onCompleted]가 호출된다.
 * 기기별로 공유 메뉴 모양이 달라지는 지점(길게 눌러 나오는 메뉴)은 두 장을 나란히 참고로
 * 보여줄 뿐, 어느 쪽을 골라도(혹은 다른 곳을 탭해도) 다음 장으로 넘어간다.
 */
private val photoGuideSteps: List<List<Int>> = listOf(
    listOf(R.drawable.coachmark_1),
    listOf(R.drawable.coachmark_2),
    listOf(R.drawable.coachmark_3_1, R.drawable.coachmark_3_2),
    listOf(R.drawable.coachmark_4),
    listOf(R.drawable.coachmark_5),
    listOf(R.drawable.coachmark_6),
    listOf(R.drawable.coachmark_7)
)

@Composable
fun PhotoGuideOverlay(onCompleted: () -> Unit) {
    var stepIndex by remember { mutableIntStateOf(0) }
    val images = photoGuideSteps[stepIndex]
    val isLastStep = stepIndex == photoGuideSteps.lastIndex

    fun goNext() {
        if (isLastStep) onCompleted() else stepIndex++
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.92f))
            .pointerInput(stepIndex) { detectTapGestures { goNext() } },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (images.size == 1) {
                Image(
                    painter = painterResource(images[0]),
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth(0.86f),
                    contentScale = ContentScale.FillWidth
                )
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    images.forEach { res ->
                        Image(
                            painter = painterResource(res),
                            contentDescription = null,
                            modifier = Modifier.weight(1f),
                            contentScale = ContentScale.FillWidth
                        )
                    }
                }
            }

            Text(
                text = "화면을 터치하면 다음으로 넘어가요 (${stepIndex + 1}/${photoGuideSteps.size})",
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}
