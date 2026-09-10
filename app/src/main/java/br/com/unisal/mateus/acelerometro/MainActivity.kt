package br.com.unisal.mateus.acelerometro

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.unisal.mateus.acelerometro.ui.theme.AcelerometroTheme
import kotlin.math.pow
import kotlin.math.sqrt
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

class MainActivity : ComponentActivity(), SensorEventListener {

    var sensorManager: SensorManager? = null
    var sensor: Sensor? = null

    val gravidade = FloatArray(3)
    val aceleracaoLinear = FloatArray(3)

    // Valores que serão exibidos na interface
    var valorX by mutableStateOf(0f)
    var valorY by mutableStateOf(0f)
    var valorZ by mutableStateOf(0f)
    var valorGravidade by mutableStateOf(0f)
    var valorAceleracaoLinear by mutableStateOf(0f)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Obtém o serviço responsável pelos sensores do dispositivo
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager

        // Obtém o sensor do acelerômetro
        sensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        setContent {
            AcelerometroTheme {
                AcelerometroScreen(
                    valorX = valorX,
                    valorY = valorY,
                    valorZ = valorZ,
                    valorGravidade = valorGravidade,
                    valorAceleracaoLinear = valorAceleracaoLinear
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()

        // Começa a receber os dados do acelerômetro
        sensorManager?.registerListener(
            this,
            sensor,
            SensorManager.SENSOR_DELAY_NORMAL
        )
    }

    override fun onPause() {
        super.onPause()

        // Para a leitura do sensor quando a aplicação fica em segundo plano
        sensorManager?.unregisterListener(this)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // A precisão do sensor não será utilizada nesta aplicação
    }

    override fun onSensorChanged(evento: SensorEvent?) {

        if (evento?.sensor?.type != Sensor.TYPE_ACCELEROMETER)
            return

        // Valores recebidos pelo acelerômetro
        val x = evento.values[0]
        val y = evento.values[1]
        val z = evento.values[2]

        // Filtro utilizado para separar a gravidade da aceleração
        val alpha: Float = 0.8f

        gravidade[0] = alpha * gravidade[0] + (1 - alpha) * x
        gravidade[1] = alpha * gravidade[1] + (1 - alpha) * y
        gravidade[2] = alpha * gravidade[2] + (1 - alpha) * z

        // Calcula a aceleração linear
        aceleracaoLinear[0] = x - gravidade[0]
        aceleracaoLinear[1] = y - gravidade[1]
        aceleracaoLinear[2] = z - gravidade[2]

        // Calcula o módulo da aceleração da gravidade
        val acelGravidade = sqrt(
            gravidade[0].pow(2) +
                    gravidade[1].pow(2) +
                    gravidade[2].pow(2)
        )

        // Calcula o módulo da aceleração linear
        val acelLinear = sqrt(
            aceleracaoLinear[0].pow(2) +
                    aceleracaoLinear[1].pow(2) +
                    aceleracaoLinear[2].pow(2)
        )

        // Atualiza os valores exibidos na interface
        valorX = x
        valorY = y
        valorZ = z
        valorGravidade = acelGravidade
        valorAceleracaoLinear = acelLinear
    }
}


/*
 * Tela principal da aplicação.
 *
 * Os valores recebidos do sensor são apresentados
 * utilizando componentes do Material Design.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AcelerometroScreen(
    valorX: Float,
    valorY: Float,
    valorZ: Float,
    valorGravidade: Float,
    valorAceleracaoLinear: Float
) {

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Acelerômetro"
                    )
                }
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = "Monitoramento em tempo real",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Eixos do acelerômetro",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Cards dos três eixos
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                EixoCard(
                    modifier = Modifier.weight(1f),
                    eixo = "Eixo X",
                    valor = valorX
                )

                EixoCard(
                    modifier = Modifier.weight(1f),
                    eixo = "Eixo Y",
                    valor = valorY
                )

                EixoCard(
                    modifier = Modifier.weight(1f),
                    eixo = "Eixo Z",
                    valor = valorZ
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Aceleração",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Card da aceleração linear
            InformacaoCard(
                titulo = "Aceleração linear",
                valor = valorAceleracaoLinear
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Card da gravidade
            InformacaoCard(
                titulo = "Aceleração da gravidade",
                valor = valorGravidade
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "● Sensor ativo",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}


/*
 * Card utilizado para apresentar os valores X, Y e Z.
 */
@Composable
fun EixoCard(
    modifier: Modifier = Modifier,
    eixo: String,
    valor: Float
) {

    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = eixo,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "%.2f".format(valor),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "m/s²",
                fontSize = 12.sp
            )
        }
    }
}


/*
 * Card utilizado para apresentar a aceleração linear
 * e a aceleração da gravidade.
 */
@Composable
fun InformacaoCard(
    titulo: String,
    valor: Float
) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Text(
                text = titulo,
                fontWeight = FontWeight.Medium
            )

            Column(
                horizontalAlignment = Alignment.End
            ) {

                Text(
                    text = "%.2f".format(valor),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "m/s²",
                    fontSize = 12.sp
                )
            }
        }
    }
}

