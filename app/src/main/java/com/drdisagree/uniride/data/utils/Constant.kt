package com.drdisagree.uniride.data.utils

object Constant {

    // Google Sign In
    const val WEB_CLIENT_ID = "YOUR_WEB_CLIENT_ID"

    // Firebase Collections
    const val ADMIN_COLLECTION = "admin_list"
    const val STUDENT_COLLECTION = "student_list"
    const val DRIVER_COLLECTION = "driver_list"
    const val WHICH_USER_COLLECTION = "which_user_list"
    const val BUS_COLLECTION = "bus_list"
    const val RUNNING_BUS_COLLECTION = "running_bus_list"
    const val BUS_CATEGORY_COLLECTION = "bus_category_list"
    const val ROUTE_CATEGORY_COLLECTION = "route_category_list"
    const val ROUTE_COLLECTION = "route_list"
    const val SCHEDULE_COLLECTION = "schedule_list"
    const val PLACE_COLLECTION = "place_list"
    const val DRIVER_DOCUMENT_COLLECTION = "driver_documents_list"
    const val ANNOUNCEMENT_COLLECTION = "announcement_list"
    const val DRIVE_HISTORY_COLLECTION = "drive_history_list"
    const val ISSUE_COLLECTION = "issue_list"
    const val DRIVER_REVIEW_COLLECTION = "driver_review_list"
    const val CHAT_COLLECTION = "chat_list"

    // Allowed Student Email Suffix
    val STUDENT_MAIL_SUFFIX = listOf(
        "@diu.edu.bd",
        "@daffodilvarsity.edu.bd"
    )

    // Phone Number Prefix
    const val PHONE_NUMBER_PREFIX = "+880"

    // Emergency Phone Numbers
    val EMERGENCY_PHONE_NUMBERS = listOf(
        "+8809617901212" to "DIU Helpline",
        "+8801847140037" to "Transport Office",
        "999" to "National Helpline",
    )

    // Driver Documents
    const val DRIVER_PROFILE_PICTURE = "driver_profile_picture"
    const val DRIVING_LICENSE_FRONT = "driving_license_front"
    const val DRIVING_LICENSE_BACK = "driving_license_back"
    const val NID_CARD_FRONT = "nid_card_front"
    const val NID_CARD_BACK = "nid_card_back"

    // Driver privacy policy
    const val DRIVER_PRIVACY_POLICY_URL =
        "https://drive.google.com/file/d/1mQ3pznvkVc0iNYTNCim4fcajVv-sshPt/view"
    const val ROAD_TRANSPORT_ACT_URL =
        "https://drive.google.com/file/d/1q5VO9USOz7-gOb_z7yrg01uNHzgUDoDE/view"

    // Gemini Prompts
    const val REVIEW_SUMMARY_PROMPT = "Summarize the following reviews given by students for the " +
            "driver. Try to keep the review as short as possible, preferably " +
            "in 2-3 sentences. Here are the reviews:\n\n"

    // Resource info url
    const val RESOURCE_INFO_URL = "https://pd.daffodilvarsity.edu.bd/transport-1"
}