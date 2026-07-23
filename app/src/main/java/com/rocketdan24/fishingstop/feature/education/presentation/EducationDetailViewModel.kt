package com.rocketdan24.fishingstop.feature.education.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.navigation.toRoute
import com.rocketdan24.fishingstop.core.navigation.Routes
import com.rocketdan24.fishingstop.feature.education.domain.EducationCategory
import com.rocketdan24.fishingstop.feature.education.domain.EducationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/** 예방 교육 상세 ViewModel: 라우트의 categoryId 로 콘텐츠를 로드한다. */
@HiltViewModel
class EducationDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    repository: EducationRepository
) : ViewModel() {
    private val args = savedStateHandle.toRoute<Routes.EducationDetail>()
    val category: EducationCategory? = repository.getCategory(args.categoryId)
}
