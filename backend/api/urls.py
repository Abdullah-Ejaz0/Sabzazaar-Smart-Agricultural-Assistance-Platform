from django.urls import path
from api.views import (
    scan_views, community_views, soil_views,
    expert_views, auth_views, misc_views,
    disease_views, chatbot_views
)

urlpatterns = [
    # New simplified auth flow
    
    # path('users/check-phone/', ),
    # path('users/complete-signup/', ),

    # Auth and profile
    path('auth/onboarding/',             auth_views.complete_onboarding),
    path('auth/settings/',               auth_views.update_settings),
    path('auth/profile/',                auth_views.get_profile),

    # Scans
    path('scans/recent/',                scan_views.recent_scans),
    path('scans/detect/',                disease_views.detect_rice_disease),
    path('scans/<str:scan_id>/',         scan_views.scan_detail),
    path('scans/',                       scan_views.save_scan),

    # Soil health
    path('soil/latest/',                 soil_views.latest_soil),
    path('soil/',                        soil_views.save_soil),

    # Community
    path('community/',                   community_views.community_feed),
    path('community/post/',              community_views.submit_post),
    path('community/<str:post_id>/replies/', community_views.submit_reply),
    path('community/<str:post_id>/',     community_views.post_detail),

    # Expert portal
    path('expert/dashboard/',            expert_views.dashboard_stats),
    path('expert/pending/',              expert_views.pending_questions),
    path('expert/broadcast/',            expert_views.send_broadcast),

    # Misc
    path('chatbot/faqs/',                misc_views.chatbot_faqs),
    path('chatbot/ask/',                 chatbot_views.ask_chatbot),
    path('chatbot/history/',             chatbot_views.chatbot_history),
    path('broadcasts/',                  misc_views.broadcasts),
    path('storage/upload-url/',          misc_views.get_upload_url),
    path('weather/',                     misc_views.weather),

    # Health check
    path('health/',                      misc_views.health_check),
]
