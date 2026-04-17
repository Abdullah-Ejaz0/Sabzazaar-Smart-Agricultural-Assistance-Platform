from django.contrib import admin
from .models import LanguageOnboardingSession, UserPreference


@admin.register(UserPreference)
class UserPreferenceAdmin(admin.ModelAdmin):
	list_display = ('id', 'phone_number', 'preferred_language', 'voice_assistant_enabled', 'created_at', 'updated_at')
	search_fields = ('phone_number', 'preferred_language')


@admin.register(LanguageOnboardingSession)
class LanguageOnboardingSessionAdmin(admin.ModelAdmin):
	list_display = ('token', 'preferred_language', 'voice_assistant_enabled', 'created_at', 'expires_at')
	search_fields = ('preferred_language',)
