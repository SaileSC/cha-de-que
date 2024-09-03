package com.example.cha00.service

import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface PredictService {
    @Multipart
    @POST("predict")
    fun predict(@Part file: MultipartBody.Part): Call<ResponseBody>
}
