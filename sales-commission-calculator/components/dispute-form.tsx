"use client"

import type React from "react"

import { useState } from "react"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Textarea } from "@/components/ui/textarea"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Badge } from "@/components/ui/badge"
import { Progress } from "@/components/ui/progress"
import { Alert, AlertDescription } from "@/components/ui/alert"
import { AlertCircle, Upload, X, FileText, DollarSign, CheckCircle, Info } from "lucide-react"
import type { DisputeType, DisputePriority, DisputeFormData } from "@/lib/dispute-workflow/types"
import { disputesApi } from "@/lib/api"

interface DisputeFormProps {
  onSubmit: (disputeId: string) => void
  onCancel: () => void
  dealId?: string
  commissionId?: string
  prefillAmount?: number
}

const DISPUTE_TYPES = [
  {
    value: "commission_calculation" as DisputeType,
    label: "Commission Calculation Error",
    description: "Issues with commission rate, calculation, or amount",
  },
  {
    value: "deal_attribution" as DisputeType,
    label: "Deal Attribution Issue",
    description: "Disputes about deal ownership or credit",
  },
  {
    value: "payout_timing" as DisputeType,
    label: "Payout Timing Problem",
    description: "Late or missing commission payments",
  },
  {
    value: "rate_discrepancy" as DisputeType,
    label: "Rate Discrepancy",
    description: "Incorrect commission rates applied",
  },
  {
    value: "bonus_eligibility" as DisputeType,
    label: "Bonus Eligibility",
    description: "Questions about bonus qualification or amounts",
  },
  {
    value: "other" as DisputeType,
    label: "Other Issue",
    description: "Other commission-related issues",
  },
]

const PRIORITY_LEVELS = [
  {
    value: "low" as DisputePriority,
    label: "Low",
    color: "bg-gray-100 text-gray-800",
    description: "Non-urgent issue, can wait 2+ weeks",
  },
  {
    value: "medium" as DisputePriority,
    label: "Medium",
    color: "bg-blue-100 text-blue-800",
    description: "Standard priority, resolve within 1 week",
  },
  {
    value: "high" as DisputePriority,
    label: "High",
    color: "bg-orange-100 text-orange-800",
    description: "Important issue, resolve within 3 days",
  },
  {
    value: "urgent" as DisputePriority,
    label: "Urgent",
    color: "bg-red-100 text-red-800",
    description: "Critical issue, immediate attention required",
  },
]

