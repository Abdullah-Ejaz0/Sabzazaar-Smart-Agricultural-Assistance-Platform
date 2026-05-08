import json
from django.http import JsonResponse
from django.views.decorators.http import require_http_methods
from django.views.decorators.csrf import csrf_exempt
from rest_framework.decorators import api_view
from api.supabase_client import supabase
from api.middleware.auth import require_auth

@csrf_exempt
@api_view(["POST"])
@require_http_methods(["POST"])
def complete_onboarding(request):
    """POST /api/auth/onboarding/ — called after registration to save name, language, voice pref."""
    try:
        body = json.loads(request.body)
    except json.JSONDecodeError:
        return JsonResponse({'error': 'Invalid JSON'}, status=400)

    result = supabase.rpc('complete_onboarding', {
        'p_user_id':   body['user_id'],
        'p_full_name': body['full_name'],
        'p_language':  body.get('language', 'en'),
        'p_voice':     body.get('voice_assistance', False)
    }).execute()
    return JsonResponse({'status': 'onboarding complete'})


@csrf_exempt
@api_view(["POST"])
@require_auth
@require_http_methods(["POST"])
def update_settings(request):
    """POST /api/auth/settings/ — updates language and voice guidance preferences."""
    try:
        body = json.loads(request.body)
    except json.JSONDecodeError:
        return JsonResponse({'error': 'Invalid JSON'}, status=400)

    result = supabase.rpc('update_user_settings', {
        'p_user_id':  request.user_id,
        'p_language': body['language'],
        'p_voice':    body['voice_guidance']
    }).execute()
    return JsonResponse({'status': 'settings updated'})


@csrf_exempt
@api_view(["GET", "PATCH"])
@require_auth
@require_http_methods(["GET", "PATCH"])
def get_profile(request):
    """
    GET  /api/auth/profile/ — returns the authenticated user's full profile.
    PATCH /api/auth/profile/ — updates allowed profile fields (full_name, avatar_url, region, username).
    """
    if request.method == 'GET':
        result = supabase \
            .from_('profiles') \
            .select('*') \
            .eq('id', request.user_id) \
            .single() \
            .execute()
        return JsonResponse(result.data)

    # PATCH
    try:
        body = json.loads(request.body)
    except json.JSONDecodeError:
        return JsonResponse({'error': 'Invalid JSON'}, status=400)

    # Whitelist allowed fields — never let a user change their own role
    allowed = ['full_name', 'avatar_url', 'region', 'username']
    update_data = {k: v for k, v in body.items() if k in allowed}

    if not update_data:
        return JsonResponse({'error': 'No valid fields to update'}, status=400)

    supabase \
        .from_('profiles') \
        .update(update_data) \
        .eq('id', request.user_id) \
        .execute()

    return JsonResponse({'status': 'profile updated'})