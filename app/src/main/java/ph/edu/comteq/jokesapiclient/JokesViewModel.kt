package ph.edu.comteq.jokesapiclient


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class JokesUiState{
    object Idle : JokesUiState()  // initial state
    object Loading : JokesUiState()   // Fetching data
    data class Success(val jokes: List<Joke>) : JokesUiState()   //Data fetched successfully
    data class Error(val message: String) : JokesUiState()  // failed
}


class JokesViewModel: ViewModel() {
    private val api = RetrofitInstance.jokeAPI


    // private: only view model can change
    private val _uiState = MutableStateFlow<JokesUiState>(JokesUiState.Idle)


    // public: can be observed by UI
    val uiState: StateFlow<JokesUiState> = _uiState.asStateFlow()


    // fetch
    fun getJokes(){
        viewModelScope.launch {
            _uiState.value = JokesUiState.Loading
            try {
                val jokes = api.getJokes()
                _uiState.value = JokesUiState.Success(jokes)
            } catch (e: Exception) {
                _uiState.value = JokesUiState.Error(
                    e.message ?: "Unknown error"
                )
            }
        }
    }

    fun addJoke(setup: String, punchline: String){
        viewModelScope.launch {
            try {
                val newJoke = Joke(id = null, setup = setup, punchline = punchline )
                api.addJoke(newJoke)
                getJokes()
            } catch (e: Exception){
                _uiState.value = JokesUiState.Error(
                    e.message ?: "Unknown error"
                )
            }
        }
    }


fun deleteJoke(id : Int){
    viewModelScope.launch {

        try {
            api.deleteJoke(id)
            getJokes()

        }catch (e: Exception){
            _uiState.value = JokesUiState.Error(
                "Failed to delete joke: ${e.message?: "Unknown error"}"

                )
            }
        }

    }

    fun updateJoke(id: Int, setup: String, punchline: String){
        viewModelScope.launch {
            try {
                val updatedJoke = Joke(id = id, setup = setup, punchline = punchline)
                api.updateJoke(id, updatedJoke)
                getJokes()
            } catch (e: Exception){
                _uiState.value = JokesUiState.Error(
                    "Failed to update joke: ${e.message ?: "Unknown error"}"
                )
            }
        }
    }


}