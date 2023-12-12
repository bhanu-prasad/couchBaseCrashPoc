package com.appcooking.couchct_poc

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.appcooking.couchct_poc.ui.theme.CouchCT_pocTheme
import com.couchbase.lite.DataSource
import com.couchbase.lite.Database
import com.couchbase.lite.MutableDictionary
import com.couchbase.lite.MutableDocument
import com.couchbase.lite.QueryBuilder
import com.couchbase.lite.SelectResult
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.util.UUID


class MainActivity : ComponentActivity() {
    private lateinit var database: Database

    private fun showToast(message: String){
        Toast.makeText(
            applicationContext, message,
            Toast.LENGTH_LONG
        ).show()
    }
    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val couchbaseManager = CouchbaseManager(applicationContext)
//        couchbaseManager.clearDB()
        val insertHandle: (text: String) -> Unit = { it ->
            try {
                it.toInt().let { numRecords ->
                    GlobalScope.launch(Dispatchers.IO){
                        for (i in 0 until numRecords) {
                            val document = MutableDictionary()
                                .setString("name", "dummy${i}")
                                .setString("email", "dummy${i}_email")
                                .setString("address", "dummy${i}_addressFull")
                                .setString("id", UUID.randomUUID().toString())

                            document.apply {
                                val uniqueId = if (contains("id")) getString("id") else null

                                couchbaseManager.getCollection()
                                    ?.save(MutableDocument(uniqueId, toMap()))
                            }
                        }
                        MainScope().launch{ showToast("Data insertion complete.")}
                    }
                }
            } catch (e: Exception) {
                throw e
            }
        }
        val deleteHandle: () -> Unit = {
            try {
                GlobalScope.launch(Dispatchers.IO) {
                    MainScope().launch{ showToast("Start removing documents.") }
                    val collection = couchbaseManager.getCollection()
                    val docIds = mutableListOf<String>()
                    collection?.apply {
                        val queryRes = QueryBuilder.select(
                            SelectResult.property("id")
                        ).from(DataSource.collection(this)).execute().allResults()
                        for (result in queryRes) {
                            val id = result.getString("id")
                            id?.let { docIds.add(it) }
                        }
                    }
                    couchbaseManager.removeDocs(docIds)
                }
            } catch (e: Exception) {
                throw e
            }
        }
        setContent {
            CouchCT_pocTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Greeting("Android", handleInsert = insertHandle, handleDelete = deleteHandle)
                }
            }
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Greeting(name: String, handleInsert: (text: String) -> Unit, handleDelete: () -> Unit) {
    var text by remember { mutableStateOf("") }

    Column {

        TextField(
            value = text,
            onValueChange = { text = it },
            label = { Text("Number of items to add") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp, 50.dp)
        )

        Button(onClick = { handleInsert(text) }, Modifier.fillMaxWidth()) {
            Text(text = "Add Items")
        }
        Button(onClick = handleDelete, Modifier.fillMaxWidth()) {
            Text(text = "Delete All Items")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    CouchCT_pocTheme {
//        Greeting("Android")
    }
}