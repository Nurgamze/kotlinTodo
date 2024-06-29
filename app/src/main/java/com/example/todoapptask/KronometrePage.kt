
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun KronometrePage(navController: NavController) {
    var isCounting by remember { mutableStateOf(false) }
    var countSeconds by remember { mutableStateOf(0) }
    var countMinutes by remember { mutableStateOf(0) }
    var timerJob by remember { mutableStateOf<Job?>(null) }

    DisposableEffect(Unit) {
        timerJob = CoroutineScope(Dispatchers.Default).launch {
            while (true) {
                delay(1000)
                if (isCounting) {
                    withContext(Dispatchers.Main) {
                        countSeconds++
                        if (countSeconds == 60) {
                            countSeconds = 0
                            countMinutes++
                        }
                    }
                }
            }
        }
        onDispose {
            timerJob?.cancel()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Gray.copy(alpha = 0.5f)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            IconButton(
                onClick = {
                    navController.popBackStack()
                },
                modifier = Modifier.padding(start = 16.dp, top = 8.dp)
            ) {
                Icon(
                    Icons.Filled.ArrowBack,
                    contentDescription = "Geri",
                    tint = Color.White
                )
            }

            Text(
                text = "Kronometre",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .size(200.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .padding(8.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${countMinutes.formatTime()}:${countSeconds.formatTime()}",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    isCounting = !isCounting
                },
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = if (isCounting) Color.Red else Color.Green
                ),
                modifier = Modifier
                    .padding(horizontal = 32.dp)
                    .height(72.dp)
                    .fillMaxWidth()
                    .clip(shape = MaterialTheme.shapes.medium)
            ) {
                Text(
                    text = if (isCounting) "Dur" else "Başlat",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    countSeconds = 0
                    countMinutes = 0
                },
                modifier = Modifier
                    .padding(horizontal = 32.dp)
                    .height(72.dp)
                    .fillMaxWidth()
                    .clip(shape = MaterialTheme.shapes.medium)
            ) {
                Text(
                    text = "Sıfırla",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

private fun Int.formatTime(): String {
    return if (this < 10) "0$this" else "$this"
}
