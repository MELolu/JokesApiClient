package ph.edu.comteq.jokesapiclient

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


object RetrofitInstance {
    // base url
    private const val BASE_URL = "https://programmingwizards.tech/"

    // logging intercepter -helps use see what is happening
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // okhttp client
    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor).build()

    // retrofit builder
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()


    // api service
    val jokeAPI: JokesApiService = retrofit.create(JokesApiService::class.java)

}