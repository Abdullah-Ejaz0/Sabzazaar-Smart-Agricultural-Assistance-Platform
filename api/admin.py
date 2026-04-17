from django.contrib import admin
from .models import LanguageOnboardingSession, ScanImageUpload, UserPreference


@admin.register(UserPreference)
class UserPreferenceAdmin(admin.ModelAdmin):
	list_display = ('id', 'phone_number', 'preferred_language', 'voice_assistant_enabled', 'created_at', 'updated_at')
	search_fields = ('phone_number', 'preferred_language')


@admin.register(LanguageOnboardingSession)
class LanguageOnboardingSessionAdmin(admin.ModelAdmin):
	list_display = ('token', 'preferred_language', 'voice_assistant_enabled', 'created_at', 'expires_at')
	search_fields = ('preferred_language',)


@admin.register(ScanImageUpload)
class ScanImageUploadAdmin(admin.ModelAdmin):
	list_display = ('id', 'user_preference', 'source', 'analysis_status', 'created_at')
	list_filter = ('source', 'analysis_status', 'created_at')
	search_fields = ('user_preference__phone_number',)