export function DisputeForm({ onSubmit, onCancel, dealId, commissionId, prefillAmount }: DisputeFormProps) {
  const [currentStep, setCurrentStep] = useState(1)
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [uploadProgress, setUploadProgress] = useState(0)
  const [errors, setErrors] = useState<Record<string, string>>({})
  const [newTag, setNewTag] = useState("")

  const [formData, setFormData] = useState<DisputeFormData>({
    title: "",
    description: "",
    type: "commission_calculation",
    priority: "medium",
    dealId: dealId || "",
    commissionId: commissionId || "",
    disputedAmount: prefillAmount || 0,
    expectedAmount: 0,
    documents: [],
    tags: [],
  })

  const validateForm = (): boolean => {
    const newErrors: Record<string, string> = {}

    if (!formData.title.trim()) {
      newErrors.title = "Title is required"
    }

    if (!formData.description.trim()) {
      newErrors.description = "Description is required"
    }

    if (formData.disputedAmount < 0) {
      newErrors.disputedAmount = "Disputed amount cannot be negative"
    }

    if (formData.expectedAmount < 0) {
      newErrors.expectedAmount = "Expected amount cannot be negative"
    }

    setErrors(newErrors)
    return Object.keys(newErrors).length === 0
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()

    if (!validateForm()) {
      return
    }

    setIsSubmitting(true)

    try {
      // Simulate file upload progress
      if (formData.documents.length > 0) {
        for (let i = 0; i <= 100; i += 10) {
          setUploadProgress(i)
          await new Promise((resolve) => setTimeout(resolve, 100))
        }
      }

      // Create dispute via API
      const dispute = await disputesApi.create({
        calculationId: formData.commissionId || formData.dealId || "unknown",
        salesRepId: "rep-001",
        title: formData.title,
        description: formData.description,
      })

      onSubmit(dispute.id)
    } catch (error) {
      console.error("Failed to create dispute:", error)
      setErrors({ submit: "Failed to create dispute. Please try again." })
    } finally {
      setIsSubmitting(false)
      setUploadProgress(0)
    }
  }

  const handleFileUpload = (e: React.ChangeEvent<HTMLInputElement>) => {
    const files = Array.from(e.target.files || [])
    setFormData((prev) => ({
      ...prev,
      documents: [...prev.documents, ...files],
    }))
  }

  const removeFile = (index: number) => {
    setFormData((prev) => ({
      ...prev,
      documents: prev.documents.filter((_, i) => i !== index),
    }))
  }

  const addTag = () => {
    if (newTag.trim() && !formData.tags.includes(newTag.trim())) {
      setFormData((prev) => ({
        ...prev,
        tags: [...prev.tags, newTag.trim()],
      }))
      setNewTag("")
    }
  }

  const removeTag = (tag: string) => {
    setFormData((prev) => ({
      ...prev,
      tags: prev.tags.filter((t) => t !== tag),
    }))
  }

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat("en-US", {
      style: "currency",
      currency: "USD",
    }).format(amount)
  }

  const formatFileSize = (bytes: number) => {
    if (bytes === 0) return "0 Bytes"
    const k = 1024
    const sizes = ["Bytes", "KB", "MB", "GB"]
    const i = Math.floor(Math.log(bytes) / Math.log(k))
    return Number.parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + " " + sizes[i]
  }

  const selectedType = DISPUTE_TYPES.find((type) => type.value === formData.type)
  const selectedPriority = PRIORITY_LEVELS.find((priority) => priority.value === formData.priority)

  return (
    <div className="max-w-4xl mx-auto space-y-6">
      {/* Progress Indicator */}
      <div className="flex items-center justify-between mb-6">
        <h2 className="text-2xl font-bold">Create New Dispute</h2>
        <div className="flex items-center gap-2">
          <span className="text-sm text-muted-foreground">Step {currentStep} of 3</span>
          <Progress value={(currentStep / 3) * 100} className="w-24" />
        </div>
      </div>

      <form onSubmit={handleSubmit} className="space-y-6">
        {/* Step 1: Basic Information */}
        {currentStep === 1 && (
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <Info className="w-5 h-5" />
                Basic Information
              </CardTitle>
              <CardDescription>Provide basic details about your dispute</CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="space-y-2">
                <Label htmlFor="title">Dispute Title *</Label>
                <Input
                  id="title"
                  value={formData.title}
                  onChange={(e) => setFormData((prev) => ({ ...prev, title: e.target.value }))}
                  placeholder="Brief description of the issue"
                  className={errors.title ? "border-red-500" : ""}
                />
                {errors.title && <p className="text-sm text-red-600">{errors.title}</p>}
              </div>

              <div className="space-y-2">
                <Label htmlFor="description">Detailed Description *</Label>
                <Textarea
                  id="description"
                  value={formData.description}
                  onChange={(e) => setFormData((prev) => ({ ...prev, description: e.target.value }))}
                  placeholder="Provide a detailed explanation of the issue, including relevant dates, amounts, and circumstances"
                  rows={4}
                  className={errors.description ? "border-red-500" : ""}
                />
                {errors.description && <p className="text-sm text-red-600">{errors.description}</p>}
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="type">Dispute Type *</Label>
                  <Select
                    value={formData.type}
                    onValueChange={(value: DisputeType) => setFormData((prev) => ({ ...prev, type: value }))}
                  >
                    <SelectTrigger>
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      {DISPUTE_TYPES.map((type) => (
                        <SelectItem key={type.value} value={type.value}>
                          <div>
                            <div className="font-medium">{type.label}</div>
                            <div className="text-xs text-muted-foreground">{type.description}</div>
                          </div>
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                  {selectedType && <p className="text-xs text-muted-foreground">{selectedType.description}</p>}
                </div>

                <div className="space-y-2">
                  <Label htmlFor="priority">Priority *</Label>
                  <Select
                    value={formData.priority}
                    onValueChange={(value: DisputePriority) => setFormData((prev) => ({ ...prev, priority: value }))}
                  >
                    <SelectTrigger>
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      {PRIORITY_LEVELS.map((priority) => (
                        <SelectItem key={priority.value} value={priority.value}>
                          <div className="flex items-center gap-2">
                            <Badge className={priority.color}>{priority.label}</Badge>
                            <span className="text-xs">{priority.description}</span>
                          </div>
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                  {selectedPriority && <p className="text-xs text-muted-foreground">{selectedPriority.description}</p>}
                </div>
              </div>

              <div className="flex justify-end">
                <Button type="button" onClick={() => setCurrentStep(2)}>
                  Next: Financial Details
                </Button>
              </div>
            </CardContent>
          </Card>
        )}

        {/* Step 2: Financial Information */}
        {currentStep === 2 && (
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <DollarSign className="w-5 h-5" />
                Financial Details
              </CardTitle>
              <CardDescription>Specify the amounts involved in this dispute</CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="disputedAmount">Current Amount</Label>
                  <div className="relative">
                    <DollarSign className="absolute left-3 top-1/2 transform -translate-y-1/2 w-4 h-4 text-muted-foreground" />
                    <Input
                      id="disputedAmount"
                      type="number"
                      step="0.01"
                      value={formData.disputedAmount}
                      onChange={(e) =>
                        setFormData((prev) => ({
                          ...prev,
                          disputedAmount: Number.parseFloat(e.target.value) || 0,
                        }))
                      }
                      className={`pl-10 ${errors.disputedAmount ? "border-red-500" : ""}`}
                      placeholder="0.00"
                    />
                  </div>
                  <p className="text-xs text-muted-foreground">Amount you currently received</p>
                  {errors.disputedAmount && <p className="text-sm text-red-600">{errors.disputedAmount}</p>}
                </div>

                <div className="space-y-2">
                  <Label htmlFor="expectedAmount">Expected Amount</Label>
                  <div className="relative">
                    <DollarSign className="absolute left-3 top-1/2 transform -translate-y-1/2 w-4 h-4 text-muted-foreground" />
                    <Input
                      id="expectedAmount"
                      type="number"
                      step="0.01"
                      value={formData.expectedAmount}
                      onChange={(e) =>
                        setFormData((prev) => ({
                          ...prev,
                          expectedAmount: Number.parseFloat(e.target.value) || 0,
                        }))
                      }
                      className={`pl-10 ${errors.expectedAmount ? "border-red-500" : ""}`}
                      placeholder="0.00"
                    />
                  </div>
                  <p className="text-xs text-muted-foreground">Amount you believe you should receive</p>
                  {errors.expectedAmount && <p className="text-sm text-red-600">{errors.expectedAmount}</p>}
                </div>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="dealId">Related Deal ID</Label>
                  <Input
                    id="dealId"
                    value={formData.dealId}
                    onChange={(e) => setFormData((prev) => ({ ...prev, dealId: e.target.value }))}
                    placeholder="deal-001"
                  />
                  <p className="text-xs text-muted-foreground">Related deal identifier (if applicable)</p>
                </div>

                <div className="space-y-2">
                  <Label htmlFor="commissionId">Commission ID</Label>
                  <Input
                    id="commissionId"
                    value={formData.commissionId}
                    onChange={(e) => setFormData((prev) => ({ ...prev, commissionId: e.target.value }))}
                    placeholder="comm-001"
                  />
                  <p className="text-xs text-muted-foreground">Related commission record (if applicable)</p>
                </div>
              </div>

              {/* Amount Summary */}
              {(formData.disputedAmount > 0 || formData.expectedAmount > 0) && (
                <Alert>
                  <Info className="h-4 w-4" />
                  <AlertDescription>
                    <div className="space-y-1">
                      {formData.disputedAmount > 0 && (
                        <div>
                          <strong>Current Amount:</strong> {formatCurrency(formData.disputedAmount)}
                        </div>
                      )}
                      {formData.expectedAmount > 0 && (
                        <div>
                          <strong>Expected Amount:</strong> {formatCurrency(formData.expectedAmount)}
                        </div>
                      )}
                      {formData.expectedAmount !== formData.disputedAmount && formData.expectedAmount > 0 && (
                        <div
                          className={`font-medium ${formData.expectedAmount > formData.disputedAmount ? "text-green-600" : "text-red-600"}`}
                        >
                          <strong>Difference:</strong>{" "}
                          {formatCurrency(formData.expectedAmount - formData.disputedAmount)}
                        </div>
                      )}
                    </div>
                  </AlertDescription>
                </Alert>
              )}

              <div className="flex justify-between">
                <Button type="button" variant="outline" onClick={() => setCurrentStep(1)}>
                  Back
                </Button>
                <Button type="button" onClick={() => setCurrentStep(3)}>
                  Next: Supporting Documents
                </Button>
              </div>
            </CardContent>
          </Card>
        )}

        {/* Step 3: Supporting Documents and Tags */}
        {currentStep === 3 && (
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <FileText className="w-5 h-5" />
                Supporting Documents & Tags
              </CardTitle>
              <CardDescription>Upload relevant documents and add tags to help categorize your dispute</CardDescription>
            </CardHeader>
            <CardContent className="space-y-6">
              {/* File Upload */}
              <div className="space-y-4">
                <Label>Supporting Documents</Label>
                <div className="border-2 border-dashed border-gray-300 rounded-lg p-6 text-center">
                  <Upload className="w-8 h-8 text-muted-foreground mx-auto mb-2" />
                  <p className="text-sm text-muted-foreground mb-2">Drag and drop files here, or click to browse</p>
                  <input
                    type="file"
                    multiple
                    accept=".pdf,.doc,.docx,.xls,.xlsx,.png,.jpg,.jpeg"
                    onChange={handleFileUpload}
                    className="hidden"
                    id="file-upload"
                  />
                  <Button type="button" variant="outline" asChild>
                    <label htmlFor="file-upload" className="cursor-pointer">
                      Choose Files
                    </label>
                  </Button>
                  <p className="text-xs text-muted-foreground mt-2">
                    Supported formats: PDF, DOC, DOCX, XLS, XLSX, PNG, JPG (Max 10MB each)
                  </p>
                </div>

                {/* Uploaded Files */}
                {formData.documents.length > 0 && (
                  <div className="space-y-2">
                    <Label>Uploaded Files ({formData.documents.length})</Label>
                    {formData.documents.map((file, index) => (
                      <div key={index} className="flex items-center justify-between p-2 border rounded">
                        <div className="flex items-center gap-2">
                          <FileText className="w-4 h-4" />
                          <span className="text-sm">{file.name}</span>
                          <span className="text-xs text-muted-foreground">({formatFileSize(file.size)})</span>
                        </div>
                        <Button type="button" variant="ghost" size="sm" onClick={() => removeFile(index)}>
                          <X className="w-4 h-4" />
                        </Button>
                      </div>
                    ))}
                  </div>
                )}

                {/* Upload Progress */}
                {uploadProgress > 0 && uploadProgress < 100 && (
                  <div className="space-y-2">
                    <div className="flex items-center justify-between">
                      <span className="text-sm">Uploading files...</span>
                      <span className="text-sm">{uploadProgress}%</span>
                    </div>
                    <Progress value={uploadProgress} />
                  </div>
                )}
              </div>

              {/* Tags */}
              <div className="space-y-4">
                <Label>Tags</Label>
                <div className="flex items-center gap-2">
                  <Input
                    value={newTag}
                    onChange={(e) => setNewTag(e.target.value)}
                    placeholder="Add a tag"
                    onKeyPress={(e) => e.key === "Enter" && (e.preventDefault(), addTag())}
                  />
                  <Button type="button" variant="outline" onClick={addTag}>
                    Add
                  </Button>
                </div>
                {formData.tags.length > 0 && (
                  <div className="flex flex-wrap gap-2">
                    {formData.tags.map((tag) => (
                      <Badge key={tag} variant="secondary" className="flex items-center gap-1">
                        {tag}
                        <Button
                          type="button"
                          variant="ghost"
                          size="sm"
                          className="h-auto p-0 hover:bg-transparent"
                          onClick={() => removeTag(tag)}
                        >
                          <X className="w-3 h-3" />
                        </Button>
                      </Badge>
                    ))}
                  </div>
                )}
                <p className="text-xs text-muted-foreground">
                  Tags help categorize and search for disputes. Examples: urgent, Q1-2024, enterprise-deal
                </p>
              </div>

              {/* Submit Section */}
              <div className="space-y-4 pt-4 border-t">
                {errors.submit && (
                  <Alert>
                    <AlertCircle className="h-4 w-4" />
                    <AlertDescription>{errors.submit}</AlertDescription>
                  </Alert>
                )}

                <div className="flex justify-between">
                  <Button type="button" variant="outline" onClick={() => setCurrentStep(2)}>
                    Back
                  </Button>
                  <div className="flex gap-2">
                    <Button type="button" variant="outline" onClick={onCancel}>
                      Cancel
                    </Button>
                    <Button type="submit" disabled={isSubmitting}>
                      {isSubmitting ? (
                        <>
                          <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin mr-2" />
                          Creating Dispute...
                        </>
                      ) : (
                        <>
                          <CheckCircle className="w-4 h-4 mr-2" />
                          Create Dispute
                        </>
                      )}
                    </Button>
                  </div>
                </div>
              </div>
            </CardContent>
          </Card>
        )}
      </form>
    </div>
  )
}
