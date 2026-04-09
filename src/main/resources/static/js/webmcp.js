/**
 * ============================================================
 * WebMCP Imperative Tool Registration
 * ============================================================
 *
 * Registers commission AI tools via Chrome 146+ native
 * navigator.modelContext.registerTool() API.
 *
 * Requires: chrome://flags -> "WebMCP" -> Enabled -> Relaunch
 *
 * Execute callbacks return MCP content format:
 *   { content: [{ type: 'text', text: '...' }] }
 *
 * Include on any page: <script src="/js/webmcp.js"></script>
 * Tools are registered once (duplicates are skipped).
 *
 * @see https://webmachinelearning.github.io/webmcp/
 */
(function () {
    'use strict';

    if (!navigator.modelContext) {
        console.warn('[WebMCP] navigator.modelContext not available. Enable in chrome://flags -> WebMCP');
        return;
    }

    const BASE = '/api/ai';

    const TOOLS = [
        {
            name: 'askCommissionQuestion',
            description: 'Ask a natural language question about commissions using RAG (Retrieval-Augmented Generation).',
            inputSchema: {
                type: 'object',
                properties: { question: { type: 'string', description: 'The question to ask' } },
                required: ['question']
            },
            endpoint: `${BASE}/rag/ask`,
            method: 'POST'
        },
        {
            name: 'generatePerformanceReport',
            description: 'Generate an AI-powered performance report for a sales representative.',
            inputSchema: {
                type: 'object',
                properties: { salesRepName: { type: 'string', description: 'Name of the sales rep' } },
                required: ['salesRepName']
            },
            endpoint: `${BASE}/rag/report/{salesRepName}`,
            method: 'GET'
        },
        {
            name: 'explainCommissionCalculation',
            description: 'Use AI to explain how a specific commission calculation was computed.',
            inputSchema: {
                type: 'object',
                properties: { calculationId: { type: 'string', description: 'The calculation ID' } },
                required: ['calculationId']
            },
            endpoint: `${BASE}/explain/calculation/{calculationId}`,
            method: 'GET'
        },
        {
            name: 'explainCommissionPlan',
            description: 'Use AI to explain a commission plan in plain language.',
            inputSchema: {
                type: 'object',
                properties: { planId: { type: 'string', description: 'The plan ID' } },
                required: ['planId']
            },
            endpoint: `${BASE}/explain/plan/{planId}`,
            method: 'GET'
        },
        {
            name: 'analyzeDispute',
            description: 'AI-powered detailed analysis of a commission dispute.',
            inputSchema: {
                type: 'object',
                properties: { disputeId: { type: 'string', description: 'The dispute ID' } },
                required: ['disputeId']
            },
            endpoint: `${BASE}/disputes/analyze/{disputeId}`,
            method: 'GET'
        },
        {
            name: 'triageDispute',
            description: 'AI triage of a commission dispute: categorize severity and recommend action.',
            inputSchema: {
                type: 'object',
                properties: { disputeId: { type: 'string', description: 'The dispute ID' } },
                required: ['disputeId']
            },
            endpoint: `${BASE}/disputes/triage/{disputeId}`,
            method: 'GET'
        },
        {
            name: 'forecastCommissions',
            description: 'Forecast future commission earnings for a sales representative.',
            inputSchema: {
                type: 'object',
                properties: { userId: { type: 'string', description: 'The sales rep user ID' } },
                required: ['userId']
            },
            endpoint: `${BASE}/forecast/user/{userId}`,
            method: 'GET'
        },
        {
            name: 'forecastTeamCommissions',
            description: 'Forecast commission earnings for the entire sales team.',
            inputSchema: { type: 'object', properties: {} },
            endpoint: `${BASE}/forecast/team`,
            method: 'GET'
        },
        {
            name: 'detectCommissionAnomalies',
            description: 'Scan all commission calculations for anomalies using AI.',
            inputSchema: { type: 'object', properties: {} },
            endpoint: `${BASE}/anomaly/detect`,
            method: 'GET'
        },
        {
            name: 'checkCalculationForAnomalies',
            description: 'Check a specific commission calculation for anomalies.',
            inputSchema: {
                type: 'object',
                properties: { calculationId: { type: 'string', description: 'The calculation ID' } },
                required: ['calculationId']
            },
            endpoint: `${BASE}/anomaly/check/{calculationId}`,
            method: 'GET'
        },
        {
            name: 'askReActAgent',
            description: 'Ask the ReAct reasoning agent a question — it thinks step-by-step using tools.',
            inputSchema: {
                type: 'object',
                properties: { question: { type: 'string', description: 'Your question' } },
                required: ['question']
            },
            endpoint: `${BASE}/agent/ask`,
            method: 'POST'
        },
        {
            name: 'executeCommissionReview',
            description: 'Execute a multi-agent AI workflow to review commissions comprehensively.',
            inputSchema: {
                type: 'object',
                properties: { request: { type: 'string', description: 'What to review' } },
                required: ['request']
            },
            endpoint: `${BASE}/workflow/review`,
            method: 'POST'
        },
        {
            name: 'validateInput',
            description: 'Validate input through 4-layer guardrails: null check, length, injection detection, topic boundary.',
            inputSchema: {
                type: 'object',
                properties: { input: { type: 'string', description: 'Text to validate' } },
                required: ['input']
            },
            endpoint: `${BASE}/moderation/validate`,
            method: 'POST'
        },
        {
            name: 'classifyInput',
            description: 'AI-powered classification of whether input is an appropriate commission query.',
            inputSchema: {
                type: 'object',
                properties: { input: { type: 'string', description: 'Text to classify' } },
                required: ['input']
            },
            endpoint: `${BASE}/moderation/classify`,
            method: 'POST'
        },
        {
            name: 'sanitizeOutput',
            description: 'Scan text for sensitive data (SSN, credit cards, emails, API keys) and redact them.',
            inputSchema: {
                type: 'object',
                properties: { text: { type: 'string', description: 'Text to sanitize' } },
                required: ['text']
            },
            endpoint: `${BASE}/moderation/sanitize`,
            method: 'POST'
        }
    ];

    // ============================================================
    // Execute callback — calls REST API, returns MCP content format
    // ============================================================
    function createExecute(def) {
        return async function (args) {
            try {
                let url = def.endpoint;
                const pathParams = url.match(/\{(\w+)\}/g) || [];
                pathParams.forEach(p => {
                    const key = p.replace(/[{}]/g, '');
                    url = url.replace(p, encodeURIComponent(args[key] || ''));
                });

                const opts = { method: def.method };
                if (def.method === 'POST' || def.method === 'PUT') {
                    const body = {};
                    Object.entries(args).forEach(([k, v]) => {
                        if (!pathParams.some(p => p.includes(k)) && v !== '' && v !== undefined) {
                            body[k] = v;
                        }
                    });
                    opts.headers = { 'Content-Type': 'application/json' };
                    opts.body = JSON.stringify(body);
                }

                const res = await fetch(url, opts);
                let text;
                if (!res.ok) {
                    text = `Error HTTP ${res.status}: ${await res.text()}`;
                } else {
                    const ct = res.headers.get('content-type') || '';
                    if (ct.includes('json')) {
                        const data = await res.json();
                        text = typeof data === 'string' ? data : JSON.stringify(data, null, 2);
                    } else {
                        text = await res.text();
                    }
                }
                return { content: [{ type: 'text', text }] };
            } catch (e) {
                return { content: [{ type: 'text', text: `Error: ${e.message}` }] };
            }
        };
    }

    // ============================================================
    // Register all tools imperatively
    // ============================================================
    let registered = 0;
    TOOLS.forEach(def => {
        try {
            navigator.modelContext.registerTool({
                name: def.name,
                description: def.description,
                inputSchema: def.inputSchema,
                execute: createExecute(def)
            });
            registered++;
        } catch (e) {
            // Already registered (e.g. navigated back) — skip silently
            if (!e.message?.includes('already registered')) {
                console.error(`[WebMCP] Failed to register "${def.name}":`, e.message);
            }
        }
    });

    console.log(`[WebMCP] ${registered}/${TOOLS.length} tools registered via imperative API`);

    // Update navbar badge if present
    const badge = document.getElementById('webmcp-tool-count');
    if (badge) {
        badge.textContent = `${registered} WebMCP tools`;
        badge.style.display = registered > 0 ? 'inline-block' : 'none';
    }

})();
