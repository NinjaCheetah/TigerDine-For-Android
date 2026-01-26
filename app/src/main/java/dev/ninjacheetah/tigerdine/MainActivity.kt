package dev.ninjacheetah.tigerdine

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import dev.ninjacheetah.tigerdine.components.getAllDiningInfo
import dev.ninjacheetah.tigerdine.components.parseLocationInfo
import dev.ninjacheetah.tigerdine.data.types.DiningLocationsParser
import dev.ninjacheetah.tigerdine.ui.theme.TigerDineTheme
import java.util.Date

class MainActivity : ComponentActivity() {
    private fun getDiningData() {
        getAllDiningInfo(this, ::parseDiningData)
    }

    private fun parseDiningData(diningData: DiningLocationsParser?) {
        for (location in diningData!!.locations) {
            println(parseLocationInfo(location, Date()))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TigerDineTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(Modifier.padding(innerPadding)) {
                        Text("TigerDine for Android")
                        Button(onClick = {
                            println("button pressed")
                            getDiningData()
                        }) {
                            Text("Button")
                        }

                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ContentViewPreview() {
    MainActivity()
}
