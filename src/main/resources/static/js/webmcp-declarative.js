/**
 * ============================================================
 * WebMCP Declarative Implementation
 * ============================================================
 *
 * Implements WebMCP spec Section 4.3 — Declarative Tool Registration.
 * Auto-synthesizes MCP tool definitions from HTML elements annotated
 * with data-webmcp-* attributes and registers them via Chrome's
 * native navigator.modelContext.registerTool() API.
 *
 * Requires: Chrome 146+ with WebMCP flag enabled
 *   chrome://flags -> "WebMCP" -> Enabled -> Relaunch
 *
 * Usage: Add attributes to any container element:
 *
 *   <div data-webmcp-tool="askQuestion"
 *        data-webmcp-description="Ask about commissions"
 *        data-webmcp-endpoint="/api/ai/rag/ask"
 *        data-webmcp-method="POST"
 *        data-webmcp-readonly="true">
 *     <input name="question" data-webmcp-required="true">
 *   </div>
 *
 * @see https://webmachinelearning.github.io/webmcp/
 * @see https://github.com/WebMCP-org/examples
 */
(function () {
    'use strict';

    // Guard: Chrome 146+ with WebMCP flag enabled
    if (!navigator.modelContext) {
        console.warn('[WebMCP] navigator.modelContext not available. Enable in chrome://flags -> WebMCP');
        return;
    }

    console.log('[WebMCP Declarative] navigator.modelContext detected');

    // ============================================================
    // Schema Synthesis — derive JSON Schema from HTML form elements
    // ============================================================

    function synthesizeSchema(container) {
        const properties = {};
        const required = [];

        const elements = container.querySelectorAll(
            'input[name], textarea[name], select[name], [data-webmcp-param][id]'
        );

        elements.forEach(el => {
            const name = el.getAttribute('name') || el.id;
            if (!name) return;

            const prop = {};
            const inputType = el.type || el.tagName.toLowerCase();

            switch (inputType) {
                case 'number': case 'range':
                    prop.type = 'number'; break;
                case 'checkbox':
                    prop.type = 'boolean'; break;
                case 'select': case 'select-one':
                    prop.type = 'string';
                    const opts = Array.from(el.options).filter(o => o.value).map(o => o.value);
                    if (opts.length > 0) prop.enum = opts;
                    break;
                default:
                    prop.type = 'string';
            }

            prop.description = el.getAttribute('data-webmcp-param-description')
                || el.getAttribute('placeholder')
                || findLabelText(el, container)
                || name;

            properties[name] = prop;

            if (el.hasAttribute('required') || el.getAttribute('data-webmcp-required') === 'true') {
                required.push(name);
            }
        });

        const schema = { type: 'object', properties };
        if (required.length > 0) schema.required = required;
        return schema;
    }

    function findLabelText(el, container) {
        if (el.id) {
            const label = container.querySelector(`label[for="${el.id}"]`);
            if (label) return label.textContent.trim().replace(/:$/, '');
        }
        const parentLabel = el.closest('label');
        if (parentLabel) return parentLabel.textContent.trim().replace(/:$/, '');
        return null;
    }

    // ============================================================
    // Execute Callback — calls REST API and returns MCP content format
    // ============================================================

    function createExecuteCallback(endpoint, method) {
        /**
         * MCP execute callback.
         * Must return { content: [{ type: 'text', text: '...' }] }
         * per the MCP content format used by Chrome's native API.
         */
        return async function (args) {
            let url = endpoint;

            // Replace {param} path placeholders
            const pathParams = endpoint.match(/\{(\w+)\}/g) || [];
            pathParams.forEach(placeholder => {
                const key = placeholder.replace(/[{}]/g, '');
                const val = args[key] || '';
                url = url.replace(placeholder, encodeURIComponent(val));
            });

            const fetchOpts = { method: method || 'POST' };

            if (method === 'GET' || method === 'HEAD') {
                const queryParams = Object.entries(args)
                    .filter(([k]) => !pathParams.some(p => p.includes(k)))
                    .filter(([, v]) => v !== '' && v !== undefined);
                if (queryParams.length > 0) {
                    url += '?' + new URLSearchParams(queryParams).toString();
                }
            } else {
                const bodyParams = {};
                Object.entries(args).forEach(([k, v]) => {
                    if (!pathParams.some(p => p.includes(k)) && v !== '' && v !== undefined) {
                        bodyParams[k] = v;
                    }
                });
                fetchOpts.headers = { 'Content-Type': 'application/json' };
                fetchOpts.body = JSON.stringify(bodyParams);
            }

            try {
                const response = await fetch(url, fetchOpts);
                let resultText;

                if (!response.ok) {
                    resultText = `Error HTTP ${response.status}: ${await response.text()}`;
                } else {
                    const ct = response.headers.get('content-type') || '';
                    if (ct.includes('json')) {
                        const data = await response.json();
                        resultText = typeof data === 'string' ? data : JSON.stringify(data, null, 2);
                    } else {
                        resultText = await response.text();
                    }
                }

                // Return in MCP content format
                return {
                    content: [{ type: 'text', text: resultText }]
                };
            } catch (e) {
                return {
                    content: [{ type: 'text', text: `Error: ${e.message}` }]
                };
            }
        };
    }

    // ============================================================
    // Auto-Discovery & Registration
    // ============================================================

    function scanAndRegister() {
        const toolContainers = document.querySelectorAll('[data-webmcp-tool]');
        let count = 0;

        toolContainers.forEach(container => {
            const name = container.getAttribute('data-webmcp-tool');
            const description = container.getAttribute('data-webmcp-description') || '';
            const endpoint = container.getAttribute('data-webmcp-endpoint') || '';
            const method = (container.getAttribute('data-webmcp-method') || 'POST').toUpperCase();

            if (!name || !description || !endpoint) {
                console.warn('[WebMCP Declarative] Skipping — missing name/description/endpoint', container);
                return;
            }

            const inputSchema = synthesizeSchema(container);

            try {
                navigator.modelContext.registerTool({
                    name: name,
                    description: description,
                    inputSchema: inputSchema,
                    async execute(args) {
                        const callback = createExecuteCallback(endpoint, method);
                        return await callback(args);
                    }
                });

                container.setAttribute('data-webmcp-registered', 'true');
                count++;
                console.log(`[WebMCP Declarative] Registered: ${name}`, inputSchema);
            } catch (e) {
                console.error(`[WebMCP Declarative] Failed "${name}":`, e.message);
            }
        });

        if (count > 0) {
            console.log(`[WebMCP Declarative] ${count} tool(s) registered from HTML`);
        }

        // Update navbar badge
        const badge = document.getElementById('webmcp-tool-count');
        if (badge) {
            badge.textContent = `${count} WebMCP tools`;
            badge.style.display = count > 0 ? 'inline-block' : 'none';
        }
    }

    // ============================================================
    // Public API
    // ============================================================
    window.WebMCP = {
        scan: scanAndRegister,
        unregisterAll() {
            document.querySelectorAll('[data-webmcp-tool]').forEach(el => {
                const name = el.getAttribute('data-webmcp-tool');
                try { navigator.modelContext.unregisterTool(name); } catch (e) {}
                el.removeAttribute('data-webmcp-registered');
            });
        }
    };

    // Auto-scan on DOM ready
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', scanAndRegister);
    } else {
        scanAndRegister();
    }

})();
