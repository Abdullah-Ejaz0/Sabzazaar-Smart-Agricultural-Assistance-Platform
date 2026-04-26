import json
from django.http import JsonResponse
from django.views.decorators.http import require_http_methods
from django.views.decorators.csrf import csrf_exempt
from api.supabase_client import supabase, supabase_admin
from api.middleware.auth import require_auth


@csrf_exempt
@require_auth
@require_http_methods(["POST"])
def get_upload_url(request):
    """
    POST /api/storage/upload-url/
    Body: { "bucket": "scan-images" or "community-photos", "filename": "photo.jpg" }
    Returns a signed URL the frontend uses to upload directly to Supabase Storage.
    """
    try:
        body = json.loads(request.body)
    except json.JSONDecodeError:
        return JsonResponse({'error': 'Invalid JSON'}, status=400)

    bucket = body.get('bucket')
    filename = body.get('filename')

    if bucket not in ['scan-images', 'community-photos']:
        return JsonResponse({'error': 'Invalid bucket name. Use scan-images or community-photos'}, status=400)

    if not filename:
        return JsonResponse({'error': 'filename is required'}, status=400)

    # scan-images stored under user's folder so RLS policy (foldername = user_id) works
    if bucket == 'scan-images':
        path = f"{request.user_id}/{filename}"
    else:
        path = f"posts/{filename}"

    try:
        response = supabase_admin.storage \
            .from_(bucket) \
            .create_signed_upload_url(path)

        return JsonResponse({
            'signed_url': response.signed_url,
            'path': response.path,
            'token': response.token
        })
    except Exception as e:
        return JsonResponse({'error': str(e)}, status=500)

@require_auth
@require_http_methods(["GET"])
def chatbot_faqs(request):
    """GET /api/chatbot/faqs/ — returns localised FAQ content for the chatbot."""
    result = supabase.rpc('get_chatbot_faqs', {
        'p_category': request.GET.get('category'),
        'p_language': request.GET.get('language', 'en')
    }).execute()
    return JsonResponse(result.data, safe=False)


@require_auth
@require_http_methods(["GET"])
def broadcasts(request):
    """GET /api/broadcasts/ — returns broadcast alerts for the farmer's region."""
    result = supabase.rpc('get_broadcasts', {
        'p_region': request.GET.get('region'),
        'p_limit':  int(request.GET.get('limit', 10))
    }).execute()
    return JsonResponse(result.data, safe=False)


@require_http_methods(["GET"])
def health_check(request):
    """GET /api/health/ — no auth needed, verifies Django can reach Supabase."""
    try:
        result = supabase.from_('crops').select('name').limit(1).execute()
        return JsonResponse({'status': 'ok', 'db': 'connected'})
    except Exception as e:
        return JsonResponse({'status': 'error', 'detail': str(e)}, status=500)
