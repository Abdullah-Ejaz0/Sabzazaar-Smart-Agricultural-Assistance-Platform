from rest_framework import serializers

from .models import LanguageOnboardingSession, UserPreference


class StartLanguageOnboardingSerializer(serializers.Serializer):
    preferred_language = serializers.CharField(max_length=30)


class VoiceAssistantInputSerializer(serializers.Serializer):
    onboarding_token = serializers.UUIDField()
    voice_assistant_enabled = serializers.BooleanField()


class LanguageOnboardingSessionSerializer(serializers.ModelSerializer):
    class Meta:
        model = LanguageOnboardingSession
        fields = ['token', 'preferred_language', 'voice_assistant_enabled', 'created_at', 'expires_at']
        read_only_fields = fields


class PhoneInputSerializer(serializers.Serializer):
    phone_number = serializers.CharField(max_length=25)
    onboarding_token = serializers.UUIDField()


class CancelOnboardingSerializer(serializers.Serializer):
    onboarding_token = serializers.UUIDField()


class UserPreferenceSerializer(serializers.ModelSerializer):
    class Meta:
        model = UserPreference
        fields = [
            'id',
            'phone_number',
            'preferred_language',
            'voice_assistant_enabled',
            'created_at',
            'updated_at',
        ]
        read_only_fields = ['id', 'created_at', 'updated_at']
