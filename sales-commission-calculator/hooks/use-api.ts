"use client"

import { useState, useEffect, useCallback } from "react"
import {
  dealsApi,
  plansApi,
  calculationsApi,
  disputesApi,
  type DealResponse,
  type DealStatus,
  type CommissionPlanResponse,
  type PlanStatus,
  type CommissionCalculationResponse,
  type DisputeResponse,
  type DisputeStatusApi,
} from "@/lib/api"

interface UseApiState<T> {
  data: T | null
  loading: boolean
  error: string | null
}

function useApiCall<T>(fetcher: () => Promise<T>, deps: any[] = []) {
  const [state, setState] = useState<UseApiState<T>>({ data: null, loading: true, error: null })

  const refetch = useCallback(() => {
    setState((prev) => ({ ...prev, loading: true, error: null }))
    fetcher()
      .then((data) => setState({ data, loading: false, error: null }))
      .catch((err) => setState({ data: null, loading: false, error: err.message }))
  }, deps)

  useEffect(() => {
    refetch()
  }, [refetch])

  return { ...state, refetch }
}

// ── Deals ───────────────────────────────────────────────────────────────────

export function useDeals(params?: { salesRepId?: string; status?: DealStatus }) {
  return useApiCall<DealResponse[]>(() => dealsApi.getAll(params), [params?.salesRepId, params?.status])
}

export function useDeal(id: string) {
  return useApiCall<DealResponse>(() => dealsApi.get(id), [id])
}

// ── Plans ───────────────────────────────────────────────────────────────────

export function usePlans(status?: PlanStatus) {
  return useApiCall<CommissionPlanResponse[]>(() => plansApi.getAll(status), [status])
}

export function usePlan(id: string) {
  return useApiCall<CommissionPlanResponse>(() => plansApi.get(id), [id])
}

// ── Calculations ────────────────────────────────────────────────────────────

export function useCalculations(params?: { dealId?: string; salesRepId?: string }) {
  return useApiCall<CommissionCalculationResponse[]>(
    () => calculationsApi.getAll(params),
    [params?.dealId, params?.salesRepId],
  )
}

// ── Disputes ────────────────────────────────────────────────────────────────

export function useDisputes(params?: { salesRepId?: string; status?: DisputeStatusApi }) {
  return useApiCall<DisputeResponse[]>(() => disputesApi.getAll(params), [params?.salesRepId, params?.status])
}

export function useDispute(id: string) {
  return useApiCall<DisputeResponse>(() => disputesApi.get(id), [id])
}
