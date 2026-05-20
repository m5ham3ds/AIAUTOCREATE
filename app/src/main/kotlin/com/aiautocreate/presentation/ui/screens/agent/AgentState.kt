package com.aiautocreate.presentation.ui.screens.agent

import com.aiautocreate.agent.AgentStats
import java.util.UUID

data class AgentState(
    val selectedTab: Int = 0, // 0=Chat, 1=Interventions, 2=Permissions, 3=Stats

    // Chat
    val chatMessages: List<ChatMessage> = emptyList(),
    val inputText: String = "",
    val isChatLoading: Boolean = false,
    val chatError: String? = null,

    // Interventions
    val interventions: List<AgentIntervention> = emptyList(),

    // Permissions
    val permissions: AgentPermissions = AgentPermissions(),

    // Stats
    val stats: AgentStats? = null,
    val isRefreshingStats: Boolean = false,

    val isLoading: Boolean = true
)

// باقي النماذج كما هي (ChatMessage, AgentIntervention, AgentPermissions)

data class ChatMessage(
    val id: String,
    val text: String,
    val isUser: Boolean
)

data class AgentIntervention(
    val id: String,
    val type: String,
    val title: String,
    val description: String,
    val status: String,
    val timestamp: Long
)

data class AgentPermissions(
    val autoFixErrors: Boolean = false,
    val autoRetry: Boolean = false,
    val optimizeResources: Boolean = false,
    val accessProjects: Boolean = false,
    val maxInterventionDepth: Int = 3
)
