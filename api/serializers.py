from rest_framework import serializers

from .models import LanguageOnboardingSession, ScanImageUpload, UserPreference


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


class ScanImageUploadInputSerializer(serializers.Serializer):
    phone_number = serializers.CharField(max_length=25)
    image = serializers.ImageField()
    source = serializers.ChoiceField(choices=ScanImageUpload.ImageSource.choices)


class ScanImageUploadSerializer(serializers.ModelSerializer):
    phone_number = serializers.CharField(source='user_preference.phone_number', read_only=True)
    image_url = serializers.SerializerMethodField()

    class Meta:
        model = ScanImageUpload
        fields = [
            'id',
            'phone_number',
            'source',
            'analysis_status',
            'analysis_result',
            'image',
            'image_url',
            'created_at',
            'updated_at',
        ]
        read_only_fields = fields

    def get_image_url(self, obj):
        request = self.context.get('request')
        if request is None:
            return obj.image.url
        return request.build_absolute_uri(obj.image.url)
