package com.bavly.chirp.features.chat.di

import com.bavly.chirp.features.chat.data.repository.FirestoreChatRepository
import com.bavly.chirp.features.chat.data.repository.FirestoreMessageRepository
import com.bavly.chirp.features.chat.data.repository.FirestoreParticipantRepository
import com.bavly.chirp.features.chat.domain.repository.ChatRepository
import com.bavly.chirp.features.chat.domain.repository.MessageRepository
import com.bavly.chirp.features.chat.domain.repository.ParticipantRepository
import com.bavly.chirp.features.chat.presentation.chat_detail.ChatDetailViewModel
import com.bavly.chirp.features.chat.presentation.chat_list.ChatListViewModel
import com.bavly.chirp.features.chat.presentation.chat_list_detail.ChatListDetailViewModel
import com.bavly.chirp.features.chat.presentation.create_chat.CreateChatViewModel
import com.bavly.chirp.features.chat.presentation.manage_chat.ManageChatViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val chatModule = module {
    singleOf(::FirestoreParticipantRepository) bind ParticipantRepository::class
    singleOf(::FirestoreChatRepository) bind ChatRepository::class
    singleOf(::FirestoreMessageRepository) bind MessageRepository::class

    viewModelOf(::ChatListViewModel)
    viewModelOf(::ChatListDetailViewModel)
    viewModelOf(::ChatDetailViewModel)
    viewModelOf(::CreateChatViewModel)
    viewModelOf(::ManageChatViewModel)
}