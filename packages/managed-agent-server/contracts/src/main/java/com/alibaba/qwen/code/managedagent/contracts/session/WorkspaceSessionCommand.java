package com.alibaba.qwen.code.managedagent.contracts.session;

import com.alibaba.qwen.code.managedagent.contracts.extensions.ManagedExtensionPackages.Pin;
import com.alibaba.qwen.code.managedagent.contracts.workspace.WorkspaceSelection;
import com.alibaba.qwen.code.managedagent.repository.SessionWorktreeStore;

import java.util.List;
import java.util.Map;
import java.util.Set;

public record WorkspaceSessionCommand(String tenantId, String actorId,
        String idempotencyKey, String requestDigest, String agentId, String requestedRevision,
        String title, List<Map<String, Object>> input, String payloadDigest, WorkspaceSelection selection,
        String requestedApprovalMode, boolean approvalSelectionSupported, Boolean requestedPlanMode,
        boolean planModeSupported, List<Map<String, Object>> skills, boolean publicChildrenSupported, String modelId,
        List<Pin> extensionPins, Set<String> extensionLifecycleProfiles, SessionWorktreeStore.Plan worktree) {
    public WorkspaceSessionCommand(String tenantId, String actorId,
            String idempotencyKey, String requestDigest, String agentId, String requestedRevision,
            String title, List<Map<String, Object>> input, String payloadDigest, WorkspaceSelection selection) {
        this(tenantId, actorId, idempotencyKey, requestDigest, agentId, requestedRevision,
                title, input, payloadDigest, selection, null, false, null, false, List.of(), false,
                null, null, Set.of(), null);
    }
}
