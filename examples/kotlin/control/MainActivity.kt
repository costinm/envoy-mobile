package io.envoyproxy.envoymobile.hellocontrolkotlin

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log

import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.TimeUnit

private const val REQUEST_HANDLER_THREAD_NAME = "hello_envoy_kt"
private const val ENDPOINT = "http://s3.amazonaws.com/api.lyft.com/static/demo/hello_world.txt"

class MainActivity : Activity() {
  private lateinit var recyclerView: RecyclerView
  private val thread = HandlerThread(REQUEST_HANDLER_THREAD_NAME)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    recyclerView = findViewById(R.id.recycler_view) as RecyclerView
    recyclerView.layoutManager = LinearLayoutManager(this)

    val adapter = ResponseRecyclerViewAdapter()
    recyclerView.adapter = adapter
    val dividerItemDecoration = DividerItemDecoration(recyclerView.context, DividerItemDecoration.VERTICAL)
    recyclerView.addItemDecoration(dividerItemDecoration)
    thread.start()
    val handler = Handler(thread.looper)

    // Run a request loop until the application exits.
    handler.postDelayed(object : Runnable {
      override fun run() {
        try {
          val response = makeRequest()
          recyclerView.post({ adapter.add(response) })
        } catch (e: IOException) {
          Log.d("MainActivity", "exception making request.", e)
        }

        // Make a call again
        handler.postDelayed(this, TimeUnit.SECONDS.toMillis(1))
      }
    }, TimeUnit.SECONDS.toMillis(1))
  }

  override fun onDestroy() {
    super.onDestroy()
    thread.quit()
  }

  private fun makeRequest(): Response {
    val url = URL(ENDPOINT)
    // Open connection to the envoy thread listening locally on port 9001
    val connection = url.openConnection() as HttpURLConnection
    val status = connection.responseCode
    if (status != 200) {
      throw IOException("non 200 status: $status")
    }

    val inputStream = connection.inputStream
    val body = deserialize(inputStream)
    inputStream.close()
    return Response(body, "")
  }

  private fun deserialize(inputStream: InputStream): String {
    return inputStream.bufferedReader().use { reader -> reader.readText() }
  }
}
