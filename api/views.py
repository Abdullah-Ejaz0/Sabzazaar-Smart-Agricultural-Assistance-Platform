from rest_framework.decorators import api_view, permission_classes
from rest_framework.decorators import parser_classes
from rest_framework.permissions import AllowAny
from rest_framework.response import Response
from rest_framework import status
from rest_framework.parsers import FormParser, MultiPartParser
from django.utils import timezone

from .models import LanguageOnboardingSession, ScanImageUpload, UserPreference
from .serializers import (
    CancelOnboardingSerializer,
    LanguageOnboardingSessionSerializer,
    PhoneInputSerializer,
    ScanImageUploadInputSerializer,
    ScanImageUploadSerializer,
    StartLanguageOnboardingSerializer,
    UserPreferenceSerializer,
    VoiceAssistantInputSerializer,
)


def cleanup_expired_onboarding_sessions():
    LanguageOnboardingSession.objects.filter(expires_at__lt=timezone.now()).delete()


@api_view(['POST'])
@permission_classes([AllowAny])
def start_language_onboarding(request):
    cleanup_expired_onboarding_sessions()

    serializer = StartLanguageOnboardingSerializer(data=request.data)
    serializer.is_valid(raise_exception=True)

    session = LanguageOnboardingSession.objects.create(
        preferred_language=serializer.validated_data['preferred_language'],
    )
    return Response(LanguageOnboardingSessionSerializer(session).data, status=status.HTTP_201_CREATED)


@api_view(['POST'])
@permission_classes([AllowAny])
def save_voice_assistant_preference(request):
    cleanup_expired_onboarding_sessions()

    serializer = VoiceAssistantInputSerializer(data=request.data)
    serializer.is_valid(raise_exception=True)

    onboarding_token = serializer.validated_data['onboarding_token']
    voice_assistant_enabled = serializer.validated_data['voice_assistant_enabled']

    try:
        session = LanguageOnboardingSession.objects.get(token=onboarding_token)
    except LanguageOnboardingSession.DoesNotExist:
        return Response(
            {'detail': 'Invalid or expired onboarding token.'},
            status=status.HTTP_404_NOT_FOUND,
        )

    session.voice_assistant_enabled = voice_assistant_enabled
    session.save(update_fields=['voice_assistant_enabled'])

    return Response(LanguageOnboardingSessionSerializer(session).data, status=status.HTTP_200_OK)


@api_view(['POST'])
@permission_classes([AllowAny])
def register_phone_number(request):
    cleanup_expired_onboarding_sessions()

    serializer = PhoneInputSerializer(data=request.data)
    serializer.is_valid(raise_exception=True)

    phone_number = serializer.validated_data['phone_number']
    onboarding_token = serializer.validated_data['onboarding_token']

    try:
        session = LanguageOnboardingSession.objects.get(token=onboarding_token)
    except LanguageOnboardingSession.DoesNotExist:
        return Response(
            {'detail': 'Invalid or expired onboarding token.'},
            status=status.HTTP_404_NOT_FOUND,
        )

    user_preference, created = UserPreference.objects.get_or_create(
        phone_number=phone_number,
        defaults={
            'preferred_language': session.preferred_language,
            'voice_assistant_enabled': session.voice_assistant_enabled,
        },
    )

    if not created:
        user_preference.preferred_language = session.preferred_language
        user_preference.voice_assistant_enabled = session.voice_assistant_enabled
        user_preference.save(update_fields=['preferred_language', 'voice_assistant_enabled', 'updated_at'])

    # Remove temporary language record after successful phone registration.
    session.delete()

    response_data = UserPreferenceSerializer(user_preference).data
    response_data['created'] = created
    return Response(response_data, status=status.HTTP_201_CREATED if created else status.HTTP_200_OK)


@api_view(['POST'])
@permission_classes([AllowAny])
def cancel_onboarding(request):
    serializer = CancelOnboardingSerializer(data=request.data)
    serializer.is_valid(raise_exception=True)

    onboarding_token = serializer.validated_data['onboarding_token']
    deleted_count, _ = LanguageOnboardingSession.objects.filter(token=onboarding_token).delete()

    if deleted_count == 0:
        return Response({'detail': 'Session already removed or token invalid.'}, status=status.HTTP_404_NOT_FOUND)

    return Response({'detail': 'Onboarding cancelled and temporary language removed.'}, status=status.HTTP_200_OK)


@api_view(['POST'])
@permission_classes([AllowAny])
@parser_classes([MultiPartParser, FormParser])
def upload_scan_image(request):
    serializer = ScanImageUploadInputSerializer(data=request.data)
    serializer.is_valid(raise_exception=True)

    phone_number = serializer.validated_data['phone_number']

    try:
        user_preference = UserPreference.objects.get(phone_number=phone_number)
    except UserPreference.DoesNotExist:
        return Response(
            {'detail': 'Phone number not found. Register user first.'},
            status=status.HTTP_404_NOT_FOUND,
        )

    scan_upload = ScanImageUpload.objects.create(
        user_preference=user_preference,
        image=serializer.validated_data['image'],
        source=serializer.validated_data['source'],
        analysis_status='pending',
    )

    response_serializer = ScanImageUploadSerializer(scan_upload, context={'request': request})
    return Response(response_serializer.data, status=status.HTTP_201_CREATED)


@api_view(['GET'])
@permission_classes([AllowAny])
def list_scan_images(request):
    phone_number = request.query_params.get('phone_number')
    queryset = ScanImageUpload.objects.select_related('user_preference').all()

    if phone_number:
        queryset = queryset.filter(user_preference__phone_number=phone_number)

    response_serializer = ScanImageUploadSerializer(queryset, many=True, context={'request': request})
    return Response(response_serializer.data, status=status.HTTP_200_OK)