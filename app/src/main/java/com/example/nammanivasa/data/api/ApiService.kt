package com.example.nammanivasa.data.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    // --- Health ---
    @GET("/health")
    suspend fun health(): Response<ApiResponse<Unit>>

    @GET("/ready")
    suspend fun ready(): Response<ApiResponse<Unit>>

    // --- Auth / User ---
    @GET("/api/v1/me")
    suspend fun getMe(): Response<ApiResponse<MeResponse>>

    @GET("/api/v1/me/communities")
    suspend fun getMyCommunities(): Response<ApiResponse<List<CommunityMembershipInfo>>>

    // --- Communities ---
    /**
     * Finds ACTIVE communities by name for the signed-in user, independent of
     * membership. Discovery grants no membership or administrative access.
     */
    @GET("/api/v1/communities/discover")
    suspend fun discoverCommunities(
        @Query("q") query: String
    ): Response<ApiResponse<List<CommunitySearchResult>>>

    @GET("/api/v1/communities/{communityId}")
    suspend fun getCommunity(
        @Path("communityId") communityId: String
    ): Response<ApiResponse<CommunityDetailResponse>>

    // --- Notices ---
    @GET("/api/v1/communities/{communityId}/notices")
    suspend fun getNotices(
        @Path("communityId") communityId: String
    ): Response<ApiResponse<List<NoticeResponse>>>

    @POST("/api/v1/communities/{communityId}/notices")
    suspend fun createNotice(
        @Path("communityId") communityId: String,
        @Body request: CreateNoticeRequest
    ): Response<ApiResponse<NoticeResponse>>

    @POST("/api/v1/communities/{communityId}/notices/{noticeId}/publish")
    suspend fun publishNotice(
        @Path("communityId") communityId: String,
        @Path("noticeId") noticeId: String
    ): Response<ApiResponse<NoticeResponse>>

    // --- Maintenance Requests ---
    @GET("/api/v1/communities/{communityId}/maintenance-requests")
    suspend fun getMaintenanceRequests(
        @Path("communityId") communityId: String
    ): Response<ApiResponse<List<MaintenanceRequestResponse>>>

    @POST("/api/v1/communities/{communityId}/maintenance-requests")
    suspend fun createMaintenanceRequest(
        @Path("communityId") communityId: String,
        @Body request: CreateMaintenanceRequestRequest
    ): Response<ApiResponse<MaintenanceRequestResponse>>

    // --- Visitors ---
    @GET("/api/v1/communities/{communityId}/visitors")
    suspend fun getVisitors(
        @Path("communityId") communityId: String
    ): Response<ApiResponse<List<VisitorRequestResponse>>>

    @POST("/api/v1/communities/{communityId}/visitors")
    suspend fun createVisitorRequest(
        @Path("communityId") communityId: String,
        @Body request: CreateVisitorRequestRequest
    ): Response<ApiResponse<VisitorRequestResponse>>

    @POST("/api/v1/communities/{communityId}/visitors/{id}/approve")
    suspend fun approveVisitor(
        @Path("communityId") communityId: String,
        @Path("id") visitorId: String
    ): Response<ApiResponse<VisitorRequestResponse>>

    @POST("/api/v1/communities/{communityId}/visitors/{id}/reject")
    suspend fun rejectVisitor(
        @Path("communityId") communityId: String,
        @Path("id") visitorId: String
    ): Response<ApiResponse<VisitorRequestResponse>>

    @POST("/api/v1/communities/{communityId}/visitors/{id}/cancel")
    suspend fun cancelVisitor(
        @Path("communityId") communityId: String,
        @Path("id") visitorId: String
    ): Response<ApiResponse<VisitorRequestResponse>>

    // --- Bills ---
    @GET("/api/v1/communities/{communityId}/bills")
    suspend fun getBills(
        @Path("communityId") communityId: String
    ): Response<ApiResponse<List<BillResponse>>>

    @GET("/api/v1/communities/{communityId}/bills/{billId}")
    suspend fun getBill(
        @Path("communityId") communityId: String,
        @Path("billId") billId: String
    ): Response<ApiResponse<BillResponse>>

    @POST("/api/v1/communities/{communityId}/bills")
    suspend fun createBill(
        @Path("communityId") communityId: String,
        @Body request: CreateBillRequest
    ): Response<ApiResponse<BillResponse>>

    @PATCH("/api/v1/communities/{communityId}/bills/{billId}")
    suspend fun updateBill(
        @Path("communityId") communityId: String,
        @Path("billId") billId: String,
        @Body request: UpdateBillRequest
    ): Response<ApiResponse<BillResponse>>

    // --- Payments ---
    @GET("/api/v1/communities/{communityId}/payments/me")
    suspend fun getMyPayments(
        @Path("communityId") communityId: String
    ): Response<ApiResponse<List<PaymentResponse>>>

    @GET("/api/v1/communities/{communityId}/payments/{paymentId}")
    suspend fun getPayment(
        @Path("communityId") communityId: String,
        @Path("paymentId") paymentId: String
    ): Response<ApiResponse<PaymentResponse>>

    @POST("/api/v1/communities/{communityId}/payments")
    suspend fun createPayment(
        @Path("communityId") communityId: String,
        @Body request: CreatePaymentRequest
    ): Response<ApiResponse<PaymentResponse>>

    @POST("/api/v1/communities/{communityId}/payments/{paymentId}/verify")
    suspend fun verifyPayment(
        @Path("communityId") communityId: String,
        @Path("paymentId") paymentId: String,
        @Body request: VerifyPaymentRequest
    ): Response<ApiResponse<PaymentResponse>>

    @POST("/api/v1/communities/{communityId}/payments/{paymentId}/reject")
    suspend fun rejectPayment(
        @Path("communityId") communityId: String,
        @Path("paymentId") paymentId: String,
        @Body request: RejectPaymentRequest
    ): Response<ApiResponse<PaymentResponse>>

    @POST("/api/v1/communities/{communityId}/payments/{paymentId}/reverse")
    suspend fun reversePayment(
        @Path("communityId") communityId: String,
        @Path("paymentId") paymentId: String,
        @Body request: ReversePaymentRequest
    ): Response<ApiResponse<PaymentResponse>>

    // --- Household Financial Access ---
    @GET("/api/v1/communities/{communityId}/household-financial-access")
    suspend fun getFinancialAccessGrants(
        @Path("communityId") communityId: String
    ): Response<ApiResponse<List<FinancialAccessGrantResponse>>>

    @POST("/api/v1/communities/{communityId}/household-financial-access")
    suspend fun createFinancialAccessGrant(
        @Path("communityId") communityId: String,
        @Body request: CreateFinancialAccessGrantRequest
    ): Response<ApiResponse<FinancialAccessGrantResponse>>

    @POST("/api/v1/communities/{communityId}/household-financial-access/{grantId}/revoke")
    suspend fun revokeFinancialAccessGrant(
        @Path("communityId") communityId: String,
        @Path("grantId") grantId: String,
        @Body request: RevokeFinancialAccessGrantRequest
    ): Response<ApiResponse<FinancialAccessGrantResponse>>
}