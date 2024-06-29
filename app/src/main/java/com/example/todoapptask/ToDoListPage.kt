
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.todoapptask.R
import com.example.todoapptask.ToDo
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoListPage(navController: NavController) {
    val todoViewModel: TodoViewModel = viewModel()
    val todoList by todoViewModel.todoList
    val completedTaskCount by todoViewModel.completedTaskCount

    var inputTitle by remember { mutableStateOf("") }
    var inputDescription by remember { mutableStateOf("") }
    val dialogVisibleState = remember { mutableStateOf(false) }
    var inputCompleted by remember { mutableStateOf(false) }
    var inputDate by remember { mutableStateOf(LocalDate.now()) }
    val congratsVisibleState = remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }

    val Renk = Color(0xFF945A5A)


    Box(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    Arrangement.SpaceAround
                    // horizontalArrangement = Arrangement.Center
                ) {
                    Button(
                        onClick = {
                            dialogVisibleState.value = true
                        },
                        modifier = Modifier.width(150.dp) ,
                        colors = ButtonDefaults.buttonColors(Renk)
                    ) {
                        Text(text = "Görev Ekle")
                    }
                    Spacer(modifier = Modifier.width(4.dp))

                    Button(
                        onClick = {
                            navController.navigate("kronometrePage")
                        },
                        modifier = Modifier.width(150.dp) ,
                        colors = ButtonDefaults.buttonColors(Renk)
                    ) {
                        Text(text = "Kronometre")
                    }
                }

                LazyColumn(content = {
                    itemsIndexed(todoList) { index: Int, item: ToDo ->
                        TodoItem(item = item, todoViewModel = todoViewModel)
                    }
                }
                )
            }
            WaterLevel(
                modifier = Modifier.padding(16.dp),
                toplamGorev = todoList.size,
                tamamlananGorev = completedTaskCount,
                hazneKapasitesi = 800,
                onAnimationEnd = { congratsVisibleState.value = true }
            )
        }
        if (congratsVisibleState.value) {
            ConfettiAnimation(
                modifier = Modifier.fillMaxSize(),
                onAnimationEnd = { congratsVisibleState.value = false }
            )
        }
    }

    if (dialogVisibleState.value) {
        AlertDialog(
            onDismissRequest = {
                dialogVisibleState.value = false
                inputTitle = ""
                inputDescription = ""
                inputCompleted = false
            },
            title = { Text(text = "Yeni Görev Ekle") },
            text = {
                Column {
                    OutlinedTextField(
                        value = inputTitle,
                        onValueChange = { inputTitle = it },
                        label = { Text("Başlık") }
                    )
                    OutlinedTextField(
                        value = inputDescription,
                        onValueChange = { inputDescription = it },
                        label = { Text("Açıklama") }
                    )
                    DatePicker(
                        selectedDate = selectedDate,
                        onDateSelected = { date ->
                            selectedDate = date
                        }
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = inputCompleted,
                            onCheckedChange = { inputCompleted = it }
                        )
                        Text(text = "Tamamlandı")
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        todoViewModel.viewModelScope.launch {
                            todoViewModel.gorevEkle(inputTitle, inputDescription, inputCompleted)
                        }
                        dialogVisibleState.value = false
                        inputTitle = ""
                        inputDescription = ""
                        inputCompleted = false
                    }
                ) {
                    Text("Ekle")
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        dialogVisibleState.value = false
                        inputTitle = ""
                        inputDescription = ""
                        inputCompleted = false
                    }
                ) {
                    Text("İptal")
                }
            }
        )
    }
}

@Composable
fun TodoItem(item: ToDo, todoViewModel: TodoViewModel) {
    var isCompleted by remember { mutableStateOf(item.yapildiMi) }

    val transition = updateTransition(targetState = isCompleted, label = "CompletionTransition")

    val bottleHeight by transition.animateDp(
        transitionSpec = { tween(durationMillis = 1000) },
        label = "BottleHeight"
    ) { completed ->
        if (completed) 100.dp else 0.dp
    }

    val Renk = Color(0xFF945A5A)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Renk)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = isCompleted,
            onCheckedChange = { isChecked ->
                isCompleted = isChecked
                // CheckBox'a tıklandığında veritabanındaki değeri güncelle
                val taskId = item.id // Görevin ID'sini buradan alın
                todoViewModel.viewModelScope.launch {
                    todoViewModel.gorevTamamlandi(taskId, isChecked)
                }
            },
            colors = CheckboxDefaults.colors(
                checkedColor = Color.Green,
                uncheckedColor = Color.White,
                checkmarkColor = Color.White
            )
        )


        Spacer(modifier = Modifier.width(8.dp))
        Column(
            modifier = Modifier.weight(1f)
        ) {

            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
            val date = LocalDateTime.parse(item.notTarihi.toString(), formatter)
            val formattedDate = date.toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))

            Text(text = formattedDate, fontSize = 10.sp, color = Color.LightGray)
            Text(text = item.baslik, fontSize = 18.sp, color = Color.LightGray)
            Text(text = item.aciklama, fontSize = 13.sp, color = Color.LightGray)
