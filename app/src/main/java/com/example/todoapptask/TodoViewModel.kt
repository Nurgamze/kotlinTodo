import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todoapptask.ToDo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException

class TodoViewModel : ViewModel() {

    var todoList = mutableStateOf<List<ToDo>>(emptyList())
        private set

    var completedTaskCount = mutableStateOf(0)
        private set

    private var connection: Connection? = null

    init {
        viewModelScope.launch {
            connectToDatabase()
            fetchTasks()
        }
    }

    private suspend fun connectToDatabase() {
        withContext(Dispatchers.IO) {
            try {
                Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver")
                val url = "jdbc:sqlserver://192.168.1.109:1433;databaseName=MobilDB;encrypt=false;trustServerCertificate=true;integratedSecurity=true;"
                val user = "Gamzenur"
                val password = "Nurgamze1"
                println("Connecting to database...")
                connection = DriverManager.getConnection(url, user, password)
                println("Database connection established successfully.")
            } catch (ex: SQLException) {
                println("SQL Exception during connection: ${ex.message}")
                ex.printStackTrace()
            }
        }
    }

    // Coroutine to fetch tasks from the database
    private suspend fun fetchTasks() {
        withContext(Dispatchers.IO) {
            connection?.let { conn ->
                var statement: java.sql.Statement? = null
                try {
                    statement = conn.createStatement()
                    val resultSet = statement.executeQuery("SELECT * FROM Gorevler")

                    val tasks = mutableListOf<ToDo>()
                    while (resultSet.next()) {
                        tasks.add(
                            ToDo(
                                id = resultSet.getInt("id"),
                                baslik = resultSet.getString("baslik"),
                                aciklama = resultSet.getString("aciklama"),
                                yapildiMi = resultSet.getBoolean("yapildiMi"),
                                notTarihi = resultSet.getDate("notTarihi"),
                            )
                        )
                    }

                    withContext(Dispatchers.Main) {
                        todoList.value = tasks
                        updateTaskCompletion()
                    }
                } catch (ex: SQLException) {
                    println("SQL Exception during query: ${ex.message}")
                    ex.printStackTrace()
                } finally {
                    statement?.close()
                }
            } ?: println("Connection is null, cannot execute query.")
        }
    }

    // Function to add a new task
    fun gorevEkle(title: String, description: String, completed: Boolean) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                connection?.let { conn ->
                    var statement: java.sql.PreparedStatement? = null
                    try {
                        val query = "INSERT INTO Gorevler (baslik, aciklama, yapildiMi) VALUES (?, ?, ?)"
                        statement = conn.prepareStatement(query)
                        statement.setString(1, title)
                        statement.setString(2, description)
                        statement.setBoolean(3, completed)

                        statement.executeUpdate()

                        fetchTasks()
                    } catch (ex: SQLException) {
                        println("SQL Exception during insert: ${ex.message}")
                        ex.printStackTrace()
                    } finally {
                        statement?.close()
                    }
                } ?: println("Connection is null, cannot execute insert.")
            }
        }
    }

    // Function to delete a task by ID
    fun gorevSil(id: Int) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                connection?.let { conn ->
                    var statement: java.sql.PreparedStatement? = null
                    try {
                        val query = "DELETE FROM Gorevler WHERE id = ?"
                        statement = conn.prepareStatement(query)
                        statement.setInt(1, id)

                        statement.executeUpdate()

                        fetchTasks()
                    } catch (ex: SQLException) {
                        println("SQL Exception during delete: ${ex.message}")
                        ex.printStackTrace()
                    } finally {
                        statement?.close()
                    }
                } ?: println("Connection is null, cannot execute delete.")
            }
        }
    }

    // Function to mark a task as completed or not completed
    fun gorevTamamlandi(id: Int, completed: Boolean) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                connection?.let { conn ->
                    var statement: java.sql.PreparedStatement? = null
                    try {
                        val query = "UPDATE Gorevler SET yapildiMi = ? WHERE id = ?"
                        statement = conn.prepareStatement(query)
                        statement.setBoolean(1, completed)
                        statement.setInt(2, id)

                        statement.executeUpdate()

                        fetchTasks()
                    } catch (ex: SQLException) {
                        println("SQL Exception during update: ${ex.message}")
                        ex.printStackTrace()
                    } finally {
                        statement?.close()
                    }
                } ?: println("Connection is null, cannot execute update.")
            }
        }
    }

    private fun updateTaskCompletion() {
        completedTaskCount.value = todoList.value.count { it.yapildiMi }
    }

    override fun onCleared() {
        super.onCleared()
        connection?.close()
    }

}
