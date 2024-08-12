package com.renalize.android.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.renalize.android.R
import com.renalize.android.ui.theme.HealthcareTheme
import com.renalize.android.ui.util.dashedBorder
import com.renalize.android.util.FileProvider

@Composable
fun DocUpload(
    title: String? = null,
    supportingText: String? = null,
    onImageSelected: (List<Uri>) -> Unit,
    modifier: Modifier = Modifier
) {
    val ctx = LocalContext.current

    var selectedImageUri by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var hasImage by remember { mutableStateOf(false) }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia()
    ) {
        hasImage = it.isNotEmpty()
        selectedImageUri = it
    }

    val openCamera = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) {
        hasImage = it
    }

    LaunchedEffect(selectedImageUri, hasImage) {
        if (hasImage && selectedImageUri.isNotEmpty()) {
            onImageSelected(selectedImageUri)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        title?.let { Text(text = it, fontSize = 18.sp, fontWeight = FontWeight.W600) }
        supportingText?.let { Text(it, fontSize = 12.sp, color = Color.Gray) }
        Spacer(modifier = modifier.size(24.dp))
        Row {
            AppOutlinedButton(
                onClick = {
                    selectedImageUri = listOf(FileProvider.getImageUri(ctx))
                    openCamera.launch(selectedImageUri.first())
                },
                modifier = Modifier
                    .weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = R.drawable.ic_camera),
                        contentDescription = "Add Circle",
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text("Take photo")
                }
            }
            Spacer(modifier = Modifier.size(16.dp))
            AppOutlinedButton(
                onClick = {
                    photoPicker.launch(
                        PickVisualMediaRequest(
                            ActivityResultContracts.PickVisualMedia.ImageOnly
                        )
                    )
                },
                modifier = Modifier
                    .weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = R.drawable.ic_upload),
                        contentDescription = "Add Circle"
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text("Upload")
                }
            }
        }
    }
}

@Composable
fun PhotoPicker(
    title: String,
    selectedImageUri: Uri?,
    onImageSelected: (Uri) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val ctx = LocalContext.current
    var tempImageUri by remember { mutableStateOf<Uri?>(null) }
    var hasImage by remember { mutableStateOf(selectedImageUri != null) }
    var showOptionsSheet by remember { mutableStateOf(false) }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) {
        it?.let{
            onImageSelected(it)
        }
    }

    val openCamera = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) {
        hasImage =  it
        if(it) onImageSelected(tempImageUri!!)
    }

    if (showOptionsSheet) {
        BottomSheet(
            title = title,
            isRetake = hasImage,
            onDismiss = { showOptionsSheet = false },
            onCamera = {
                tempImageUri = FileProvider.getImageUri(ctx)
                openCamera.launch(tempImageUri!!)
                showOptionsSheet = false
            },
            onUpload = {
                photoPicker.launch(
                    PickVisualMediaRequest(
                        ActivityResultContracts.PickVisualMedia.ImageOnly
                    )
                )
                showOptionsSheet = false
            },
            onDelete = {
                hasImage = false
                showOptionsSheet = false
            }
        )
    }

    if (selectedImageUri == null) {
        Box(
            modifier = modifier
                .dashedBorder(
                    1.dp,
                    if (enabled) Color.Gray else Color.LightGray,
                    cornerRadiusDp = 8.dp
                )
                .fillMaxWidth()
                .aspectRatio(2f)
                .clickable(enabled) { showOptionsSheet = true }
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.background(
                        if (enabled) MaterialTheme.colorScheme.primaryContainer else Color.LightGray,
                        CircleShape
                    )
                ) {
                    Icon(
                        Icons.Default.Add,
                        modifier = Modifier
                            .padding(8.dp)
                            .size(24.dp),
                        contentDescription = "Add Image",
                        tint = if (enabled) Color.Black else Color.DarkGray
                    )
                }
                Text(
                    text = title,
                    color = if (enabled) Color.Black else Color.DarkGray
                )
            }
        }
    } else{
        Box {
            AsyncImage(
                model = selectedImageUri,
                contentDescription = "Prescription Image",
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth()
                    .aspectRatio(2f)
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            FilledIconButton(
                onClick = { showOptionsSheet = true },
                modifier = Modifier
                    .size(20.dp)
                    .align(Alignment.TopEnd),
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add Image",
                    modifier = Modifier
                        .rotate(45f)
                        .size(12.dp),
                    tint = if (enabled) Color.White else Color.DarkGray
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheet(
    title: String,
    isRetake: Boolean = false,
    onDismiss: () -> Unit,
    onCamera: () -> Unit,
    onUpload: () -> Unit,
    onDelete: () -> Unit
) {
    val modalBottomSheetState = rememberModalBottomSheetState()

    @Composable
    fun BottomSheetItem(
        onClick: () -> Unit,
        icon: ImageVector,
        text: String,
        color: Color = MaterialTheme.colorScheme.primary
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
        ) {
            Icon(
                icon,
                contentDescription = text,
                tint = color
            )
            Text(
                text,
                color =color,
                modifier = Modifier.padding(start = 8.dp),
                fontSize = 16.sp,
                fontWeight = FontWeight.W500
            )
        }
    }

    AppModalBottomSheet(
        onDismissRequest = { onDismiss() },
        sheetState = modalBottomSheetState,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Row(
                modifier = Modifier.padding(bottom = 12.dp)
            ){
                Text(
                    title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.W500,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    Icons.Default.Add,
                    modifier = Modifier
                        .rotate(45f)
                        .clickable { onDismiss() },
                    contentDescription = "dismiss",
                )
            }
            BottomSheetItem(
                onClick = { onCamera() },
                icon = ImageVector.vectorResource(id = R.drawable.ic_camera),
                text = if(isRetake) "Retake Photo" else "Take a Photo"
            )
            Divider()
            BottomSheetItem(
                onClick = { onUpload() },
                icon = ImageVector.vectorResource(id = R.drawable.ic_upload),
                text = if(isRetake) "Upload Again" else "Upload"
            )
            Divider()
            BottomSheetItem(
                onClick = { onDelete() },
                icon = Icons.Outlined.Delete,
                text = "Delete",
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.size(36.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DocUploadPreview() {
    HealthcareTheme {
        DocUpload(
            title = "Upload Prescription",
            supportingText = "Upload your prescription to get started",
            onImageSelected = {}
        )
    }
}
