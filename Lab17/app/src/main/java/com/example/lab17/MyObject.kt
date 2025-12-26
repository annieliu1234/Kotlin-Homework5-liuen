package com.example.lab17

import com.google.gson.annotations.SerializedName

data class MyObject(
    val result: Result
) {
    data class Result(
        val records: List<Record>
    ) {
        data class Record(
            // 將 JSON 的 "SiteName" 對應到 Kotlin 的 "siteName"
            @SerializedName("SiteName")
            val siteName: String,

            @SerializedName("Status")
            val status: String
        )
    }
}