package com.rocketdan24.fishingstop.feature.education.domain

/** 예방 교육 콘텐츠 저장소. 현재는 앱 내장 정적 콘텐츠를 제공한다. */
interface EducationRepository {
    fun getCategories(): List<EducationCategory>
    fun getCategory(id: String): EducationCategory?
}
