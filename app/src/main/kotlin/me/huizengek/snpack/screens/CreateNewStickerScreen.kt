package me.huizengek.snpack.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.navigate
import kotlinx.coroutines.launch
import me.huizengek.snpack.Database
import me.huizengek.snpack.LocalNavigator
import me.huizengek.snpack.R
import me.huizengek.snpack.stickers.StickerRepository
import me.huizengek.snpack.destinations.StickerPackScreenDestination
import me.huizengek.snpack.destinations.StickerScreenDestination
import me.huizengek.snpack.util.px
import me.huizengek.snpack.util.toDp
import me.huizengek.snpack.stickers.toSticker
import me.huizengek.snpack.ui.components.ColorSelector
import me.huizengek.snpack.ui.components.EmojiDisplay
import me.huizengek.snpack.ui.components.NavigationAwareBack
import me.huizengek.snpack.ui.components.StickerEmojiPicker
import me.huizengek.snpack.ui.components.StickerPreview
import me.huizengek.snpack.ui.components.TopAppBarTitle
import me.huizengek.snpack.util.uriSaver

@OptIn(ExperimentalMaterial3Api::class, ExperimentalGlideComposeApi::class)
@Destination
@Composable
fun CreateNewStickerScreen(packId: Long) {
    val context = LocalContext.current
    val navigator = LocalNavigator.current

    val pack by Database.pack(packId).collectAsState(initial = null)

    var imageUri by rememberSaveable(stateSaver = uriSaver) { mutableStateOf(null) }
    val picker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = {
            imageUri = it
        }
    )

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val emojis = remember { mutableStateMapOf<Int, String>() }
    var idx by rememberSaveable { mutableIntStateOf(0) }

    var text by rememberSaveable { mutableStateOf("") }
    var size by rememberSaveable { mutableFloatStateOf(24f) }
    var stroke by rememberSaveable { mutableFloatStateOf(8f) }

    var fillColor by remember { mutableStateOf(Color.White) }
    var strokeColor by remember { mutableStateOf(Color.Black) }

    var tab by rememberSaveable { mutableIntStateOf(0) }

    pack?.let { actualPack ->
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { TopAppBarTitle(title = "Nieuwe sticker") },
                    navigationIcon = { NavigationAwareBack() }
                )
            },
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
        ) { paddingValues ->
            val scrollState = rememberScrollState()
            var pastEmojiHeightPx by remember { mutableFloatStateOf(0f) }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .defaultMinSize(minHeight = 160.dp)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TabRow(
                    selectedTabIndex = tab,
                    indicator = { tabPositions ->
                        if (tab < tabPositions.size) {
                            val width by animateDpAsState(
                                targetValue = tabPositions[tab].contentWidth,
                                label = ""
                            )
                            TabRowDefaults.PrimaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(tabPositions[tab]),
                                width = width,
                                shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
                            )
                        }
                    }
                ) {
                    Tab(
                        selected = tab == 0,
                        onClick = { tab = 0 },
                        text = { Text(text = "Afbeelding") },
                        icon = {
                            Icon(
                                painter = painterResource(id = R.drawable.image),
                                contentDescription = null
                            )
                        }
                    )
                    Tab(
                        selected = tab == 1,
                        onClick = { tab = 1 },
                        text = { Text(text = "Tekst") },
                        icon = {
                            Icon(
                                painter = painterResource(id = R.drawable.text),
                                contentDescription = null
                            )
                        }
                    )
                }

                if (tab == 0) {
                    GlideImage(
                        model = imageUri,
                        contentDescription = null,
                        modifier = Modifier
                            .size(128.dp)
                            .let { if (imageUri == null) it.background(Color.Black.copy(alpha = 0.5f)) else it }
                            .padding(12.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ElevatedButton(onClick = {
                        picker.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }) {
                        Icon(imageVector = Icons.Filled.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))
                        Text(text = "Kies sticker")
                    }
                } else {
                    OutlinedTextField(
                        value = text,
                        onValueChange = { text = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        placeholder = { Text(text = "bijv. 'dat gaat op Zilliz'") },
                        label = { Text(text = "Tekst") }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Tekstgrootte",
                        modifier = Modifier
                            .align(Alignment.Start)
                            .padding(start = 16.dp)
                    )
                    Slider(
                        value = size,
                        onValueChange = { size = it },
                        valueRange = 2f..128f,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    Text(
                        text = "Contourdikte",
                        modifier = Modifier
                            .align(Alignment.Start)
                            .padding(start = 16.dp)
                    )
                    Slider(
                        value = stroke,
                        onValueChange = { stroke = it },
                        valueRange = 0f..48f,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    ColorSelector(
                        name = "Kleur tekst",
                        color = fillColor,
                        setColor = { fillColor = it },
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    ColorSelector(
                        name = "Kleur contour",
                        color = strokeColor,
                        setColor = { strokeColor = it },
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    StickerPreview(
                        text = text,
                        size = size,
                        stroke = stroke,
                        fillColor = fillColor.toArgb(),
                        strokeColor = strokeColor.toArgb(),
                        modifier = Modifier.size(512.px.toDp())
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                EmojiDisplay(
                    index = idx,
                    setIndex = { idx = it },
                    emojis = emojis,
                    modifier = Modifier.onGloballyPositioned {
                        pastEmojiHeightPx = it.positionInParent().y + it.size.height
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        coroutineScope.launch {
                            val actualEmojis = emojis.values.toList()
                            if (actualEmojis.firstOrNull()
                                    .isNullOrBlank()
                            ) return@launch snackbarHostState
                                .showSnackbar("Er is op z'n minst één emoji nodig voor deze sticker!")
                                .let { }

                            val sticker = with(context) {
                                val stickerImage =
                                    if (tab == 0) imageUri?.toSticker()
                                        ?: return@launch snackbarHostState
                                            .showSnackbar("Er is geen sticker gekozen, kies deze eerst")
                                            .let { }
                                    else text.ifBlank { null }?.toSticker(
                                        size = size,
                                        stroke = stroke,
                                        fillColor = fillColor.toArgb(),
                                        strokeColor = strokeColor.toArgb()
                                    ) ?: return@launch snackbarHostState
                                        .showSnackbar("Er is geen tekst ingevoerd, doe dat eerst")
                                        .let { }
                                StickerRepository.insertSticker(
                                    pack = actualPack.pack,
                                    image = stickerImage,
                                    emojis = actualEmojis
                                ).also { stickerImage.recycle() }
                            }
                            if (sticker != null)
                                navigator.navigate(StickerScreenDestination(stickerId = sticker.id)) {
                                    popUpTo(StickerPackScreenDestination.route)
                                }
                        }
                    },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Icon(imageVector = Icons.Filled.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))
                    Text(text = "Opslaan")
                }

                Spacer(modifier = Modifier.height(8.dp))

                StickerEmojiPicker(onPicked = {
                    emojis[idx] = it
                    idx = (idx + 1) % 3
                })
            }

            AnimatedVisibility(
                visible = scrollState.value > pastEmojiHeightPx,
                enter = slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                Box(
                    modifier = Modifier
                        .padding(paddingValues)
                        .padding(top = 8.dp)
                ) {
                    EmojiDisplay(index = idx, setIndex = { idx = it }, emojis = emojis)
                }
            }
        }
    }
}