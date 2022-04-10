/*
 * Copyright (c) 2020 Hemanth Savarla.
 *
 * Licensed under the GNU General Public License v3
 *
 * This is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 */
package io.github.me0wzz.music.fragments.about

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.core.app.ShareCompat
import androidx.core.net.toUri
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.utils.MDUtil.updatePadding
import io.github.me0wzz.music.Constants
import io.github.me0wzz.music.R
import io.github.me0wzz.music.adapter.ContributorAdapter
import io.github.me0wzz.music.databinding.FragmentAboutBinding
import io.github.me0wzz.music.fragments.LibraryViewModel
import io.github.me0wzz.music.util.NavigationUtil
import io.github.me0wzz.music.util.RetroUtil
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class AboutFragment : Fragment(R.layout.fragment_about), View.OnClickListener {
    private var _binding: FragmentAboutBinding? = null
    private val binding get() = _binding!!
    private val libraryViewModel by sharedViewModel<LibraryViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAboutBinding.bind(view)
        binding.aboutContent.cardOther.version.setSummary(getAppVersion())
        setUpView()
        loadContributors()
        // This is a workaround as CollapsingToolbarLayout consumes insets and
        // insets are not passed to child views
        // https://github.com/material-components/material-components-android/issues/1310
        if (!RetroUtil.isLandscape()) {
            binding.aboutContent.root.updatePadding(bottom = RetroUtil.getNavigationBarHeight())
        }
    }

    private fun openUrl(url: String) {
        val i = Intent(Intent.ACTION_VIEW)
        i.data = url.toUri()
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(i)
    }

    private fun setUpView() {
        binding.aboutContent.cardRetroInfo.appGithub.setOnClickListener(this)
        binding.aboutContent.cardRetroInfo.faqLink.setOnClickListener(this)
        binding.aboutContent.cardRetroInfo.appTranslation.setOnClickListener(this)
        binding.aboutContent.cardRetroInfo.appShare.setOnClickListener(this)
        binding.aboutContent.cardOther.changelog.setOnClickListener(this)
        binding.aboutContent.cardOther.openSource.setOnClickListener(this)
        binding.aboutContent.cardRetroInfo.bugReportLink.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.faqLink -> openUrl(Constants.FAQ_LINK)
            R.id.appGithub -> openUrl(Constants.GITHUB_PROJECT)
            R.id.appTranslation -> openUrl(Constants.TRANSLATE)
            R.id.appShare -> shareApp()
            R.id.changelog -> NavigationUtil.gotoWhatNews(requireActivity())
            R.id.openSource -> NavigationUtil.goToOpenSource(requireActivity())
            R.id.bugReportLink -> NavigationUtil.bugReport(requireActivity())
        }
    }

    private fun getAppVersion(): String {
        return try {
            val isPro = "Libre"
            val packageInfo =
                requireActivity().packageManager.getPackageInfo(requireActivity().packageName, 0)
            "${packageInfo.versionName} $isPro"
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            "0.0.0"
        }
    }

    private fun shareApp() {
        ShareCompat.IntentBuilder(requireActivity()).setType("text/plain")
            .setChooserTitle(R.string.share_app)
            .setText("https://github.com/Me0wzz/Letro/releases/latest")
            .startChooser()
    }

    private fun loadContributors() {
        val contributorAdapter = ContributorAdapter(emptyList())
        binding.aboutContent.cardCredit.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            itemAnimator = DefaultItemAnimator()
            adapter = contributorAdapter
        }
        libraryViewModel.fetchContributors().observe(viewLifecycleOwner) { contributors ->
            contributorAdapter.swapData(contributors)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
