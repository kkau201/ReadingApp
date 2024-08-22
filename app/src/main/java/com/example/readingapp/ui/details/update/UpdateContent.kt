package com.example.readingapp.ui.details.update

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Divider
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material.icons.rounded.StarRate
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.readingapp.R
import com.example.readingapp.model.MBook
import com.example.readingapp.ui.components.BookStatus
import com.example.readingapp.ui.components.BookStatusButton
import com.example.readingapp.ui.theme.AppTheme
import com.example.readingapp.ui.theme.AppTheme.spacing

@Composable
fun UpdateScreenContent(
    modifier: Modifier = Modifier,
    book: MBook,
    selectedStatus: BookStatus,
    selectedRating: Int,
    noteInput: String,
    onNoteInputChanged: (String) -> Unit,
    onStatusChanged: (BookStatus) -> Unit,
    onRatingChanged: (Int) -> Unit,
    onSaveClick: () -> Unit,
    onCancelClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(horizontal = spacing.lgSpacing)
    ) {
        book.title?.let {
            Text(
                text = it,
                textAlign = TextAlign.Center,
                style = AppTheme.typography.titleLarge,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(bottom = spacing.mdSpacing)
            )
        }

        Column(
            Modifier
                .padding(top = spacing.smSpacing)
                .background(shape = RoundedCornerShape(spacing.smSpacing), color = AppTheme.colors.surface)
                .fillMaxWidth()
                .padding(spacing.smSpacing)
        ) {
            UpdateInputField(noteInput = noteInput, onNoteInputChanged = onNoteInputChanged)

            UpdateBookStatus(selectedStatus = selectedStatus, onStatusChanged = onStatusChanged)

            UpdateRating(selectedRating = selectedRating, onRatingChanged = onRatingChanged)

            Divider(color = AppTheme.colors.onSurface, modifier = Modifier.padding(top = spacing.mdSpacing))

            UpdateButtons(onSaveClick = onSaveClick, onCancelClick = onCancelClick)

        }
    }
}

@Composable
fun UpdateInputField(
    color: Color = AppTheme.colors.onSurface,
    noteInput: String,
    onNoteInputChanged: (String) -> Unit
) {
    val textFieldColors = TextFieldDefaults.outlinedTextFieldColors(
        textColor = color,
        unfocusedLabelColor = color.copy(alpha = 0.5f),
        unfocusedBorderColor = color.copy(alpha = 0.5f),
        focusedBorderColor = color,
        focusedLabelColor = color,
        cursorColor = color
    )

    OutlinedTextField(
        label = { Text(text = "Enter your thoughts") },
        value = noteInput,
        minLines = 10,
        shape = RoundedCornerShape(spacing.smSpacing),
        colors = textFieldColors,
        onValueChange = onNoteInputChanged,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = spacing.smSpacing)
    )
}

@Composable
fun UpdateBookStatus(
    color: Color = AppTheme.colors.onSurface,
    selectedStatus: BookStatus,
    onStatusChanged: (BookStatus) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = spacing.smSpacing)
    ) {
        BookStatus.entries.forEach { status ->
            BookStatusButton(
                status = status,
                color = color,
                isSelected = selectedStatus == status,
                onClick = { onStatusChanged(status) }
            )
        }
    }
}

@Composable
fun UpdateRating(
    color: Color = AppTheme.colors.onSurface,
    stars: Int = 5,
    selectedRating: Int = 0,
    onRatingChanged: (Int) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(spacing.smSpacing)
    ) {
        for (i in 1..stars) {
            IconButton(onClick = { onRatingChanged(i) }) {
                Icon(
                    imageVector = if (i <= selectedRating) Icons.Rounded.StarRate else Icons.Rounded.StarBorder,
                    contentDescription = stringResource(id = R.string.cont_desc_star_number, i),
                    tint = color,
                    modifier = Modifier.size(40.dp)
                )
            }
        }
    }
}

@Composable
fun UpdateButtons(
    modifier: Modifier = Modifier,
    saveColor: Color = AppTheme.colors.onSurface,
    cancelColor: Color = AppTheme.colors.onSurface.copy(alpha = 0.7f),
    horizontalArrangement: Arrangement.Horizontal = Arrangement.SpaceEvenly,
    onSaveClick: () -> Unit,
    onCancelClick: () -> Unit
) {
    Row(
        horizontalArrangement = horizontalArrangement,
        modifier = modifier
            .fillMaxWidth()
            .padding(top = spacing.xsmSpacing)
    ) {
        Button(
            colors = ButtonDefaults.buttonColors(backgroundColor = Color.Transparent),
            elevation = ButtonDefaults.elevation(0.dp),
            onClick = onCancelClick
        ) {
            Icon(
                imageVector = Icons.Rounded.Close,
                contentDescription = null,
                tint = cancelColor,
                modifier = Modifier
                    .padding(end = spacing.xsmSpacing)
                    .alpha(0.7f)
            )
            Text(text = stringResource(R.string.update_cancel), color = cancelColor)
        }
        Button(
            colors = ButtonDefaults.buttonColors(backgroundColor = Color.Transparent),
            elevation = ButtonDefaults.elevation(0.dp),
            onClick = onSaveClick
        ) {
            Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = null,
                tint = saveColor,
                modifier = Modifier.padding(end = spacing.xsmSpacing)
            )
            Text(text = stringResource(R.string.update_save), fontWeight = FontWeight.Bold, color = saveColor)
        }
    }
}