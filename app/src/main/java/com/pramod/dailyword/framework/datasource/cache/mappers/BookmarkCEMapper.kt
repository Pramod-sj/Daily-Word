package com.pramod.dailyword.framework.datasource.cache.mappers

import com.pramod.dailyword.business.domain.model.Bookmark
import com.pramod.dailyword.business.domain.util.EntityMapper
import com.pramod.dailyword.framework.datasource.cache.model.BookmarkCE
import javax.inject.Inject

class BookmarkCEMapper @Inject constructor() : EntityMapper<BookmarkCE, Bookmark> {
    override fun fromEntity(entity: BookmarkCE): Bookmark {
        return Bookmark(
            entity.bookmarkId,
            entity.bookmarkedWord,
            entity.bookmarkedAt,
            entity.bookmarkSeenAt
        )
    }

    override fun toEntity(domain: Bookmark): BookmarkCE {
        return BookmarkCE(
            domain.bookmarkId?:-1,
            domain.bookmarkedWord,
            domain.bookmarkedAt,
            domain.bookmarkSeenAt
        )
    }
}