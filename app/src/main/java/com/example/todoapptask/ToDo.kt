// Import necessary packages
package com.example.todoapptask

import java.sql.Date

data class ToDo(
    var id: Int,
    var baslik: String,
    var aciklama: String,
    var yapildiMi: Boolean,
    var notTarihi: Date
)

