"use client"

import { useState } from "react"
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle } from "@/components/ui/dialog"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { Progress } from "@/components/ui/progress"
import { Separator } from "@/components/ui/separator"
import { Alert, AlertDescription } from "@/components/ui/alert"
import {
  Building2,
  Calendar,
  User,
  Phone,
  Mail,
  AlertTriangle,
  CheckCircle,
  Clock,
  ExternalLink,
  Copy,
  Edit,
  Calculator,
  FileText,
  X,
} from "lucide-react"

interface DealData {
  id: string
  title: string
  description: string
  company: string
  contactName: string
  contactEmail: string
  contactPhone: string
  stage: string
  probability: number
  value: number
  originalValue?: number
  discount?: number
  discountType?: "percentage" | "fixed"
  startDate: string
  closeDate: string
  createdDate: string
  lastModified: string
  source: string
  dealType: string
  products: Array<{
    id: string
    name: string
    quantity: number
    unitPrice: number
    totalPrice: number
  }>
  commissionDetails: {
    baseCommission: number
    acceleratorBonus?: number
    spifBonus?: number
    totalCommission: number
    commissionRate: number
    planName: string
  }
  images?: string[]
  documents?: Array<{
    id: string
    name: string
    type: string
    url: string
  }>
  notes?: string
  termsAndConditions?: string
  status: "open" | "won" | "lost" | "pending"
}

interface DealModalProps {
  isOpen: boolean
  onClose: () => void
  dealData: DealData | null
  userRole: "sales-rep" | "manager" | "admin"
  onApplyDeal?: (dealId: string) => void
  onEditDeal?: (dealId: string) => void
  onViewInHubSpot?: (dealId: string) => void
}

