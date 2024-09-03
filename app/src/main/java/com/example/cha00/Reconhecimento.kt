package com.example.cha00

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.cha00.service.PredictService
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.ByteArrayOutputStream

class Reconhecimento : AppCompatActivity() {

    private lateinit var predictService: PredictService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_reconhecimento)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Configura o Retrofit
        val retrofit = Retrofit.Builder()
            .baseUrl("http://34.95.160.50/")
            .client(OkHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        predictService = retrofit.create(PredictService::class.java)

        val imagem = intent.getParcelableExtra<Bitmap>("imagem")
        val imageView: ImageView = findViewById(R.id.image)
        imageView.setImageBitmap(imagem)

        imagem?.let {
            enviarImagem(it)
        }
    }

    private fun enviarImagem(bitmap: Bitmap) {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, stream)
        val byteArray = stream.toByteArray()
        val requestBody = RequestBody.create("image/jpeg".toMediaTypeOrNull(), byteArray)
        val filePart = MultipartBody.Part.createFormData("file", "image.jpg", requestBody)

        // Fazer a requisição
        val call = predictService.predict(filePart)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    // Manipular a resposta
                    val respostaJson = response.body()?.string()
                    Log.d("API Response", "Success: $respostaJson")
                    respostaJson?.let {
                        processarResposta(it)
                    }
                } else {
                    Log.e("API Response", "Error: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("API Response", "Failure: ${t.message}")
            }
        })
    }

    private fun processarResposta(jsonResponse: String) {
        try {
            val jsonObject = JSONObject(jsonResponse)

            val classe = jsonObject.getString("classe")
            val nomeCientifico = jsonObject.getString("nome_cientifico")
            val beneficios = jsonObject.getJSONArray("beneficios")

            val textNome: TextView = findViewById(R.id.text_nome)
            val textNomeCientifico: TextView = findViewById(R.id.text_nome_cientifico)
            val textBeneficios: TextView = findViewById(R.id.text_beneficios)

            val progress: ProgressBar = findViewById(R.id.progress)
            val tableInfo: LinearLayout = findViewById(R.id.info)


            textNome.text = classe
            textNomeCientifico.text = nomeCientifico

            val beneficiosStr = StringBuilder()
            for (i in 0 until beneficios.length()) {
                beneficiosStr.append("- ").append(beneficios.getString(i)).append("\n")
            }

            textBeneficios.text = beneficiosStr.toString()

            progress.visibility = View.GONE
            tableInfo.visibility = View.VISIBLE

        } catch (e: Exception) {
            Log.e("Processar Resposta", "Erro ao processar a resposta JSON", e)
        }
    }
}
