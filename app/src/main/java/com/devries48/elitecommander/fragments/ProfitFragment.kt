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
import com.devries48.elitecommander.databinding.FragmentProfitBinding
import com.devries48.elitecommander.models.ProfitModel

class ProfitFragment : Fragment() {

    private val mViewModel: CommanderViewModel by navGraphViewModels(R.id.nav_graph)
    private var _binding: FragmentProfitBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfitBinding.inflate(inflater, container, false)

        binding.viewModel = mViewModel
        binding.lifecycleOwner = this

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val list = mViewModel.getProfitChart()
        val ctx = requireActivity().applicationContext!!

        list.observe(viewLifecycleOwner,
            {
                run {
                    val sections = ArrayList<DonutSection>()

                    list.value?.forEach {
                        when (it.t) {
                            ProfitModel.ProfitType.COMBAT -> sections.add(
                                DonutSection(
                                    "Combat", ContextCompat.getColor(ctx, R.color.elite_combat), it.percentage
                                )
                            )
                            ProfitModel.ProfitType.EXPLORATION -> sections.add(
                                DonutSection(
                                    "Exploration", ContextCompat.getColor(ctx, R.color.elite_exploration), it.percentage
                                )
                            )
                            ProfitModel.ProfitType.TRADING -> sections.add(
                                DonutSection(
                                    "Trading", ContextCompat.getColor(ctx, R.color.elite_trading),
                                    it.percentage
                                )
                            )
                            ProfitModel.ProfitType.MINING -> sections.add(
                                DonutSection(
                                    "Mining", ContextCompat.getColor(ctx, R.color.elite_mining),
                                    it.percentage
                                )
                            )
                            ProfitModel.ProfitType.SMUGGLING -> sections.add(
                                DonutSection(
                                    "Smuggling", ContextCompat.getColor(ctx, R.color.elite_yellow),
                                    it.percentage
                                )
                            )
                            ProfitModel.ProfitType.SEARCH_RESCUE -> sections.add(
                                DonutSection(
                                    "Search & Rescue", ContextCompat.getColor(ctx, R.color.elite_orange),
                                    it.percentage
                                )
                            )

                            else ->
                                sections.add(
                                    DonutSection(
                                        "Other", ContextCompat.getColor(ctx, R.color.elite_light_orange),
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