export function DealModal({
  isOpen,
  onClose,
  dealData,
  userRole,
  onApplyDeal,
  onEditDeal,
  onViewInHubSpot,
}: DealModalProps) {
  const [isLoading, setIsLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [selectedImage, setSelectedImage] = useState<string | null>(null)

  // Error handling for missing or invalid data
  if (!dealData) {
    return (
      <Dialog open={isOpen} onOpenChange={onClose}>
        <DialogContent className="max-w-md">
          <DialogHeader>
            <DialogTitle className="flex items-center gap-2">
              <AlertTriangle className="w-5 h-5 text-red-500" />
              Deal Not Found
            </DialogTitle>
            <DialogDescription>
              The requested deal information could not be loaded. This may be due to a network issue or the deal may no
              longer exist.
            </DialogDescription>
          </DialogHeader>
          <div className="flex justify-end gap-2 pt-4">
            <Button variant="outline" onClick={onClose}>
              Close
            </Button>
            <Button onClick={() => window.location.reload()}>Retry</Button>
          </div>
        </DialogContent>
      </Dialog>
    )
  }

  const handleApplyDeal = async () => {
    if (!onApplyDeal) return

    setIsLoading(true)
    setError(null)

    try {
      await onApplyDeal(dealData.id)
      onClose()
    } catch (err) {
      setError("Failed to apply deal. Please try again.")
    } finally {
      setIsLoading(false)
    }
  }

  const handleEditDeal = () => {
    if (onEditDeal) {
      onEditDeal(dealData.id)
    }
  }

  const handleViewInHubSpot = () => {
    if (onViewInHubSpot) {
      onViewInHubSpot(dealData.id)
    }
  }

  const copyDealId = () => {
    navigator.clipboard.writeText(dealData.id)
  }

  const getStatusColor = (status: string) => {
    switch (status) {
      case "won":
        return "bg-green-100 text-green-800 border-green-200"
      case "lost":
        return "bg-red-100 text-red-800 border-red-200"
      case "pending":
        return "bg-yellow-100 text-yellow-800 border-yellow-200"
      default:
        return "bg-blue-100 text-blue-800 border-blue-200"
    }
  }

  const getStageColor = (stage: string) => {
    const stageColors: Record<string, string> = {
      Qualified: "bg-blue-100 text-blue-800",
      Discovery: "bg-purple-100 text-purple-800",
      Proposal: "bg-orange-100 text-orange-800",
      Negotiation: "bg-yellow-100 text-yellow-800",
      "Closed Won": "bg-green-100 text-green-800",
      "Closed Lost": "bg-red-100 text-red-800",
    }
    return stageColors[stage] || "bg-gray-100 text-gray-800"
  }

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat("en-US", {
      style: "currency",
      currency: "USD",
    }).format(amount)
  }

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString("en-US", {
      year: "numeric",
      month: "long",
      day: "numeric",
    })
  }

  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent className="max-w-4xl max-h-[90vh] overflow-hidden flex flex-col">
        <DialogHeader className="flex-shrink-0">
          <div className="flex items-start justify-between">
            <div className="flex-1">
              <DialogTitle className="text-xl font-bold flex items-center gap-2">
                <Building2 className="w-5 h-5" />
                {dealData.title}
              </DialogTitle>
              <DialogDescription className="mt-1">
                {dealData.company} • Deal ID: {dealData.id}
                <Button variant="ghost" size="sm" className="ml-2 h-auto p-1" onClick={copyDealId}>
                  <Copy className="w-3 h-3" />
                </Button>
              </DialogDescription>
            </div>
            <div className="flex items-center gap-2">
              <Badge className={getStatusColor(dealData.status)}>{dealData.status.toUpperCase()}</Badge>
              <Badge variant="outline" className={getStageColor(dealData.stage)}>
                {dealData.stage}
              </Badge>
            </div>
          </div>
        </DialogHeader>

        {error && (
          <Alert className="mb-4">
            <AlertTriangle className="h-4 w-4" />
            <AlertDescription>{error}</AlertDescription>
          </Alert>
        )}

        <div className="flex-1 overflow-auto">
          <Tabs defaultValue="overview" className="w-full">
            <TabsList className="grid w-full grid-cols-4">
              <TabsTrigger value="overview">Overview</TabsTrigger>
              <TabsTrigger value="commission">Commission</TabsTrigger>
              <TabsTrigger value="products">Products</TabsTrigger>
              <TabsTrigger value="documents">Documents</TabsTrigger>
            </TabsList>

            <TabsContent value="overview" className="space-y-4 mt-4">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                {/* Deal Information */}
                <Card>
                  <CardHeader>
                    <CardTitle className="text-lg">Deal Information</CardTitle>
                  </CardHeader>
                  <CardContent className="space-y-4">
                    <div className="space-y-3">
                      <div className="flex items-center justify-between">
                        <span className="text-sm font-medium">Deal Value</span>
                        <div className="text-right">
                          {dealData.originalValue && dealData.originalValue !== dealData.value && (
                            <div className="text-sm text-muted-foreground line-through">
                              {formatCurrency(dealData.originalValue)}
                            </div>
                          )}
                          <div className="text-lg font-bold">{formatCurrency(dealData.value)}</div>
                        </div>
                      </div>

                      {dealData.discount && (
                        <div className="flex items-center justify-between">
                          <span className="text-sm font-medium">Discount Applied</span>
                          <Badge variant="secondary">
                            {dealData.discountType === "percentage"
                              ? `${dealData.discount}% off`
                              : `${formatCurrency(dealData.discount)} off`}
                          </Badge>
                        </div>
                      )}

                      <div className="flex items-center justify-between">
                        <span className="text-sm font-medium">Probability</span>
                        <div className="flex items-center gap-2">
                          <Progress value={dealData.probability} className="w-16" />
                          <span className="text-sm font-medium">{dealData.probability}%</span>
                        </div>
                      </div>

                      <Separator />

                      <div className="space-y-2">
                        <div className="flex items-center gap-2">
                          <Calendar className="w-4 h-4 text-muted-foreground" />
                          <span className="text-sm">
                            <strong>Created:</strong> {formatDate(dealData.createdDate)}
                          </span>
                        </div>
                        <div className="flex items-center gap-2">
                          <Calendar className="w-4 h-4 text-muted-foreground" />
                          <span className="text-sm">
                            <strong>Expected Close:</strong> {formatDate(dealData.closeDate)}
                          </span>
                        </div>
                        <div className="flex items-center gap-2">
                          <Clock className="w-4 h-4 text-muted-foreground" />
                          <span className="text-sm">
                            <strong>Last Modified:</strong> {formatDate(dealData.lastModified)}
                          </span>
                        </div>
                      </div>
                    </div>
                  </CardContent>
                </Card>

                {/* Contact Information */}
                <Card>
                  <CardHeader>
                    <CardTitle className="text-lg">Contact Information</CardTitle>
                  </CardHeader>
                  <CardContent className="space-y-3">
                    <div className="flex items-center gap-2">
                      <User className="w-4 h-4 text-muted-foreground" />
                      <span className="font-medium">{dealData.contactName}</span>
                    </div>
                    <div className="flex items-center gap-2">
                      <Mail className="w-4 h-4 text-muted-foreground" />
                      <a href={`mailto:${dealData.contactEmail}`} className="text-blue-600 hover:underline">
                        {dealData.contactEmail}
                      </a>
                    </div>
                    <div className="flex items-center gap-2">
                      <Phone className="w-4 h-4 text-muted-foreground" />
                      <a href={`tel:${dealData.contactPhone}`} className="text-blue-600 hover:underline">
                        {dealData.contactPhone}
                      </a>
                    </div>
                    <div className="flex items-center gap-2">
                      <Building2 className="w-4 h-4 text-muted-foreground" />
                      <span>{dealData.company}</span>
                    </div>
                  </CardContent>
                </Card>
              </div>

              {/* Description */}
              {dealData.description && (
                <Card>
                  <CardHeader>
                    <CardTitle className="text-lg">Description</CardTitle>
                  </CardHeader>
                  <CardContent>
                    <p className="text-sm text-muted-foreground leading-relaxed">{dealData.description}</p>
                  </CardContent>
                </Card>
              )}

              {/* Images */}
              {dealData.images && dealData.images.length > 0 && (
                <Card>
                  <CardHeader>
                    <CardTitle className="text-lg">Images</CardTitle>
                  </CardHeader>
                  <CardContent>
                    <div className="grid grid-cols-2 md:grid-cols-4 gap-2">
                      {dealData.images.map((image, index) => (
                        <div
                          key={index}
                          className="aspect-square bg-gray-100 rounded-lg overflow-hidden cursor-pointer hover:opacity-80 transition-opacity"
                          onClick={() => setSelectedImage(image)}
                        >
                          <img
                            src={image || "/placeholder.svg"}
                            alt={`Deal image ${index + 1}`}
                            className="w-full h-full object-cover"
                            onError={(e) => {
                              const target = e.target as HTMLImageElement
                              target.style.display = "none"
                              target.parentElement!.innerHTML = `
                                <div class="w-full h-full flex items-center justify-center">
                                  <div class="text-center">
                                    <ImageIcon class="w-8 h-8 text-muted-foreground mx-auto mb-2" />
                                    <span class="text-xs text-muted-foreground">Image unavailable</span>
                                  </div>
                                </div>
                              `
                            }}
                          />
                        </div>
                      ))}
                    </div>
                  </CardContent>
                </Card>
              )}

              {/* Terms and Conditions */}
              {dealData.termsAndConditions && (
                <Card>
                  <CardHeader>
                    <CardTitle className="text-lg">Terms and Conditions</CardTitle>
                  </CardHeader>
                  <CardContent>
                    <p className="text-sm text-muted-foreground leading-relaxed whitespace-pre-wrap">
                      {dealData.termsAndConditions}
                    </p>
                  </CardContent>
                </Card>
              )}
            </TabsContent>

            <TabsContent value="commission" className="space-y-4 mt-4">
              <Card>
                <CardHeader>
                  <CardTitle className="text-lg flex items-center gap-2">
                    <Calculator className="w-5 h-5" />
                    Commission Breakdown
                  </CardTitle>
                  <CardDescription>
                    Commission calculation based on {dealData.commissionDetails.planName}
                  </CardDescription>
                </CardHeader>
                <CardContent className="space-y-4">
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div className="space-y-3">
                      <div className="flex items-center justify-between">
                        <span className="text-sm font-medium">Commission Rate</span>
                        <span className="font-bold">{dealData.commissionDetails.commissionRate}%</span>
                      </div>
                      <div className="flex items-center justify-between">
                        <span className="text-sm font-medium">Base Commission</span>
                        <span className="font-bold">{formatCurrency(dealData.commissionDetails.baseCommission)}</span>
                      </div>
                      {dealData.commissionDetails.acceleratorBonus && (
                        <div className="flex items-center justify-between">
                          <span className="text-sm font-medium">Accelerator Bonus</span>
                          <span className="font-bold text-green-600">
                            +{formatCurrency(dealData.commissionDetails.acceleratorBonus)}
                          </span>
                        </div>
                      )}
                      {dealData.commissionDetails.spifBonus && (
                        <div className="flex items-center justify-between">
                          <span className="text-sm font-medium">SPIF Bonus</span>
                          <span className="font-bold text-purple-600">
                            +{formatCurrency(dealData.commissionDetails.spifBonus)}
                          </span>
                        </div>
                      )}
                      <Separator />
                      <div className="flex items-center justify-between">
                        <span className="text-lg font-bold">Total Commission</span>
                        <span className="text-lg font-bold text-green-600">
                          {formatCurrency(dealData.commissionDetails.totalCommission)}
                        </span>
                      </div>
                    </div>
                    <div className="bg-gray-50 p-4 rounded-lg">
                      <h4 className="font-medium mb-2">Commission Plan Details</h4>
                      <p className="text-sm text-muted-foreground">
                        This deal is calculated using the <strong>{dealData.commissionDetails.planName}</strong>{" "}
                        commission plan. The calculation includes base commission rate and any applicable bonuses based
                        on your current quota attainment and deal characteristics.
                      </p>
                    </div>
                  </div>
                </CardContent>
              </Card>
            </TabsContent>

            <TabsContent value="products" className="space-y-4 mt-4">
              <Card>
                <CardHeader>
                  <CardTitle className="text-lg">Products & Services</CardTitle>
                </CardHeader>
                <CardContent>
                  {dealData.products && dealData.products.length > 0 ? (
                    <div className="space-y-3">
                      {dealData.products.map((product) => (
                        <div key={product.id} className="flex items-center justify-between p-3 border rounded-lg">
                          <div className="flex-1">
                            <h4 className="font-medium">{product.name}</h4>
                            <p className="text-sm text-muted-foreground">
                              Quantity: {product.quantity} × {formatCurrency(product.unitPrice)}
                            </p>
                          </div>
                          <div className="text-right">
                            <div className="font-bold">{formatCurrency(product.totalPrice)}</div>
                          </div>
                        </div>
                      ))}
                      <Separator />
                      <div className="flex items-center justify-between font-bold">
                        <span>Total Deal Value</span>
                        <span>{formatCurrency(dealData.value)}</span>
                      </div>
                    </div>
                  ) : (
                    <div className="text-center py-8">
                      <FileText className="w-12 h-12 text-muted-foreground mx-auto mb-4" />
                      <h3 className="text-lg font-medium mb-2">No Products Listed</h3>
                      <p className="text-muted-foreground">Product details are not available for this deal.</p>
                    </div>
                  )}
                </CardContent>
              </Card>
            </TabsContent>

            <TabsContent value="documents" className="space-y-4 mt-4">
              <Card>
                <CardHeader>
                  <CardTitle className="text-lg">Documents & Attachments</CardTitle>
                </CardHeader>
                <CardContent>
                  {dealData.documents && dealData.documents.length > 0 ? (
                    <div className="space-y-3">
                      {dealData.documents.map((document) => (
                        <div key={document.id} className="flex items-center justify-between p-3 border rounded-lg">
                          <div className="flex items-center gap-3">
                            <FileText className="w-5 h-5 text-muted-foreground" />
                            <div>
                              <h4 className="font-medium">{document.name}</h4>
                              <p className="text-sm text-muted-foreground">{document.type}</p>
                            </div>
                          </div>
                          <Button variant="outline" size="sm" asChild>
                            <a href={document.url} target="_blank" rel="noopener noreferrer">
                              <ExternalLink className="w-4 h-4 mr-2" />
                              View
                            </a>
                          </Button>
                        </div>
                      ))}
                    </div>
                  ) : (
                    <div className="text-center py-8">
                      <FileText className="w-12 h-12 text-muted-foreground mx-auto mb-4" />
                      <h3 className="text-lg font-medium mb-2">No Documents</h3>
                      <p className="text-muted-foreground">No documents or attachments are available for this deal.</p>
                    </div>
                  )}
                </CardContent>
              </Card>
            </TabsContent>
          </Tabs>
        </div>

        {/* Action Buttons */}
        <div className="flex-shrink-0 flex items-center justify-between pt-4 border-t">
          <div className="flex items-center gap-2">
            <Button variant="outline" size="sm" onClick={handleViewInHubSpot}>
              <ExternalLink className="w-4 h-4 mr-2" />
              View in HubSpot
            </Button>
            {(userRole === "admin" || userRole === "manager") && (
              <Button variant="outline" size="sm" onClick={handleEditDeal}>
                <Edit className="w-4 h-4 mr-2" />
                Edit Deal
              </Button>
            )}
          </div>
          <div className="flex items-center gap-2">
            <Button variant="outline" onClick={onClose}>
              Close
            </Button>
            {dealData.status === "open" && onApplyDeal && (
              <Button onClick={handleApplyDeal} disabled={isLoading}>
                {isLoading ? (
                  <>
                    <Clock className="w-4 h-4 mr-2 animate-spin" />
                    Processing...
                  </>
                ) : (
                  <>
                    <CheckCircle className="w-4 h-4 mr-2" />
                    Apply Deal
                  </>
                )}
              </Button>
            )}
          </div>
        </div>

        {/* Image Preview Modal */}
        {selectedImage && (
          <Dialog open={!!selectedImage} onOpenChange={() => setSelectedImage(null)}>
            <DialogContent className="max-w-3xl">
              <DialogHeader>
                <DialogTitle>Image Preview</DialogTitle>
              </DialogHeader>
              <div className="relative">
                <img
                  src={selectedImage || "/placeholder.svg"}
                  alt="Deal image preview"
                  className="w-full h-auto max-h-[70vh] object-contain rounded-lg"
                />
                <Button
                  variant="outline"
                  size="sm"
                  className="absolute top-2 right-2 bg-transparent"
                  onClick={() => setSelectedImage(null)}
                >
                  <X className="w-4 h-4" />
                </Button>
              </div>
            </DialogContent>
          </Dialog>
        )}
      </DialogContent>
    </Dialog>
  )
}
