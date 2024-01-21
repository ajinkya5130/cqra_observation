package com.example.observationapp.dashboard.presentationlayer.ui.fragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.observationapp.R
import com.example.observationapp.dashboard.domainlayer.ObservationViewModel
import com.example.observationapp.databinding.FragmentObservationBinding
import com.example.observationapp.models.Accountable
import com.example.observationapp.models.ObservationSeverity
import com.example.observationapp.models.ObservationType
import com.example.observationapp.models.ProjectModelItem
import com.example.observationapp.models.StageModel
import com.example.observationapp.models.StructureModel
import com.example.observationapp.models.TradeGroupModel
import com.example.observationapp.models.TradeModel
import com.example.observationapp.photo_edit.EditImageActivity
import com.example.observationapp.util.CommonConstant
import com.example.observationapp.util.showShortToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class ObservationFragment : Fragment() {
    private lateinit var binding: FragmentObservationBinding
    private var projectId = ""
    private var structureId = ""
    private var stageOrFloorId = ""
    private var tradeGroupId = ""
    private var tradeId = ""
    private var observationTypeId = ""
    private var observationSeverityId = ""
    private var accountableId = ""

    companion object {
        private const val TAG = "ObservationFragment"
    }

    private val viewModel: ObservationViewModel by viewModels()
    private var projectList = listOf<ProjectModelItem>()
    private var structureList = listOf<StructureModel>()
    private var stageOrFloorList = listOf<StageModel>()
    private var tradeGroupModelList = listOf<TradeGroupModel>()
    private var tradeModelList = listOf<TradeModel>()
    private var observationTypeList = listOf<ObservationType>()
    private var observationSeverityList = listOf<ObservationSeverity>()
    private var accountableList = listOf<Accountable>()
    private var savedPathList = arrayListOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentObservationBinding.inflate(inflater, container, false)
        /*savedPathList.add("/storage/emulated/0/Pictures/1705822595241.png")
        savedPathList.add("/storage/emulated/0/Pictures/1705822617155.png")*/
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        liveDataObservers()
        setProjectAdapterData()
        clickListeners()

    }

    private fun clickListeners() {
        binding.btnCaptureImages.setOnClickListener {
            //requireContext().launchActivity<EditImageActivity>()
            if (savedPathList.size >= 2) {
                requireContext().showShortToast("You already selected 2 images.")
            } else {
                val intent = Intent(requireContext(), EditImageActivity::class.java)
                resultLauncher.launch(intent)
            }
        }
        binding.btnSavedImages.setOnClickListener {
            val bundle = Bundle()
            bundle.putStringArrayList(CommonConstant.IMAGE_PATH1, savedPathList)


            findNavController().navigate(
                R.id.action_observationFragment_to_viewImageFragment,
                bundle
            )

        }
        binding.btnSavedForm.setOnClickListener {
            val description = binding.autoDescriptionName.text.toString()
            val remark = binding.autoRemarkName.text.toString()
            val reference = binding.autoReferenceName.text.toString()


            if (viewModel.isValueEmpty(projectId)) {
                requireContext().showShortToast(getString(R.string.s_is_empty, "project name"))
                return@setOnClickListener
            }
            if (viewModel.isValueEmpty(structureId)) {
                requireContext().showShortToast(getString(R.string.s_is_empty, "structure name"))
                return@setOnClickListener
            }
            if (viewModel.isValueEmpty(stageOrFloorId)) {
                requireContext().showShortToast(getString(R.string.s_is_empty, "stage/floor name"))
                return@setOnClickListener
            }
            if (viewModel.isValueEmpty(tradeGroupId)) {
                requireContext().showShortToast(getString(R.string.s_is_empty, "trade group name"))
                return@setOnClickListener
            }
            if (viewModel.isValueEmpty(tradeId)) {
                requireContext().showShortToast(getString(R.string.s_is_empty, "trade name"))
                return@setOnClickListener
            }
            if (viewModel.isValueEmpty(observationTypeId)) {
                requireContext().showShortToast(getString(R.string.s_is_empty, "observation type"))
                return@setOnClickListener
            }
            if (viewModel.isValueEmpty(description)) {
                requireContext().showShortToast(getString(R.string.s_is_empty, "description"))
                return@setOnClickListener
            }
            if (viewModel.isValueEmpty(remark)) {
                requireContext().showShortToast(getString(R.string.s_is_empty, "remark"))
                return@setOnClickListener
            }
            if (viewModel.isValueEmpty(reference)) {
                requireContext().showShortToast(getString(R.string.s_is_empty, "reference"))
                return@setOnClickListener
            }
            if (viewModel.isValueEmpty(observationSeverityId)) {
                requireContext().showShortToast(
                    getString(
                        R.string.s_is_empty,
                        "observation severity"
                    )
                )
                return@setOnClickListener
            }
            if (viewModel.isValueEmpty(accountableId)) {
                requireContext().showShortToast(getString(R.string.s_is_empty, "accountable"))
                return@setOnClickListener
            }
            if (savedPathList.size == 0) {
                requireContext().showShortToast("No images are selected")
                return@setOnClickListener
            }
            viewModel.saveForm(
                projectId,
                structureId,
                stageOrFloorId,
                tradeGroupId,
                tradeId,
                observationTypeId,
                description,
                remark,
                reference,
                observationSeverityId,
                accountableId,
                savedPathList
            )


        }

    }

    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // There are no request codes
                val data: Intent? = result.data
                val resultString: String = data?.getStringExtra("result") ?: ""
                savedPathList.add(resultString)
                Log.e(TAG, "result: $resultString")
            }
        }


    private fun liveDataObservers() {

        viewModel.getProjectList().observe(viewLifecycleOwner) {
            it?.let { projectModelItems ->
                projectList = projectModelItems
                val adapterProject = ArrayAdapter(
                    requireContext(),
                    androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,
                    projectList
                )
                binding.autoCompleteProjectName.setAdapter(adapterProject)
                viewModel.getUserId()
            }
        }


        lifecycleScope.launch {
            viewModel.structureList.observe(viewLifecycleOwner) {
                it?.let {
                    structureList = it
                    val structureModelArrayAdapter = ArrayAdapter(
                        requireContext(),
                        androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,
                        structureList
                    )
                    binding.autoStructureName.setAdapter(structureModelArrayAdapter)
                }
            }
        }

        lifecycleScope.launch {
            viewModel.stageOrFloorList.observe(viewLifecycleOwner) {
                it?.let {
                    stageOrFloorList = it
                    val stageModelArrayAdapter = ArrayAdapter(
                        requireContext(),
                        androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,
                        stageOrFloorList
                    )
                    binding.autoStageOrFloorName.setAdapter(stageModelArrayAdapter)
                }
            }
        }

        lifecycleScope.launch {
            viewModel.getTradeGroupList().observe(viewLifecycleOwner) {
                it?.let {
                    tradeGroupModelList = it
                    val tradeGroupArrayAdapter = ArrayAdapter(
                        requireContext(),
                        androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,
                        tradeGroupModelList
                    )
                    binding.autoTradeGroupName.setAdapter(tradeGroupArrayAdapter)
                }
            }
        }

        lifecycleScope.launch {
            viewModel.tradeModelList.observe(viewLifecycleOwner) {
                it?.let {
                    tradeModelList = it
                    val tradeModelAdapter = ArrayAdapter(
                        requireContext(),
                        androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,
                        tradeModelList
                    )
                    binding.autoActivityName.setAdapter(tradeModelAdapter)
                }
            }
        }

        lifecycleScope.launch {
            viewModel.observationHistoryModel.observe(viewLifecycleOwner) {
                it?.let {
                    findNavController().popBackStack()
                }
            }
        }

        lifecycleScope.launch {
            viewModel.getObservationTypeList().observe(viewLifecycleOwner) {
                it?.let {
                    observationTypeList = it
                    val tradeGroupArrayAdapter = ArrayAdapter(
                        requireContext(),
                        androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,
                        observationTypeList
                    )
                    binding.autoObservationTypeName.setAdapter(tradeGroupArrayAdapter)
                }
            }
        }

        lifecycleScope.launch {
            viewModel.getObservationSeverityList().observe(viewLifecycleOwner) {
                it?.let {
                    observationSeverityList = it
                    val tradeGroupArrayAdapter = ArrayAdapter(
                        requireContext(),
                        androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,
                        observationSeverityList
                    )
                    binding.autoObsSeverityName.setAdapter(tradeGroupArrayAdapter)
                }
            }
        }

        lifecycleScope.launch {
            viewModel.getAccountableList().observe(viewLifecycleOwner) {
                it?.let {
                    accountableList = it
                    val tradeGroupArrayAdapter = ArrayAdapter(
                        requireContext(),
                        androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,
                        accountableList
                    )
                    binding.autoAccountableName.setAdapter(tradeGroupArrayAdapter)
                }
            }
        }


    }

    private fun setProjectAdapterData() {

        binding.autoCompleteProjectName.setOnItemClickListener { _, _, position, _ ->
            projectId = projectList[position].project_id
            viewModel.getStructureList(projectId)
            binding.autoStructureName.setText("")
            Log.d(TAG, "setProjectAdapterData: $projectId")
        }

        binding.autoStructureName.setOnItemClickListener { _, _, position, _ ->
            structureId = structureList[position].structure_id
            binding.autoStageOrFloorName.setText("")
            viewModel.getStageOrFloorList(structureId)
            Log.d(TAG, "autoStructureName: $structureId")
        }

        binding.autoStageOrFloorName.setOnItemClickListener { _, _, position, _ ->
            stageOrFloorId = stageOrFloorList[position].stage_id
            binding.autoTradeGroupName.setText("")
            Log.d(TAG, "autoStageOrFloorName: $stageOrFloorId")
        }

        binding.autoTradeGroupName.setOnItemClickListener { _, _, position, _ ->
            tradeGroupId = tradeGroupModelList[position].tradegroup_id
            binding.autoActivityName.setText("")
            viewModel.getTradeModelList(tradeGroupId)
            Log.d(TAG, "autoTradeGroupName: $tradeGroupId")
        }

        binding.autoActivityName.setOnItemClickListener { _, _, position, _ ->
            tradeId = tradeModelList[position].trade_id
            Log.d(TAG, "autoActivityTradeName: $tradeId")
        }

        binding.autoObservationTypeName.setOnItemClickListener { _, _, position, _ ->
            observationTypeId = observationTypeList[position].type_id
            Log.d(TAG, "autoObservationTypeName: $observationTypeId")
        }

        binding.autoObsSeverityName.setOnItemClickListener { _, _, position, _ ->
            observationSeverityId = observationSeverityList[position].severity_id
            Log.d(TAG, "autoObsSeverityName: $observationSeverityId")
        }

        binding.autoAccountableName.setOnItemClickListener { _, _, position, _ ->
            accountableId = accountableList[position].user_id
            Log.d(TAG, "autoAccountableName: $accountableId")
        }

    }


}