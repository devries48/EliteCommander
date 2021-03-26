@file:Suppress("unused")

package com.devries48.elitecommander.models
import com.google.gson.annotations.SerializedName

data class CodexEntry(
    @SerializedName("Category")
    val category: String,
    @SerializedName("Category_Localised")
    val categoryLocalised: String,
    @SerializedName("EntryID")
    val entryID: Int,
    val event: String,
    @SerializedName("IsNewEntry")
    val isNewEntry: Boolean,
    @SerializedName("Name")
    val name: String,
    @SerializedName("Name_Localised")
    val nameLocalised: String,
    @SerializedName("Region")
    val region: String,
    @SerializedName("Region_Localised")
    val regionLocalised: String,
    @SerializedName("SubCategory")
    val subCategory: String,
    @SerializedName("SubCategory_Localised")
    val subCategoryLocalised: String,
    @SerializedName("System")
    val system: String,
    @SerializedName("SystemAddress")
    val systemAddress: Long,
    val timestamp: String
)