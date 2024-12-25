import dev.akash42.snapshotcodegenerator.Greeting
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import dev.akash42.snapshotannotation.Snapshot
import dev.akash42.snapshotcodegenerator.ui.theme.SnapshotCodeGeneratorTheme

@Preview()
@Composable
fun GreetingPreview() {
  SnapshotCodeGeneratorTheme {
        Greeting("Android")
    }
}



@Preview()
@Composable
fun GreetingPreview2() {
  SnapshotCodeGeneratorTheme {
        Greeting("Android")
    }
}

