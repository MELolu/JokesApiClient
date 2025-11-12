package ph.edu.comteq.jokesapiclient

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface JokesApiService {
    @GET("jokes_api/")
    suspend fun getJokes(): List<Joke>


    @POST("jokes_api/")
    suspend fun addJoke(@Body joke: Joke): Joke


    @DELETE("jokes_api/{id}/")
    suspend fun deleteJoke(@Path("id") id: Int)

    @PUT("jokes_api/{id}")
    suspend fun updateJoke(@Path("id") id: Int, @Body joke: Joke): Joke


}