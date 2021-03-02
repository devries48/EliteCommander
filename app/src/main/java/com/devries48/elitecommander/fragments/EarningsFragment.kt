package com.devries48.elitecommander.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.navGraphViewModels
import app.futured.donut.DonutSection
import com.devries48.elitecommander.R
import com.devries48.elitecommander.databinding.FragmentEarningsBinding
import com.devries48.elitecommander.models.EarningModel

class EarningsFragment : Fragment() {

    private val mViewModel: CommanderViewModel by navGraphViewModels(R.id.nav_graph)
    private var _binding: FragmentEarningsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEarningsBinding.inflate(inflater, container, false)

        binding.viewModel = mViewModel
        binding.lifecycleOwner = this

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val list = mViewModel.getEarningStatistics()
        val ctx= requireActivity().applicationContext!!

        list.observe(viewLifecycleOwner,
            {
                run {
                    val sections = ArrayList<DonutSection>()

                    list.value?.forEach {
                        when (it.t) {
                            EarningModel.EarningType.COMBAT -> sections.add(
                                DonutSection(
                                    "Combat", ContextCompat.getColor(ctx, R.color.elite_combat), it.percentage
                                )
                            )
                            EarningModel.EarningType.EXPLORATION -> sections.add(
                                DonutSection(
                                    "Exploration", ContextCompat.getColor(ctx, R.color.elite_exploration), it.percentage
                                )
                            )
                            EarningModel.EarningType.MINING -> sections.add(
                                DonutSection(
                                    "Mining", ContextCompat.getColor(ctx, R.color.elite_mining),
                                    it.percentage
                                )
                            )
                            else ->
                                sections.add(
                                    DonutSection(
                                        "Trading", ContextCompat.getColor(ctx, R.color.elite_trading),
                                        it.percentage
                                    )
                                )
                        }
                    }
                    binding.donutView.cap = 100f
                    binding.donutView.submitData(sections)
                }
            })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}