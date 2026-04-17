from django.urls import path
from .views import (
    cancel_onboarding,
    register_phone_number,
    save_voice_assistant_preference,
    start_language_onboarding,
)

urlpatterns = [
    path('users/start-language/', start_language_onboarding),
    path('users/set-voice-assistant/', save_voice_assistant_preference),
    path('users/register-phone/', register_phone_number),
    path('users/cancel-onboarding/', cancel_onboarding),
]