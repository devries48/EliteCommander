package com.devries48.elitecommander.models
import com.google.gson.annotations.SerializedName

data class CodexEntry(
    @SerializedName("Category")
    val category: String, // $Codex_Category_StellarBodies;
    @SerializedName("Category_Localised")
    val categoryLocalised: String, // Astronomical Bodies
    @SerializedName("EntryID")
    val entryID: Int, // 1101001
    val event: String, // com.devries48.elitecommander.models.CodexEntry
    @SerializedName("IsNewEntry")
    val isNewEntry: Boolean, // true
    @SerializedName("Name")
    val name: String, // $Codex_Ent_TTS_Type_Name;
    @SerializedName("Name_Localised")
    val nameLocalised: String, // T Tauri Star
    @SerializedName("Region")
    val region: String, // $Codex_RegionName_2;
    @SerializedName("Region_Localised")
    val regionLocalised: String, // Empyrean Straits
    @SerializedName("SubCategory")
    val subCategory: String, // $Codex_SubCategory_Stars;
    @SerializedName("SubCategory_Localised")
    val subCategoryLocalised: String, // Stars
    @SerializedName("System")
    val system: String, // Dryoi Pri FB-W e2-1168
    @SerializedName("SystemAddress")
    val systemAddress: Long, // 5017828321500
    val timestamp: String // 2021-01-17T18:04:08Z
)