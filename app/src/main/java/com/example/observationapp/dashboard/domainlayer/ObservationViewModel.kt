package com.example.observationapp.dashboard.domainlayer

import android.text.TextUtils
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.observationapp.di.DataStoreRepoInterface
import com.example.observationapp.models.Accountable
import com.example.observationapp.models.AllocatedToModel
import com.example.observationapp.models.ObservationCategory
import com.example.observationapp.models.ObservationHistory
import com.example.observationapp.models.ObservationSeverity
import com.example.observationapp.models.ObservationType
import com.example.observationapp.models.ProjectModelItem
import com.example.observationapp.models.StageModel
import com.example.observationapp.models.StatusModel
import com.example.observationapp.models.StructureModel
import com.example.observationapp.models.TradeGroupModel
import com.example.observationapp.models.TradeModel
import com.example.observationapp.repository.database.ObservationHistoryDBRepository
import com.example.observationapp.repository.database.ObservationListDBRepository
import com.example.observationapp.repository.database.ProjectDBRepository
import com.example.observationapp.util.CommonConstant
import com.example.observationapp.util.Utility.getTodayDateAndTime
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltViewModel
class ObservationViewModel @Inject constructor() : ViewModel() {
    @Inject
    lateinit var projectDBRepository: ProjectDBRepository

    @Inject
    lateinit var observationDBRepo: ObservationListDBRepository

    @Inject
    lateinit var observationHistoryRepo: ObservationHistoryDBRepository

    @Inject
    lateinit var dataStoreRepoInterface: DataStoreRepoInterface

    private var userId = ""

    private var _structureList = MutableLiveData<List<StructureModel>>()
    var structureList: LiveData<List<StructureModel>> = _structureList

    private var _stageOrFloorList = MutableLiveData<List<StageModel>>()
    var stageOrFloorList: LiveData<List<StageModel>> = _stageOrFloorList

    private var _tradeModelList = MutableLiveData<List<TradeModel>>()
    var tradeModelList: LiveData<List<TradeModel>> = _tradeModelList

    private var _observationHistoryModel = MutableLiveData<Long>()
    val observationHistoryModel: LiveData<Long> = _observationHistoryModel

    var projectId = ""
    var structureId = ""
    var stageOrFloorId = ""
    var tradeGroupId = ""
    var tradeId = ""
    var observationTypeId = ""
    var observationCategoryId = ""
    var observationSeverityId = ""
    var accountableId = ""
    var closeById = ""
    var statusId = ""

    fun getProjectList(): LiveData<List<ProjectModelItem>> {
        return projectDBRepository.projectList
    }

    fun getStructureList(projectId: String) {
        if (projectId.isNotEmpty()) {
            viewModelScope.launch {
                _structureList.value = projectDBRepository.getStructureList(projectId)
            }
        }
    }

    fun getStageOrFloorList(structureId: String) {
        if (structureId.isNotEmpty()) {
            viewModelScope.launch {
                _stageOrFloorList.value = projectDBRepository.getStageOrFloorList(structureId)
            }
        }
    }

    fun getTradeGroupList(): LiveData<List<TradeGroupModel>> = observationDBRepo.getTradeGroupList()

    fun getObservationTypeList(): LiveData<List<ObservationType>> =
        observationDBRepo.getObservationTypeList()

    fun getObservationCategoryList(): LiveData<List<ObservationCategory>> =
        observationDBRepo.getObservationCategoryList()

    fun getObservationSeverityList(): LiveData<List<ObservationSeverity>> =
        observationDBRepo.getObservationSeverityList()

    fun getAccountableList(): LiveData<List<Accountable>> = observationDBRepo.getAccountableList()

    fun getAllocatedToList(): LiveData<List<AllocatedToModel>> =
        observationDBRepo.getAllocatedToList()

    fun getAllStatusList(): LiveData<List<StatusModel>> = observationDBRepo.getAllStatusList()

    fun getTradeModelList(tradeGroupId: String) {
        if (tradeGroupId.isNotEmpty()) {
            viewModelScope.launch {
                _tradeModelList.value = observationDBRepo.getTradeModelList(tradeGroupId)
            }
        }
    }

    fun getUserId() {
        runBlocking {
            userId = dataStoreRepoInterface.getString(CommonConstant.USER_ID) ?: ""
        }

    }

    fun isValueEmpty(str: String): Boolean {
        return TextUtils.isEmpty(str)
    }

    fun saveForm(
        location: String,
        description: String,
        remark: String,
        reference: String,
        targetDate: String,
        savedPathList: ArrayList<String>,
        savedFileNameList: ArrayList<String>,
    ) {
        val model = ObservationHistory()
        model.project_id = projectId
        model.structure_id = structureId
        model.floors = stageOrFloorId
        model.tradegroup_id = tradeGroupId
        model.activityOrTradeId = tradeId
        model.observation_type = observationTypeId
        model.description = description
        model.remark = remark
        model.reference = reference
        model.observation_severity = observationSeverityId
        model.site_representative = accountableId
        model.created_by = userId
        model.observation_date = getTodayDateAndTime()
        model.location = location
        model.target_date = targetDate
        model.status = statusId
        model.closed_by = closeById
        model.observation_image = savedPathList
        model.tempObservationId = System.currentTimeMillis().toString()

        Log.d(TAG, "saveForm: model: $model")
        val json = JsonObject()
        json.addProperty("project_id", projectId)
        json.addProperty("structure_id", structureId)
        json.addProperty("floors", stageOrFloorId)
        json.addProperty("tradegroup_id", tradeGroupId)
        json.addProperty("activity_id", tradeId)
        json.addProperty("temp_observation_number", model.tempObservationId)
        json.addProperty("observation_category", observationCategoryId)
        json.addProperty("observation_type", observationTypeId)
        json.addProperty("location", location)
        json.addProperty("description", description)
        json.addProperty("remark", remark)
        json.addProperty("reference", reference)
        json.addProperty("observation_severity", observationSeverityId)
        json.addProperty("site_representative", accountableId)
        json.addProperty("status", statusId)
        json.addProperty("closed_by", closeById)
        json.addProperty("observation_date", getTodayDateAndTime())
        json.addProperty("target_date", targetDate)
        json.add("observation_image", customisedImageList(savedFileNameList))
        Log.d(TAG, "saveForm: $json")

        saveObservationHistory(model)
    }

    private fun customisedImageList(savedPathList: ArrayList<String>): JsonArray {
        val jsonArray = JsonArray()
        savedPathList.forEachIndexed { index, path ->
            val getFileName =
                "${projectId}_${structureId}_${tradeId}_${path}_${index + 1}${CommonConstant.FILE_EXTENSIONS}"
            jsonArray.add(getFileName)
        }

        return jsonArray
    }

    private fun saveObservationHistory(model: ObservationHistory) {
        viewModelScope.launch {
            _observationHistoryModel.value = observationHistoryRepo.insertObservationHistory(model)
        }
    }

    companion object {
        private const val TAG = "ObservationViewModel"
    }
}