//            if (isCompleted) {
//                RisingWaterBottle(modifier = Modifier.size(50.dp), height = bottleHeight)
//            }
        }
        IconButton(onClick = {
            todoViewModel.viewModelScope.launch {
                todoViewModel.gorevSil(item.id)
            }

        }) {
            Icon(
                painter = painterResource(id = R.drawable.baseline_delete_24),
                contentDescription = "Delete",
                tint = Color.White
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePicker(selectedDate: LocalDate, onDateSelected: (LocalDate) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    OutlinedTextField(
        value = selectedDate.toString(),
        onValueChange = {}, // Tarih seçme bileşeni, metin alanı değerini doğrudan değiştirmemize izin vermez, bu nedenle boş bir işlev geçiyoruz
        label = { Text("Görev Oluşturma Tarihi") },
        readOnly = true,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = true }
    )
    if (expanded) {

    }
}

@Composable
fun WaterLevel(modifier: Modifier = Modifier, toplamGorev: Int, tamamlananGorev: Int, hazneKapasitesi: Int = 1000, onAnimationEnd: () -> Unit) {
    val gorevBasinaDusenSuMiktari = if (toplamGorev > 0) {
        hazneKapasitesi.toFloat() / toplamGorev
    } else {
        0f
    }

    val haznedeOlusanMiktar = tamamlananGorev * gorevBasinaDusenSuMiktari

    val suSeviyesi = haznedeOlusanMiktar.coerceIn(0f, hazneKapasitesi.toFloat())

    val suSeviyesiDp = with(LocalDensity.current) { suSeviyesi.dp }

    val animasonluSuYuksekligi by animateDpAsState(
        targetValue = suSeviyesiDp,
        animationSpec = tween(durationMillis = 1000)
    )

    var animasyonTamamlandiMi by remember { mutableStateOf(false) }

    // Görevler tamamlandığında animationComplete flag'ini kontrol et
    if (tamamlananGorev == toplamGorev && !animasyonTamamlandiMi) {
        LaunchedEffect(Unit) {
            delay(1000) // Tankın tamamen dolması için gecikme ekleyin
            onAnimationEnd() // Konfeti animasyonunu başlat
            animasyonTamamlandiMi = true // Animasyon tamamlandı olarak işaretle
        }
    }

    // Görevlerin tamamlandığından emin olunca animationComplete'i sıfırla
    LaunchedEffect(tamamlananGorev, toplamGorev) {
        animasyonTamamlandiMi = false
    }

    Box(
        modifier = modifier
            .width(60.dp)
            .height(hazneKapasitesi.dp)
            .clip(RoundedCornerShape(25.dp))
            .border(width = 2.dp, color = Color.Gray, shape = RoundedCornerShape(25.dp))
            .background(Color.LightGray.copy(alpha = 0.3f))
    ) {
        val animatedHeightPx = with(LocalDensity.current) { animasonluSuYuksekligi.toPx() }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(animasonluSuYuksekligi)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFCAF0F8),
                            Color(0xFFADE8F4),
                            Color(0xFF90E0EF),
                            Color(0xFF48CAE4),
                            Color(0xFF0077B6),
                        ),
                        startY = 0f,
                        endY = animatedHeightPx
                    )
                )
                .clip(RoundedCornerShape(bottomStart = 25.dp, bottomEnd = 25.dp))
                .align(Alignment.BottomCenter)
        )

    }
}

data class ConfettiParticle(
    val color: Color,
    val startX: Float,
    val startY: Float,
    val velocityX: Float,
    val velocityY: Float,
    val size: Float
)

@Composable
fun ConfettiAnimation(modifier: Modifier = Modifier, onAnimationEnd: () -> Unit) {
    val particles = remember { mutableStateListOf<ConfettiParticle>() }
    val random = Random.Default

    LaunchedEffect(Unit) {
        repeat(470) {
            particles.add(
                ConfettiParticle(
                    color = Color(
                        random.nextInt(256),
                        random.nextInt(256),
                        random.nextInt(256)
                    ),
                    startX = random.nextFloat() * 1000f, // Başlangıç x konumu
                    startY = random.nextFloat() * 100f, // Başlangıç y konumu
                    velocityX = (random.nextFloat() - 0.5f) * 4f, // X eksenindeki hız
                    velocityY = random.nextFloat() * 3f + 2f, // Y eksenindeki hız
                    size = random.nextFloat() * 15f + 10f
                )
            )
        }
        delay(4000) // Animation duration
        onAnimationEnd()
    }

    val infiniteTransition = rememberInfiniteTransition()
    val animProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Canvas(modifier = modifier) {
        drawConfettiParticles(particles, animProgress)
    }
}

fun DrawScope.drawConfettiParticles(particles: List<ConfettiParticle>, animProgress: Float) {
    particles.forEach { particle ->
        val x = particle.startX + particle.velocityX * animProgress * size.width
        val y = particle.startY + particle.velocityY * animProgress * size.height
        drawCircle(color = particle.color, radius = particle.size, center = Offset(x, y))
    }
}
