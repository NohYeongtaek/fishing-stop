package com.example.fishingstop.feature.education.presentation

import androidx.lifecycle.ViewModel
import com.example.fishingstop.feature.education.domain.EducationCategory
import com.example.fishingstop.feature.education.domain.EducationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/** 예방 교육 목록 ViewModel. 콘텐츠가 정적이므로 목록만 노출한다. */
@HiltViewModel
class EducationViewModel @Inject constructor(
    repository: EducationRepository
) : ViewModel() {
    val categories: List<EducationCategory> = repository.getCategories()
}
